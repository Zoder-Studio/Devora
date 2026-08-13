package dev.devora.feature.terminal.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import dev.devora.core.logging.DevoraLogger
import dev.devora.feature.filemanager.domain.action.OpenTerminalAtPathAction
import dev.devora.feature.terminal.domain.event.TerminalLaunchEvent
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import dev.devora.feature.terminal.domain.model.TerminalEngineMode
import dev.devora.feature.terminal.domain.repository.TerminalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

private const val TAG = "OpenTerminalAtPathAction"

@Module
@InstallIn(SingletonComponent::class)
object TerminalActionModule {

    @Provides
    @Singleton
    fun provideOpenTerminalAtPathAction(
        terminalRepository: TerminalRepository,
        eventBus: TerminalLaunchEventBus
    ): OpenTerminalAtPathAction = object : OpenTerminalAtPathAction {
        private val scope = CoroutineScope(Dispatchers.Default)

        override fun openTerminal(directoryPath: String) {
            when (terminalRepository.currentEngineMode()) {
                TerminalEngineMode.TERMUX_APP -> {
                    terminalRepository.openTerminalAt(directoryPath).onFailure { failure ->
                        DevoraLogger.e(TAG, "openTerminal failed: ${failure.message}", failure.cause)
                        scope.launch { eventBus.emit(TerminalLaunchEvent.Failed(failure.message)) }
                    }
                }
                TerminalEngineMode.EMBEDDED_BOOTSTRAP -> {
                    scope.launch {
                        eventBus.emit(TerminalLaunchEvent.NavigateToEmbeddedTerminal(directoryPath))
                    }
                }
            }
        }
    }
}