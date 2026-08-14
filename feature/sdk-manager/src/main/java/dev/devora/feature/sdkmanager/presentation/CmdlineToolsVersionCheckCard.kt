package dev.devora.feature.sdkmanager.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CmdlineToolsVersionCheckCard(viewModel: CmdlineToolsVersionCheckViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Cmdline-tools version", style = MaterialTheme.typography.titleSmall)

            uiState.checkResult?.let { result ->
                Text("Pinned: ${result.pinnedRevision}", style = MaterialTheme.typography.bodySmall)
                Text("Latest available: ${result.latestRevision}", style = MaterialTheme.typography.bodySmall)
                if (result.isUpdateAvailable) {
                    Button(
                        onClick = { viewModel.applyPin(result) },
                        enabled = !uiState.isApplying
                    ) { Text(if (uiState.isApplying) "Pinning..." else "Pin ${result.latestRevision}") }
                } else {
                    Text("Already up to date.", style = MaterialTheme.typography.bodySmall)
                }
            }

            uiState.appliedMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

            TextButton(onClick = { viewModel.checkForUpdate() }, enabled = !uiState.isChecking) {
                Text(if (uiState.isChecking) "Checking..." else "Check for update")
            }
        }
    }
}