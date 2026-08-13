package dev.devora.feature.editor.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class NanoLaunchEvent {
    data class NavigateToEmbeddedNano(val filePath: String) : NanoLaunchEvent()
    data class NavigateToInstallNano(val filePath: String) : NanoLaunchEvent()
    data class Failed(val message: String) : NanoLaunchEvent()
}

interface NanoLaunchEventBus {
    val events: SharedFlow<NanoLaunchEvent>
    suspend fun emit(event: NanoLaunchEvent)
}

class DefaultNanoLaunchEventBus : NanoLaunchEventBus {
    private val _events = MutableSharedFlow<NanoLaunchEvent>(extraBufferCapacity = 4)
    override val events: SharedFlow<NanoLaunchEvent> = _events
    override suspend fun emit(event: NanoLaunchEvent) {
        _events.emit(event)
    }
}