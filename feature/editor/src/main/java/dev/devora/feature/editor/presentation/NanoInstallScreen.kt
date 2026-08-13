package dev.devora.feature.editor.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NanoInstallScreen(
    filePath: String,
    onInstalled: (String) -> Unit,
    viewModel: NanoInstallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Install nano") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("nano is not installed in Devora's embedded engine yet.")
            Text(
                "This runs: apt update && apt install -y nano",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { viewModel.install(filePath, onInstalled) },
                enabled = !uiState.isInstalling
            ) {
                Text(if (uiState.isInstalling) "Installing..." else "Install now")
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(uiState.outputLines) { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}