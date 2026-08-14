package dev.devora.feature.workflowengine.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.theme.DevoraSpacing

@Composable
fun EnvironmentManagerScreen(
    workflowId: String,
    viewModel: EnvironmentManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workflowId) {
        viewModel.load(workflowId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Environment — $workflowId") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            uiState.info?.let { info ->
                if (!info.isIsolated) {
                    Text(
                        "This device is using the Termux app engine, which cannot provide per-workflow isolation.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text("Status: ${info.status}")
                    Text("Bootstrap version: ${info.installedBootstrapVersion ?: "not installed"}")
                    Text("Storage: %.1f MB".format(info.storageSizeBytes / (1024.0 * 1024.0)))

                    Row(modifier = Modifier.padding(top = DevoraSpacing.md)) {
                        Button(
                            onClick = { viewModel.reset(workflowId) },
                            enabled = !uiState.isBusy
                        ) { Text("Reset Environment") }
                        Button(
                            onClick = { viewModel.delete(workflowId) },
                            enabled = !uiState.isBusy
                        ) { Text("Delete Environment") }
                    }
                }
            }
        }
    }
}