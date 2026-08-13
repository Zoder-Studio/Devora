package dev.devora.feature.terminal.data.embedded

import dev.devora.core.common.result.DevoraResult
import dev.devora.core.logging.DevoraLogger
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

private const val TAG = "EmbeddedPrefixManager"

/**
 * Owns one Termux-compatible userland rooted at [rootDir]. Multiple
 * instances can coexist (e.g. one per workflow environment in Stage
 * 10) — this class no longer assumes it is the only prefix on the
 * device, only that it owns everything under its own rootDir.
 */
class EmbeddedPrefixManager(
    private val rootDir: File,
    private val versionStore: PinnedBootstrapVersionStore
) {
    private val prefixRoot: File get() = File(rootDir, "usr")
    private val installedMarker: File get() = File(rootDir, "usr-installed.marker")
    private val installedVersionMarker: File get() = File(rootDir, "usr-installed-version.txt")

    val prefixPath: String get() = prefixRoot.absolutePath
    val bashBinaryPath: String get() = File(prefixRoot, "bin/bash").absolutePath

    fun isPrepared(): Boolean = installedMarker.exists() && File(prefixRoot, "bin/bash").exists()

    fun installedReleaseTag(): String? =
        if (installedVersionMarker.exists()) installedVersionMarker.readText().trim() else null

    fun storageSizeBytes(): Long = if (rootDir.exists()) calculateDirectorySize(rootDir) else 0L

    /** Deletes everything under rootDir — used by "Reset Environment" / "Delete Environment" (spec section 10). */
    fun wipe(): DevoraResult<Unit> {
        return try {
            if (rootDir.exists() && !rootDir.deleteRecursively()) {
                throw java.io.IOException("Failed to fully delete ${rootDir.absolutePath}")
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to wipe environment at ${rootDir.absolutePath}", cause = e)
        }
    }

    suspend fun prepare(onProgress: (String) -> Unit = {}): DevoraResult<Unit> {
        if (isPrepared()) return DevoraResult.Success(Unit)
        val pinnedVersion = versionStore.read(BootstrapContract.DEFAULT_BOOTSTRAP_RELEASE_TAG)
        return installVersion(pinnedVersion.releaseTag, onProgress)
    }

    suspend fun reinstallWithVersion(releaseTag: String, onProgress: (String) -> Unit = {}): DevoraResult<Unit> {
        if (prefixRoot.exists()) {
            onProgress("Removing existing embedded engine...")
            prefixRoot.deleteRecursively()
        }
        installedMarker.delete()
        installedVersionMarker.delete()
        val result = installVersion(releaseTag, onProgress)
        if (result is DevoraResult.Success) {
            versionStore.write(PinnedBootstrapVersion(releaseTag))
        }
        return result
    }

    private suspend fun installVersion(releaseTag: String, onProgress: (String) -> Unit): DevoraResult<Unit> {
        val arch = DeviceAbi.resolveBootstrapArch()
            ?: return DevoraResult.Failure(
                message = "No supported bootstrap architecture for this device's ABIs: " +
                    android.os.Build.SUPPORTED_ABIS.joinToString()
            )
        return try {
            onProgress("Downloading bootstrap $releaseTag for $arch...")
            val zipFile = File(rootDir.parentFile ?: rootDir, "bootstrap-download-${rootDir.name}.zip")
            zipFile.parentFile?.mkdirs()
            downloadFile(BootstrapContract.downloadUrl(arch, releaseTag), zipFile)

            onProgress("Extracting bootstrap to ${prefixRoot.absolutePath}...")
            prefixRoot.mkdirs()
            extractZip(zipFile, prefixRoot)
            zipFile.delete()

            onProgress("Setting executable permissions...")
            markExecutables(File(prefixRoot, "bin"))
            markExecutables(File(prefixRoot, "libexec"))

            installedMarker.writeText(System.currentTimeMillis().toString())
            installedVersionMarker.writeText(releaseTag)
            onProgress("Environment ready ($releaseTag).")
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraLogger.e(TAG, "Failed to prepare embedded bootstrap at ${rootDir.absolutePath}", e)
            DevoraResult.Failure(message = "Failed to set up environment at ${rootDir.absolutePath}", cause = e)
        }
    }

    private fun downloadFile(url: String, destination: File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connect()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw java.io.IOException("Bootstrap download failed with HTTP ${connection.responseCode} from $url")
        }
        connection.inputStream.use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        }
    }

    private fun extractZip(zipFile: File, targetDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { output -> zipStream.copyTo(output) }
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
    }

    private fun markExecutables(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        dir.listFiles()?.forEach { file ->
            if (file.isFile) file.setExecutable(true, false) else if (file.isDirectory) markExecutables(file)
        }
    }

    private fun calculateDirectorySize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return size
    }
}