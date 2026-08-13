package dev.devora.feature.terminal.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class TerminalLaunchEvent {
    data class NavigateToEmbeddedTerminal(val directoryPath: String) : TerminalLaunchEvent()
    data class Failed(val message: String) : TerminalLaunchEvent()
}

interface TerminalLaunchEventBus {
    val events: SharedFlow<TerminalLaunchEvent>
    suspend fun emit(event: TerminalLaunchEvent)
}

class DefaultTerminalLaunchEventBus : TerminalLaunchEventBus {
    private val _events = MutableSharedFlow<TerminalLaunchEvent>(extraBufferCapacity = 4)
    override val events: SharedFlow<TerminalLaunchEvent> = _events
    override suspend fun emit(event: TerminalLaunchEvent) {
        _events.emit(event)
    }
}