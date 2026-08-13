package dev.devora.feature.gradlemanager.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GradleManagerScreen(
    projectRootPath: String,
    viewModel: GradleManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectRootPath) {
        viewModel.load(projectRootPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Gradle Manager") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Wrapper", style = MaterialTheme.typography.titleMedium)
            uiState.wrapperInfo?.let { info ->
                Text("Version: ${info.distributionVersion ?: "not found"}")
                Text("gradlew present: ${info.gradlewExists}, executable: ${info.gradlewIsExecutable}")
            }

            Text("Cache", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            uiState.cacheInfo?.let { cache ->
                val sizeMb = cache.sizeBytes / (1024.0 * 1024.0)
                Text("Size: %.1f MB".format(sizeMb))
                Button(onClick = { viewModel.clearCache() }, enabled = !uiState.isBusy) {
                    Text("Clear Cache")
                }
            }

            Text("Daemon", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Row {
                Button(onClick = { viewModel.checkDaemonStatus() }, enabled = !uiState.isBusy) {
                    Text("Check status")
                }
                Button(onClick = { viewModel.stopDaemon() }, enabled = !uiState.isBusy) {
                    Text("Stop")
                }
            }

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Text("Offline mode", modifier = Modifier.padding(top = 12.dp))
                Switch(checked = uiState.isOfflineMode, onCheckedChange = { viewModel.toggleOfflineMode() })
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                items(uiState.statusLines) { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}