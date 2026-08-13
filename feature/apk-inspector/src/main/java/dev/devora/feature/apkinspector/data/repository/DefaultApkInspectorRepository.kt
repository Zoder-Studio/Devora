package dev.devora.feature.apkinspector.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.apkinspector.domain.model.*
import dev.devora.feature.apkinspector.domain.repository.ApkInspectorRepository
import com.android.apksig.ApkVerifier
import java.io.File
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.zip.ZipFile

class DefaultApkInspectorRepository(
    private val context: Context
) : ApkInspectorRepository {

    override suspend fun inspectApk(apkFilePath: String): DevoraResult<ApkInspectionResult> {
        val file = File(apkFilePath)
        if (!file.exists()) {
            return DevoraResult.Failure(message = "APK file does not exist: $apkFilePath")
        }

        return try {
            val packageManager = context.packageManager

            @Suppress("DEPRECATION")
            val flags = PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS

            val packageInfo = packageManager.getPackageArchiveInfo(apkFilePath, flags)
                ?: return DevoraResult.Failure(message = "Failed to parse APK manifest: $apkFilePath (not a valid APK)")

            packageInfo.applicationInfo?.sourceDir = apkFilePath
            packageInfo.applicationInfo?.publicSourceDir = apkFilePath

            DevoraResult.Success(
                ApkInspectionResult(
                    general = buildGeneralInfo(file),
                    application = buildApplicationInfo(packageManager, packageInfo),
                    android = buildAndroidInfo(packageInfo, file),
                    signing = buildSigningInfo(file),
                    components = buildComponentsInfo(packageInfo),
                    permissions = buildPermissionsInfo(packageInfo),
                    files = buildFilesInfo(file)
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to inspect APK: $apkFilePath", cause = e)
        }
    }

    private fun buildGeneralInfo(file: File): ApkGeneralInfo {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        return ApkGeneralInfo(
            fileName = file.name,
            fileSizeBytes = file.length(),
            format = "APK",
            sha256 = digest.digest().joinToString("") { "%02x".format(it) }
        )
    }

    private fun buildApplicationInfo(pm: PackageManager, info: PackageInfo): ApkApplicationInfo {
        val appInfo = info.applicationInfo
        val label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: info.packageName
        val iconAvailable = try {
            appInfo?.let { pm.getApplicationIcon(it) } != null
        } catch (e: Exception) {
            false
        }
        @Suppress("DEPRECATION")
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
        return ApkApplicationInfo(
            packageName = info.packageName,
            appLabel = label,
            versionName = info.versionName,
            versionCode = versionCode,
            iconAvailable = iconAvailable
        )
    }

    private fun buildAndroidInfo(info: PackageInfo, file: File): ApkAndroidInfo {
        val appInfo = info.applicationInfo
        val minSdk = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            appInfo?.minSdkVersion ?: 0
        } else 0
        val targetSdk = appInfo?.targetSdkVersion ?: 0

        val abis = mutableListOf<String>()
        ZipFile(file).use { zip ->
            zip.entries().asSequence()
                .filter { it.name.startsWith("lib/") && it.isDirectory.not() }
                .mapNotNull { it.name.split("/").getOrNull(1) }
                .distinct()
                .forEach { abis.add(it) }
        }

        return ApkAndroidInfo(
            minSdk = minSdk,
            targetSdk = targetSdk,
            compileSdk = null, // not reliably exposed via PackageManager; would require reading manifest's compileSdkVersion attribute directly via binary XML parsing, not done here
            supportedAbis = abis
        )
    }

    /**
     * Uses Google's official apksig library — the same library apksigner
     * itself is built on — to verify signing schemes and read
     * certificates. Devora does not reimplement signature verification.
     */
    private fun buildSigningInfo(file: File): ApkSigningInfo {
        return try {
            val verifier = ApkVerifier.Builder(file).build()
            val result = verifier.verify()

            val schemes = mutableListOf<String>()
            if (result.isVerifiedUsingV1Scheme) schemes.add("V1")
            if (result.isVerifiedUsingV2Scheme) schemes.add("V2")
            if (result.isVerifiedUsingV3Scheme) schemes.add("V3")
            if (result.isVerifiedUsingV4Scheme) schemes.add("V4")

            val certificates = result.signerCertificates.map { cert: X509Certificate ->
                ApkCertificateInfo(
                    subject = cert.subjectX500Principal.name,
                    issuer = cert.issuerX500Principal.name,
                    sha256Fingerprint = MessageDigest.getInstance("SHA-256")
                        .digest(cert.encoded)
                        .joinToString(":") { "%02X".format(it) }
                )
            }

            ApkSigningInfo(
                isSigned = result.isVerified,
                schemesUsed = schemes,
                certificates = certificates
            )
        } catch (e: Exception) {
            ApkSigningInfo(isSigned = false, schemesUsed = emptyList(), certificates = emptyList())
        }
    }

    private fun buildComponentsInfo(info: PackageInfo): ApkComponentsInfo = ApkComponentsInfo(
        activities = info.activities?.map { it.name } ?: emptyList(),
        services = info.services?.map { it.name } ?: emptyList(),
        receivers = info.receivers?.map { it.name } ?: emptyList(),
        providers = info.providers?.map { it.name } ?: emptyList()
    )

    private fun buildPermissionsInfo(info: PackageInfo): ApkPermissionsInfo = ApkPermissionsInfo(
        requested = info.requestedPermissions?.toList() ?: emptyList(),
        declared = info.permissions?.map { it.name } ?: emptyList()
    )

    private fun buildFilesInfo(file: File): ApkFilesInfo {
        var dexCount = 0
        var hasResources = false
        var hasAssets = false
        var hasRes = false
        val libDirs = mutableSetOf<String>()

        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                when {
                    entry.name.matches(Regex("classes\\d*\\.dex")) -> dexCount++
                    entry.name == "resources.arsc" -> hasResources = true
                    entry.name.startsWith("assets/") -> hasAssets = true
                    entry.name.startsWith("res/") -> hasRes = true
                    entry.name.startsWith("lib/") && !entry.isDirectory -> {
                        entry.name.split("/").getOrNull(1)?.let { libDirs.add(it) }
                    }
                }
            }
        }

        return ApkFilesInfo(
            hasClassesDex = dexCount > 0,
            classesDexCount = dexCount,
            hasResourcesArsc = hasResources,
            libDirectories = libDirs.toList(),
            hasAssets = hasAssets,
            hasRes = hasRes
        )
    }
}