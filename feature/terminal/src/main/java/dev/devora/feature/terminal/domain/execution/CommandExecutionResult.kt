package dev.devora.feature.terminal.domain.execution

/**
 * stderr is only populated when the engine can genuinely distinguish
 * it from stdout (TERMUX_APP mode, via RUN_COMMAND's separate result
 * extras). In EMBEDDED_BOOTSTRAP mode, the pty merges both streams by
 * nature — stderrAvailable is false there and stdout holds everything.
 * Build System UI must branch on stderrAvailable rather than assuming
 * stderr is always meaningful.
 */
data class CommandExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val stderrAvailable: Boolean
)