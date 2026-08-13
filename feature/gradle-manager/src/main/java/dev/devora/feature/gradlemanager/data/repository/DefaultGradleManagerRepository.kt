package dev.devora.feature.gradlemanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.gradlemanager.domain.model.GradleCacheInfo
import dev.devora.feature.gradlemanager.domain.model.GradleDaemonStatus
import dev.devora.feature.gradlemanager.domain.model.GradleWrapperInfo
import dev.devora.feature.gradlemanager.domain.repository.GradleManagerRepository
import dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import java.io.File

class DefaultGradleManagerRepository(
    private val prefixManager: EmbeddedPrefixManager,
    private val commandRunner: EmbeddedCommandRunner
) : GradleManagerRepository {

    override fun readWrapperInfo(projectRootPath: String): DevoraResult<GradleWrapperInfo> {
        val wrapperPropertiesFile = File(projectRootPath, "gradle/wrapper/gradle-wrapper.properties")
        val gradlewFile = File(projectRootPath, "gradlew")

        if (!wrapperPropertiesFile.exists()) {
            return DevoraResult.Success(
                GradleWrapperInfo(
                    distributionUrl = null,
                    distributionVersion = null,
                    gradlewExists = gradlewFile.exists(),
                    gradlewIsExecutable = gradlewFile.canExecute()
                )
            )
        }

        return try {
            val properties = java.util.Properties()
            wrapperPropertiesFile.inputStream().use { properties.load(it) }
            val distributionUrl = properties.getProperty("distributionUrl")
            val version = distributionUrl?.let { extractVersionFromDistributionUrl(it) }

            DevoraResult.Success(
                GradleWrapperInfo(
                    distributionUrl = distributionUrl,
                    distributionVersion = version,
                    gradlewExists = gradlewFile.exists(),
                    gradlewIsExecutable = gradlewFile.canExecute()
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to read gradle-wrapper.properties", cause = e)
        }
    }

    override fun readGradleProperties(projectRootPath: String): DevoraResult<String> {
        val file = File(projectRootPath, "gradle.properties")
        if (!file.exists()) {
            return DevoraResult.Failure(message = "gradle.properties does not exist at $projectRootPath")
        }
        return try {
            DevoraResult.Success(file.readText())
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to read gradle.properties", cause = e)
        }
    }

    override suspend fun getCacheInfo(): DevoraResult<GradleCacheInfo> {
        val cacheDir = File(prefixManager.prefixPath, "home/.gradle/caches")
        return try {
            val size = if (cacheDir.exists()) calculateDirectorySize(cacheDir) else 0L
            DevoraResult.Success(
                GradleCacheInfo(
                    cacheDirPath = cacheDir.absolutePath,
                    sizeBytes = size,
                    exists = cacheDir.exists()
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to compute Gradle cache size", cause = e)
        }
    }

    override suspend fun clearCache(): DevoraResult<Unit> {
        val cacheDir = File(prefixManager.prefixPath, "home/.gradle/caches")
        if (!cacheDir.exists()) return DevoraResult.Success(Unit)
        return try {
            if (!cacheDir.deleteRecursively()) {
                throw java.io.IOException("Failed to fully delete ${cacheDir.absolutePath}")
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to clear Gradle cache", cause = e)
        }
    }

    override suspend fun getDaemonStatus(
        projectRootPath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<GradleDaemonStatus> {
        val output = StringBuilder()
        val result = commandRunner.run(
            script = "cd '$projectRootPath' && ./gradlew --status",
            onOutputLine = { line -> output.appendLine(line); onOutputLine(line) }
        )
        if (result is DevoraResult.Failure) {
            return DevoraResult.Failure(message = result.message, cause = result.cause)
        }
        val rawOutput = output.toString()
        val isRunning = rawOutput.contains("IDLE") || rawOutput.contains("BUSY")
        return DevoraResult.Success(GradleDaemonStatus(isRunning = isRunning, rawStatusOutput = rawOutput))
    }

    override suspend fun stopDaemon(
        projectRootPath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        return commandRunner.run(
            script = "cd '$projectRootPath' && ./gradlew --stop",
            onOutputLine = onOutputLine
        )
    }

    override suspend fun runDependencyInsight(
        projectRootPath: String,
        moduleTask: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        return commandRunner.run(
            script = "cd '$projectRootPath' && ./gradlew $moduleTask",
            timeoutMillis = 300_000L,
            onOutputLine = onOutputLine
        )
    }

    override fun isOfflineModeEnabled(projectRootPath: String): Boolean {
        val propertiesFile = File(projectRootPath, "gradle.properties")
        if (!propertiesFile.exists()) return false
        return propertiesFile.readLines().any { line ->
            line.trim().equals("org.gradle.offline=true", ignoreCase = true)
        }
    }

    override suspend fun setOfflineMode(projectRootPath: String, enabled: Boolean): DevoraResult<Unit> {
        val propertiesFile = File(projectRootPath, "gradle.properties")
        return try {
            val lines = if (propertiesFile.exists()) {
                propertiesFile.readLines().filterNot { it.trim().startsWith("org.gradle.offline") }
            } else {
                emptyList()
            }
            val newLines = lines + "org.gradle.offline=$enabled"
            propertiesFile.parentFile?.mkdirs()
            propertiesFile.writeText(newLines.joinToString("\n") + "\n")
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to update gradle.properties", cause = e)
        }
    }

    private fun extractVersionFromDistributionUrl(url: String): String? {
        // Real distributionUrl format: https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
        val regex = Regex("""gradle-([\d.]+)-(bin|all)\.zip""")
        return regex.find(url)?.groupValues?.get(1)
    }

    private fun calculateDirectorySize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return size
    }
}