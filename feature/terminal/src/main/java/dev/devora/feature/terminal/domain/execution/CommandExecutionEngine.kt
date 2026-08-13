package dev.devora.feature.terminal.domain.execution

import dev.devora.core.common.result.DevoraResult

interface CommandExecutionEngine {
    suspend fun run(
        workingDirectory: String,
        script: String,
        timeoutMillis: Long = 300_000L,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit>

    /** Same execution, but returns the full captured result for Build Log purposes (see CommandExecutionResult docs on stream separation). */
    suspend fun runCapturing(
        workingDirectory: String,
        script: String,
        timeoutMillis: Long = 300_000L,
        onOutputLine: (String) -> Unit
    ): DevoraResult<CommandExecutionResult>
}