package dev.devora.feature.terminal.presentation

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BootstrapVersionCheckCard(viewModel: BootstrapVersionCheckViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmUpdate by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Embedded engine version", style = MaterialTheme.typography.titleSmall)

            uiState.checkResult?.let { result ->
                Text("Pinned: ${result.pinnedReleaseTag}", style = MaterialTheme.typography.bodySmall)
                Text("Latest available: ${result.latestReleaseTag}", style = MaterialTheme.typography.bodySmall)
                if (result.isUpdateAvailable) {
                    if (showConfirmUpdate) {
                        Text(
                            "Updating will wipe the current embedded engine (nano, SDK tooling, " +
                                "anything installed inside it) and reinstall from scratch. Continue?",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = {
                            showConfirmUpdate = false
                            viewModel.applyUpdate(result.latestReleaseTag)
                        }) { Text("Confirm update") }
                        TextButton(onClick = { showConfirmUpdate = false }) { Text("Cancel") }
                    } else {
                        Button(
                            onClick = { showConfirmUpdate = true },
                            enabled = !uiState.isUpdating
                        ) { Text(if (uiState.isUpdating) "Updating..." else "Update to ${result.latestReleaseTag}") }
                    }
                } else {
                    Text("Already up to date.", style = MaterialTheme.typography.bodySmall)
                }
            }

            uiState.progressMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            TextButton(onClick = { viewModel.checkForUpdate() }, enabled = !uiState.isChecking) {
                Text(if (uiState.isChecking) "Checking..." else "Check for update")
            }
        }
    }
}