package dev.devora.feature.gradlemanager.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.gradlemanager.domain.model.GradleCacheInfo
import dev.devora.feature.gradlemanager.domain.model.GradleDaemonStatus
import dev.devora.feature.gradlemanager.domain.model.GradleWrapperInfo
import dev.devora.feature.gradlemanager.domain.repository.GradleManagerRepository

class ReadGradleWrapperInfoUseCase(private val repository: GradleManagerRepository) {
    operator fun invoke(projectRootPath: String): DevoraResult<GradleWrapperInfo> =
        repository.readWrapperInfo(projectRootPath)
}

class GetGradleCacheInfoUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(): DevoraResult<GradleCacheInfo> = repository.getCacheInfo()
}

class ClearGradleCacheUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(): DevoraResult<Unit> = repository.clearCache()
}

class GetGradleDaemonStatusUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(
        projectRootPath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<GradleDaemonStatus> = repository.getDaemonStatus(projectRootPath, onOutputLine)
}

class StopGradleDaemonUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(projectRootPath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        repository.stopDaemon(projectRootPath, onOutputLine)
}

class RunDependencyInsightUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(
        projectRootPath: String,
        moduleTask: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> = repository.runDependencyInsight(projectRootPath, moduleTask, onOutputLine)
}

class SetGradleOfflineModeUseCase(private val repository: GradleManagerRepository) {
    suspend operator fun invoke(projectRootPath: String, enabled: Boolean): DevoraResult<Unit> =
        repository.setOfflineMode(projectRootPath, enabled)
}