package dev.devora.feature.buildsystem.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.buildsystem.domain.model.BuildRun
import kotlinx.coroutines.flow.StateFlow

interface BuildRepository {
    val activeBuilds: StateFlow<List<BuildRun>>

    suspend fun runTask(projectRootPath: String, gradleTask: String): DevoraResult<BuildRun>

    fun readLogTail(buildRun: BuildRun, maxLines: Int = 100): List<String>

    fun exportLog(buildRun: BuildRun, destinationFile: java.io.File): DevoraResult<Unit>
}