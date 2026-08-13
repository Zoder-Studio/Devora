package dev.devora.feature.terminal.data.execution

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine

class EmbeddedCommandExecutionEngine(
    private val commandRunner: EmbeddedCommandRunner
) : CommandExecutionEngine {
    override suspend fun run(
        workingDirectory: String,
        script: String,
        timeoutMillis: Long,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> = commandRunner.run(script, workingDirectory, timeoutMillis, onOutputLine)
}