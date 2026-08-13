package dev.devora.feature.filemanager.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import dev.devora.feature.filemanager.domain.model.FileNode
import dev.devora.feature.filemanager.domain.model.FileNodeType

@Composable
fun FileManagerScreen(
    rootPath: String,
    viewModel: FileManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(rootPath) {
        viewModel.open(rootPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.currentPath.ifEmpty { "Files" }) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Show hidden files", modifier = Modifier.padding(top = 12.dp))
                Switch(
                    checked = uiState.showHiddenFiles,
                    onCheckedChange = { viewModel.toggleShowHiddenFiles() }
                )
                TextButton(onClick = { viewModel.openTerminalHere() }) {
                    Text("Open terminal here")
                }
                uiState.parentPath?.let { parent ->
                    TextButton(onClick = { viewModel.open(parent) }) {
                        Text("Up")
                    }
                }
            }

            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                else -> LazyColumn {
                    items(uiState.visibleEntries, key = { it.absolutePath }) { entry ->
                        FileNodeRow(entry = entry, onClick = { viewModel.onEntryClicked(entry) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FileNodeRow(entry: FileNode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val typeLabel = when (entry.type) {
            FileNodeType.DIRECTORY -> "[dir]"
            FileNodeType.SYMLINK -> "[link]"
            FileNodeType.FILE -> "[file]"
        }
        Text(text = "$typeLabel ${entry.name}", modifier = Modifier.padding(end = 8.dp))
        Text(
            text = entry.permissions.toRwxString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}