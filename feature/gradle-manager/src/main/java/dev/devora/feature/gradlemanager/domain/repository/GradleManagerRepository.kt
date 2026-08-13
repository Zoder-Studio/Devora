package dev.devora.feature.gradlemanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.gradlemanager.domain.model.GradleCacheInfo
import dev.devora.feature.gradlemanager.domain.model.GradleDaemonStatus
import dev.devora.feature.gradlemanager.domain.model.GradleWrapperInfo

interface GradleManagerRepository {
    fun readWrapperInfo(projectRootPath: String): DevoraResult<GradleWrapperInfo>

    /** Reads gradle.properties as raw text for editing via Nano — Devora does not parse/mutate it itself. */
    fun readGradleProperties(projectRootPath: String): DevoraResult<String>

    suspend fun getCacheInfo(): DevoraResult<GradleCacheInfo>

    /** Deletes the Gradle cache directory. Only called after explicit developer confirmation (spec section 8). */
    suspend fun clearCache(): DevoraResult<Unit>

    suspend fun getDaemonStatus(projectRootPath: String, onOutputLine: (String) -> Unit): DevoraResult<GradleDaemonStatus>

    suspend fun stopDaemon(projectRootPath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit>

    /** Runs "./gradlew :app:dependencies" or ":app:dependencyInsight --dependency <name>" verbatim. */
    suspend fun runDependencyInsight(
        projectRootPath: String,
        moduleTask: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit>

    fun isOfflineModeEnabled(projectRootPath: String): Boolean

    /** Toggles offline mode by editing gradle.properties' "org.gradle.offline" line — explicit developer action only. */
    suspend fun setOfflineMode(projectRootPath: String, enabled: Boolean): DevoraResult<Unit>
}