package dev.devora.feature.terminal.data.execution

import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.model.TerminalEngineMode

class DefaultCommandExecutionEngineProvider(
    private val engineSelector: TerminalEngineSelector,
    private val embeddedEngine: CommandExecutionEngine,
    private val termuxAppEngine: CommandExecutionEngine
) : CommandExecutionEngineProvider {
    override fun current(): CommandExecutionEngine = when (engineSelector.selectMode()) {
        TerminalEngineMode.TERMUX_APP -> termuxAppEngine
        TerminalEngineMode.EMBEDDED_BOOTSTRAP -> embeddedEngine
    }
}