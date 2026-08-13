package dev.devora.feature.terminal.domain.engine

import dev.devora.feature.terminal.domain.model.TerminalEngineMode

interface TerminalEngineSelector {
    fun selectMode(): TerminalEngineMode
}