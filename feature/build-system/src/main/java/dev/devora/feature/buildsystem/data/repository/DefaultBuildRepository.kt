package dev.devora.feature.buildsystem.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.buildsystem.data.BuildLogStore
import dev.devora.feature.buildsystem.domain.model.BuildRun
import dev.devora.feature.buildsystem.domain.model.BuildStatus
import dev.devora.feature.buildsystem.domain.repository.BuildRepository
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DefaultBuildRepository(
    private val engineProvider: CommandExecutionEngineProvider,
    private val enginePaths: EnginePaths,
    private val logStore: BuildLogStore
) : BuildRepository {

    private val _activeBuilds = MutableStateFlow<List<BuildRun>>(emptyList())
    override val activeBuilds: StateFlow<List<BuildRun>> = _activeBuilds

    override suspend fun runTask(projectRootPath: String, gradleTask: String): DevoraResult<BuildRun> {
        val id = UUID.randomUUID().toString()
        val (logFile, writer) = logStore.createWriterFor(id)

        var run = BuildRun(
            id = id,
            projectRootPath = projectRootPath,
            gradleTask = gradleTask,
            status = BuildStatus.RUNNING,
            startedAtEpochMillis = System.currentTimeMillis(),
            finishedAtEpochMillis = null,
            exitCode = null,
            stdoutLineCount = 0,
            stderrLineCount = 0,
            stderrAvailable = false,
            logFilePath = logFile.absolutePath
        )
        upsert(run)

        val script = "cd '$projectRootPath' && ./gradlew $gradleTask"
        val workingDirectory = enginePaths.currentPrefixPath()

        val result = engineProvider.current().runCapturing(
            workingDirectory = workingDirectory,
            script = script,
            timeoutMillis = 1_800_000L, // 30 minutes — real Android builds can be slow, esp. clean builds
            onOutputLine = { line ->
                writer.appendLine(line)
                writer.flush()
            }
        )
        writer.close()

        return when (result) {
            is DevoraResult.Success -> {
                val captured = result.data
                val stdoutLineCount = captured.stdout.lines().count { it.isNotBlank() }
                val stderrLineCount = if (captured.stderrAvailable) {
                    captured.stderr.lines().count { it.isNotBlank() }
                } else 0

                run = run.copy(
                    status = if (captured.exitCode == 0) BuildStatus.SUCCESS else BuildStatus.FAILED,
                    finishedAtEpochMillis = System.currentTimeMillis(),
                    exitCode = captured.exitCode,
                    stdoutLineCount = stdoutLineCount,
                    stderrLineCount = stderrLineCount,
                    stderrAvailable = captured.stderrAvailable
                )
                upsert(run)
                DevoraResult.Success(run)
            }
            is DevoraResult.Failure -> {
                run = run.copy(
                    status = BuildStatus.FAILED,
                    finishedAtEpochMillis = System.currentTimeMillis(),
                    exitCode = -1,
                    stdoutLineCount = logStore.countLines(logFile)
                )
                upsert(run)
                DevoraResult.Failure(message = result.message, cause = result.cause)
            }
        }
    }

    override fun readLogTail(buildRun: BuildRun, maxLines: Int): List<String> {
        return logStore.readTail(java.io.File(buildRun.logFilePath), maxLines)
    }

    override fun exportLog(buildRun: BuildRun, destinationFile: java.io.File): DevoraResult<Unit> {
        return try {
            logStore.exportTo(java.io.File(buildRun.logFilePath), destinationFile)
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to export build log", cause = e)
        }
    }

    private fun upsert(run: BuildRun) {
        _activeBuilds.update { current ->
            val withoutOld = current.filterNot { it.id == run.id }
            withoutOld + run
        }
    }
}