package dev.devora.feature.buildsystem.presentation

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
fun BuildScreen(
    projectRootPath: String,
    viewModel: BuildScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var taskInput by remember { mutableStateOf("assembleDebug") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Build") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                label = { Text("Gradle task") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.runTask(projectRootPath, taskInput) },
                enabled = !uiState.isRunning,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (uiState.isRunning) "Running..." else "Run")
            }

            if (uiState.isRunning) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            uiState.currentRun?.let { run ->
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Step: ${run.gradleTask}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row {
                        Text(
                            text = if (run.stderrAvailable) {
                                "stdout: ${run.stdoutLineCount} lines · stderr: ${run.stderrLineCount} lines"
                            } else {
                                "output: ${run.stdoutLineCount} lines (combined — embedded engine merges stdout/stderr)"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Exit code: ${run.exitCode} — ${run.status}",
                        color = if (run.status.name == "SUCCESS") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (uiState.logTail.size < run.stdoutLineCount) {
                        Text(
                            "Showing last ${uiState.logTail.size} lines",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(uiState.logTail) { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}