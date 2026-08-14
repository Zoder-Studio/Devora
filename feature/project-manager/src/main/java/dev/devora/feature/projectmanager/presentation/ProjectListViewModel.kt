package dev.devora.feature.projectmanager.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.components.DevoraEmptyState
import dev.devora.core.ui.components.DevoraLoadingState
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.projectmanager.domain.model.Project

@Composable
fun ProjectListScreen(
    onProjectSelected: (Project) -> Unit,
    onImportRequested: () -> Unit,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Projects") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onImportRequested,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Import project") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> DevoraLoadingState(label = "Loading projects...")
                uiState.projects.isEmpty() -> DevoraEmptyState("No projects yet. Import an existing Android/Gradle project to begin.")
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DevoraSpacing.sm),
                    modifier = Modifier.padding(DevoraSpacing.md)
                ) {
                    items(uiState.projects, key = { it.id }) { project ->
                        ProjectListItem(project = project, onClick = { onProjectSelected(project) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectListItem(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(DevoraSpacing.md)) {
            Text(text = project.name, style = MaterialTheme.typography.titleMedium)
            Text(text = project.rootPath, style = MaterialTheme.typography.bodySmall)
            Text(
                text = if (project.hasGradleWrapper) "Gradle wrapper: present" else "Gradle wrapper: missing",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (project.hasDevoraMetadata) ".devora: initialized" else ".devora: not initialized",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}