package dev.devora.feature.editor.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.core.logging.DevoraLogger
import dev.devora.feature.editor.domain.event.NanoLaunchEvent
import dev.devora.feature.editor.domain.event.NanoLaunchEventBus
import dev.devora.feature.editor.domain.model.NanoLaunchTarget
import dev.devora.feature.editor.domain.repository.NanoRepository
import dev.devora.feature.filemanager.domain.action.OpenFileInNanoAction
import dev.devora.feature.terminal.data.TermuxContract
import dev.devora.feature.terminal.domain.model.SessionAction
import dev.devora.feature.terminal.domain.model.TerminalCommandRequest
import dev.devora.feature.terminal.domain.repository.TerminalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton
import java.io.File

private const val TAG = "OpenFileInNanoAction"

@Module
@InstallIn(SingletonComponent::class)
object EditorActionModule {

    @Provides
    @Singleton
    fun provideNanoLaunchEventBus(): NanoLaunchEventBus = DefaultNanoLaunchEventBus()

    @Provides
    @Singleton
    fun provideOpenFileInNanoAction(
        nanoRepository: NanoRepository,
        terminalRepository: TerminalRepository,
        eventBus: NanoLaunchEventBus
    ): OpenFileInNanoAction = object : OpenFileInNanoAction {
        private val scope = CoroutineScope(Dispatchers.Default)

        override fun openInNano(filePath: String) {
            when (val target = nanoRepository.resolveLaunchTarget(filePath)) {
                is NanoLaunchTarget.TermuxAppWindow -> {
                    val result = terminalRepository.runCommand(
                        TerminalCommandRequest(
                            workingDirectory = File(filePath).parent ?: "/",
                            command = "${TermuxContract.TERMUX_SHELL_BINARY}",
                            arguments = listOf("-c", "nano '${target.filePath}'"),
                            sessionAction = SessionAction.OPEN_NEW_WINDOW
                        )
                    )
                    result.onFailure { failure ->
                        DevoraLogger.e(TAG, "Failed to open nano in Termux app: ${failure.message}", failure.cause)
                        scope.launch { eventBus.emit(NanoLaunchEvent.Failed(failure.message)) }
                    }
                }
                is NanoLaunchTarget.EmbeddedSession -> {
                    scope.launch { eventBus.emit(NanoLaunchEvent.NavigateToEmbeddedNano(target.filePath)) }
                }
                is NanoLaunchTarget.EmbeddedNeedsInstall -> {
                    scope.launch { eventBus.emit(NanoLaunchEvent.NavigateToInstallNano(target.filePath)) }
                }
                is NanoLaunchTarget.Unavailable -> {
                    scope.launch { eventBus.emit(NanoLaunchEvent.Failed(target.reason)) }
                }
            }
        }
    }
}