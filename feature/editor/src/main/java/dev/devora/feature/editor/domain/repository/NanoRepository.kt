package dev.devora.feature.editor.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.editor.domain.model.NanoLaunchTarget

interface NanoRepository {
    fun resolveLaunchTarget(filePath: String): NanoLaunchTarget

    suspend fun installNanoInEmbeddedEngine(onOutputLine: (String) -> Unit): DevoraResult<Unit>

    /**
     * Runs the official nano-syntax-highlighting install script and
     * ensures "set linenumbers" is present in nanorc. Idempotent: safe
     * to call multiple times (the upstream script itself only appends
     * missing include lines; the linenumbers check is done the same way).
     */
    suspend fun configureSyntaxHighlightingEmbedded(onOutputLine: (String) -> Unit): DevoraResult<Unit>

    /**
     * Builds the equivalent shell command for TERMUX_APP mode, dispatched
     * via RUN_COMMAND since Devora cannot read/write Termux's home
     * directory directly from its own process.
     */
    fun buildTermuxAppSyntaxConfigureCommand(): String
}