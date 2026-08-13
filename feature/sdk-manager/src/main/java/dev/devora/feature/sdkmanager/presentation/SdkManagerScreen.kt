package dev.devora.feature.sdkmanager.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.feature.sdkmanager.domain.model.SdkComponent

@Composable
fun SdkManagerScreen(viewModel: SdkManagerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setupAndRefresh()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — SDK Manager") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isBusy) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            Text("Installed", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(uiState.installed, key = { it.packagePath }) { component ->
                    SdkComponentRow(
                        component = component,
                        actionLabel = "Uninstall",
                        onAction = { viewModel.uninstall(component.packagePath) }
                    )
                }
            }

            Text("Available", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(uiState.available, key = { it.packagePath }) { component ->
                    SdkComponentRow(
                        component = component,
                        actionLabel = "Install",
                        onAction = { viewModel.install(component.packagePath) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SdkComponentRow(component: SdkComponent, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(component.packagePath, style = MaterialTheme.typography.bodyMedium)
            Text(component.displayName, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}