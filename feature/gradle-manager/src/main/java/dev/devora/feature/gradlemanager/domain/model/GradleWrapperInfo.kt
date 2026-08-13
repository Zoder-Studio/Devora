package dev.devora.feature.gradlemanager.domain.model

/**
 * Reflects exactly what a project's gradle-wrapper.properties says.
 * Devora never overrides this — the wrapper file is the single source
 * of truth for a project's Gradle version (spec section 8: "Wrapper
 * adalah sumber utama versi Gradle project").
 */
data class GradleWrapperInfo(
    val distributionUrl: String?,
    val distributionVersion: String?,
    val gradlewExists: Boolean,
    val gradlewIsExecutable: Boolean
)

data class GradleDaemonStatus(
    val isRunning: Boolean,
    val rawStatusOutput: String
)

data class GradleCacheInfo(
    val cacheDirPath: String,
    val sizeBytes: Long,
    val exists: Boolean
)