package dev.devora.feature.terminal.data.execution

import dev.devora.feature.terminal.data.TermuxContract
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.execution.EnginePaths
import dev.devora.feature.terminal.domain.model.TerminalEngineMode

class DefaultEnginePaths(
    private val engineSelector: TerminalEngineSelector,
    private val embeddedPrefixManager: EmbeddedPrefixManager
) : EnginePaths {
    override fun currentPrefixPath(): String = when (engineSelector.selectMode()) {
        TerminalEngineMode.TERMUX_APP -> "/data/data/${TermuxContract.TERMUX_PACKAGE_NAME}/files/usr"
        TerminalEngineMode.EMBEDDED_BOOTSTRAP -> embeddedPrefixManager.prefixPath
    }
}