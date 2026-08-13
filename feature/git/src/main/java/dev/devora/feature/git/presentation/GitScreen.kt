package dev.devora.feature.git.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.feature.git.domain.model.GitFileChange

@Composable
fun GitScreen(
    projectRootPath: String,
    viewModel: GitScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var commitMessage by remember { mutableStateOf("") }

    LaunchedEffect(projectRootPath) {
        viewModel.load(projectRootPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Git") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            uiState.status?.let { status ->
                Text("Branch: ${status.branch ?: "detached"} (+${status.ahead}/-${status.behind})", style = MaterialTheme.typography.titleSmall)
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            LazyColumn(modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                items(uiState.status?.changes ?: emptyList()) { change ->
                    ChangeRow(
                        change = change,
                        onStage = { viewModel.stage(change.path) },
                        onUnstage = { viewModel.unstage(change.path) }
                    )
                }
            }

            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                label = { Text("Commit message") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            Row {
                Button(onClick = { viewModel.commit(commitMessage) }, enabled = !uiState.isBusy) {
                    Text("Commit")
                }
                Button(onClick = { viewModel.push("origin", uiState.status?.branch ?: "main") }, enabled = !uiState.isBusy) {
                    Text("Push")
                }
                Button(onClick = { viewModel.pull("origin", uiState.status?.branch ?: "main") }, enabled = !uiState.isBusy) {
                    Text("Pull")
                }
            }

            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(uiState.outputLines) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun ChangeRow(change: GitFileChange, onStage: () -> Unit, onUnstage: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("[${change.status}] ${change.path}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 8.dp))
        if (change.staged) {
            TextButton(onClick = onUnstage) { Text("Unstage") }
        } else {
            TextButton(onClick = onStage) { Text("Stage") }
        }
    }
}