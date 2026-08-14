package dev.devora.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarMessage
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.core.ui.theme.DevoraStatusColors
import dev.devora.feature.editor.domain.event.NanoLaunchEventBus
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DevoraRootScaffold(
    snackbarController: DevoraSnackbarController,
    terminalLaunchEventBus: TerminalLaunchEventBus,
    nanoLaunchEventBus: NanoLaunchEventBus,
    initialDeepLinkPath: String?
) {
    var currentMessage by remember { mutableStateOf<DevoraSnackbarMessage?>(null) }

    LaunchedEffect(Unit) {
        snackbarController.messages.collectLatest { message ->
            currentMessage = message
            delay(3500)
            currentMessage = null
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(padding)) {
                DevoraNavHost(
                    terminalLaunchEventBus = terminalLaunchEventBus,
                    nanoLaunchEventBus = nanoLaunchEventBus,
                    initialDeepLinkPath = initialDeepLinkPath
                )
            }

            AnimatedVisibility(
                visible = currentMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(DevoraSpacing.md)
            ) {
                currentMessage?.let { message ->
                    Snackbar(
                        containerColor = when (message.severity) {
                            DevoraSnackbarSeverity.SUCCESS -> DevoraStatusColors.success
                            DevoraSnackbarSeverity.ERROR -> DevoraStatusColors.danger
                            DevoraSnackbarSeverity.INFO -> DevoraStatusColors.info
                        },
                        contentColor = Color.White
                    ) {
                        Text(message.text)
                    }
                }
            }
        }
    }
}