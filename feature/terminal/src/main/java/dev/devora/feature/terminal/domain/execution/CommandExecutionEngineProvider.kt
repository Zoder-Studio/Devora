package dev.devora.feature.terminal.domain.execution

interface CommandExecutionEngineProvider {
    /** Resolves the correct engine for the current device state — callers never check TerminalEngineMode themselves. */
    fun current(): CommandExecutionEngine
}