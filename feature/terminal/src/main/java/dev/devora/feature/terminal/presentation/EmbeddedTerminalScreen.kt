package dev.devora.feature.terminal.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView

@Composable
fun EmbeddedTerminalScreen(
    workingDirectory: String,
    initialCommand: String? = null,
    viewModel: EmbeddedTerminalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionClient = remember(workingDirectory, initialCommand) { NoopTerminalSessionClient() }

    LaunchedEffect(workingDirectory, initialCommand) {
        viewModel.start(workingDirectory, sessionClient, initialCommand)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Terminal — $workingDirectory") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is EmbeddedTerminalUiState.Preparing -> CircularProgressIndicator()
                is EmbeddedTerminalUiState.PreparingProgress -> Text(state.message)
                is EmbeddedTerminalUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
                is EmbeddedTerminalUiState.Ready -> AndroidView(
                    factory = { context ->
                        TerminalView(context, null).apply {
                            attachSession(state.session)
                            setTerminalViewClient(sessionClient)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}