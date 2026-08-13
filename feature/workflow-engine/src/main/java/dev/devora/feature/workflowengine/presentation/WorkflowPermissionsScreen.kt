package dev.devora.feature.workflowengine.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowPermissionEntry

@Composable
fun WorkflowPermissionsScreen(
    projectRootPath: String,
    availableWorkflowIds: List<String>,
    viewModel: WorkflowPermissionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectRootPath) {
        viewModel.load(projectRootPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Workflow Permissions") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            LazyColumn {
                items(availableWorkflowIds) { workflowId ->
                    val currentEntry = uiState.entries.find { it.workflowId == workflowId }
                    WorkflowPermissionRow(
                        workflowId = workflowId,
                        current = currentEntry?.permission,
                        onSelect = { permission ->
                            viewModel.setPermission(projectRootPath, workflowId, permission)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkflowPermissionRow(
    workflowId: String,
    current: WorkflowPermission?,
    onSelect: (WorkflowPermission) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(workflowId, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Current: ${current?.name ?: "Not granted"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (current == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Row {
                TextButton(onClick = { onSelect(WorkflowPermission.READ) }) { Text("Read") }
                TextButton(onClick = { onSelect(WorkflowPermission.WRITE) }) { Text("Write") }
                TextButton(onClick = { onSelect(WorkflowPermission.READ_WRITE) }) { Text("Read & Write") }
            }
        }
    }
}