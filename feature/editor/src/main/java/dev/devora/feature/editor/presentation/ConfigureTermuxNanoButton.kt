package dev.devora.feature.editor.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.devora.feature.editor.domain.repository.NanoRepository
import dev.devora.feature.terminal.data.TermuxContract
import dev.devora.feature.terminal.domain.model.SessionAction
import dev.devora.feature.terminal.domain.model.TerminalCommandRequest
import dev.devora.feature.terminal.domain.repository.TerminalRepository

/**
 * One-time manual trigger for TERMUX_APP mode. Devora cannot read
 * Termux's home directory to know whether syntax highlighting is
 * already configured (separate app sandbox — same limitation noted
 * in Stage 4's ExternalAppsUnverified state), so this is an explicit
 * developer action rather than something Devora runs automatically.
 * The install script itself is idempotent, so pressing it more than
 * once is harmless.
 */
@Composable
fun ConfigureTermuxNanoButton(
    nanoRepository: NanoRepository,
    terminalRepository: TerminalRepository
) {
    Button(onClick = {
        val script = nanoRepository.buildTermuxAppSyntaxConfigureCommand()
        terminalRepository.runCommand(
            TerminalCommandRequest(
                workingDirectory = "/data/data/com.termux/files/home",
                command = TermuxContract.TERMUX_SHELL_BINARY,
                arguments = listOf("-c", script),
                sessionAction = SessionAction.OPEN_NEW_WINDOW
            )
        )
    }) {
        Text("Configure nano syntax highlighting in Termux")
    }
}