package dev.devora.feature.buildsystem.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.buildsystem.domain.model.BuildRun
import dev.devora.feature.buildsystem.domain.repository.BuildRepository

class RunGradleTaskUseCase(private val repository: BuildRepository) {
    suspend operator fun invoke(projectRootPath: String, gradleTask: String): DevoraResult<BuildRun> =
        repository.runTask(projectRootPath, gradleTask)
}

class ReadBuildLogTailUseCase(private val repository: BuildRepository) {
    operator fun invoke(buildRun: BuildRun, maxLines: Int = 100): List<String> =
        repository.readLogTail(buildRun, maxLines)
}

class ExportBuildLogUseCase(private val repository: BuildRepository) {
    operator fun invoke(buildRun: BuildRun, destinationFile: java.io.File): DevoraResult<Unit> =
        repository.exportLog(buildRun, destinationFile)
}