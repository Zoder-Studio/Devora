package dev.devora.feature.terminal.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.logging.DevoraLogger
import dev.devora.feature.terminal.data.TermuxContract
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.model.SessionAction
import dev.devora.feature.terminal.domain.model.TermuxIntegrationState
import dev.devora.feature.terminal.domain.model.TerminalCommandRequest
import dev.devora.feature.terminal.domain.model.TerminalEngineMode
import dev.devora.feature.terminal.domain.repository.TerminalRepository

private const val TAG = "TerminalRepository"

class DefaultTerminalRepository(
    private val context: Context,
    private val engineSelector: TerminalEngineSelector,
    private val embeddedPrefixManager: EmbeddedPrefixManager
) : TerminalRepository {

    override fun currentEngineMode(): TerminalEngineMode = engineSelector.selectMode()

    override fun checkIntegrationState(): TermuxIntegrationState {
        return when (currentEngineMode()) {
            TerminalEngineMode.TERMUX_APP -> {
                if (!hasRunCommandPermission()) {
                    TermuxIntegrationState.RunCommandPermissionMissing
                } else {
                    TermuxIntegrationState.ExternalAppsUnverified
                }
            }
            TerminalEngineMode.EMBEDDED_BOOTSTRAP -> {
                if (embeddedPrefixManager.isPrepared()) {
                    TermuxIntegrationState.Ready
                } else {
                    TermuxIntegrationState.Checking
                }
            }
        }
    }

    override fun openTerminalAt(directoryPath: String): DevoraResult<Unit> {
        return when (currentEngineMode()) {
            TerminalEngineMode.TERMUX_APP -> runCommand(
                TerminalCommandRequest(
                    workingDirectory = directoryPath,
                    command = TermuxContract.TERMUX_SHELL_BINARY,
                    sessionAction = SessionAction.OPEN_NEW_WINDOW
                )
            )
            TerminalEngineMode.EMBEDDED_BOOTSTRAP -> DevoraResult.Failure(
                message = "Embedded engine sessions must be opened through the " +
                    "in-app terminal screen, not dispatched in the background."
            )
        }
    }

    override fun runCommand(request: TerminalCommandRequest): DevoraResult<Unit> {
        if (currentEngineMode() != TerminalEngineMode.TERMUX_APP) {
            return DevoraResult.Failure(
                message = "runCommand() via RUN_COMMAND is only available in TERMUX_APP mode."
            )
        }
        if (!hasRunCommandPermission()) {
            return DevoraResult.Failure(message = "RUN_COMMAND permission not granted.")
        }

        return try {
            val intent = Intent().apply {
                setClassName(TermuxContract.TERMUX_PACKAGE_NAME, TermuxContract.RUN_COMMAND_SERVICE)
                action = TermuxContract.RUN_COMMAND_ACTION
                putExtra(TermuxContract.EXTRA_COMMAND_PATH, request.command)
                putExtra(TermuxContract.EXTRA_COMMAND_ARGUMENTS, request.arguments.toTypedArray())
                putExtra(TermuxContract.EXTRA_COMMAND_WORKDIR, request.workingDirectory)
                putExtra(
                    TermuxContract.EXTRA_COMMAND_BACKGROUND,
                    request.sessionAction == SessionAction.RUN_IN_BACKGROUND
                )
                if (request.sessionAction == SessionAction.OPEN_NEW_WINDOW) {
                    putExtra(
                        TermuxContract.EXTRA_COMMAND_SESSION_ACTION,
                        TermuxContract.SESSION_ACTION_SWITCH_TO_NEW
                    )
                }
            }
            context.startForegroundService(intent)
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraLogger.e(TAG, "Failed to dispatch RUN_COMMAND to Termux", e)
            DevoraResult.Failure(message = "Failed to start Termux command: ${request.command}", cause = e)
        }
    }

    override suspend fun prepareEmbeddedEngineIfNeeded(onProgress: (String) -> Unit): DevoraResult<Unit> {
        if (currentEngineMode() != TerminalEngineMode.EMBEDDED_BOOTSTRAP) {
            return DevoraResult.Success(Unit)
        }
        return embeddedPrefixManager.prepare(onProgress)
    }

    private fun hasRunCommandPermission(): Boolean {
        return context.checkSelfPermission(TermuxContract.RUN_COMMAND_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED
    }
}