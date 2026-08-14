package dev.devora.feature.workflowengine.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import dev.devora.core.ui.components.DevoraLoadingState
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.workflowengine.domain.model.WorkflowStepResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStepStatus

@Composable
fun WorkflowRunScreen(
    projectRootPath: String,
    workflowFilePath: String,
    jobId: String,
    viewModel: WorkflowRunViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workflowFilePath, jobId) {
        viewModel.run(projectRootPath, workflowFilePath, jobId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Workflow: $jobId") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            if (uiState.isRunning) {
                DevoraLoadingState(label = "Running $jobId...")
            }
            LazyColumn {
                items(uiState.runResult?.stepResults ?: emptyList()) { step ->
                    WorkflowStepRow(step)
                }
            }
        }
    }
}

@Composable
private fun WorkflowStepRow(step: WorkflowStepResult) {
    val color = when (step.status) {
        WorkflowStepStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        WorkflowStepStatus.FAILED -> MaterialTheme.colorScheme.error
        WorkflowStepStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(modifier = Modifier.padding(vertical = 6.dp())) {
        Text("[${step.status}] ${step.stepName}", color = color, style = MaterialTheme.typography.bodyMedium)
        step.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())