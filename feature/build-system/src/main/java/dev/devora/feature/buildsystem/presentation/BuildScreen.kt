package dev.devora.feature.buildsystem.presentation

import androidx.compose.foundation.layout.Column
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
import dev.devora.core.ui.components.DevoraLoadingState
import dev.devora.core.ui.theme.DevoraMonospaceStyle
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.core.ui.theme.NerdFontIcons

@Composable
fun BuildScreen(
    projectRootPath: String,
    viewModel: BuildScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var taskInput by remember { mutableStateOf("assembleDebug") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("${NerdFontIcons.GEAR}  Build") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            OutlinedTextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                label = { Text("Gradle task") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.runTask(projectRootPath, taskInput) },
                enabled = !uiState.isRunning,
                modifier = Modifier.padding(top = DevoraSpacing.sm)
            ) {
                Text(if (uiState.isRunning) "Running..." else "Run")
            }

            if (uiState.isRunning) {
                DevoraLoadingState(label = "Running $taskInput...")
            }

            uiState.currentRun?.let { run ->
                val icon = if (run.status.name == "SUCCESS") NerdFontIcons.CHECK else NerdFontIcons.CROSS
                Text(
                    "$icon  ${run.status} — exit code ${run.exitCode}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = DevoraSpacing.md)
                )
            }

            LazyColumn(modifier = Modifier.padding(top = DevoraSpacing.sm)) {
                items(uiState.logTail) { line ->
                    Text(text = line, style = DevoraMonospaceStyle)
                }
            }
        }
    }
}