package dev.devora.feature.github.presentation

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
import androidx.compose.material3.Switch
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
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.theme.DevoraSpacing

@Composable
fun PushToGitHubScreen(
    projectRootPath: String,
    viewModel: PushToGitHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var repoName by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf<String?>(null) }
    var isPrivate by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadOrgs()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Push to GitHub") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            OutlinedTextField(repoName, { repoName = it }, label = { Text("Repository name") }, modifier = Modifier.fillMaxWidth())

            Text("Owner", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = DevoraSpacing.md))
            TextButton(onClick = { selectedOwner = null }) {
                Text(if (selectedOwner == null) "● Personal account" else "○ Personal account")
            }
            uiState.orgs.forEach { org ->
                TextButton(onClick = { selectedOwner = org.login }) {
                    Text(if (selectedOwner == org.login) "● ${org.login}" else "○ ${org.login}")
                }
            }

            Row {
                Text("Private repository", modifier = Modifier.padding(top = DevoraSpacing.md))
                Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
            }

            Button(
                onClick = { viewModel.createAndPush(projectRootPath, selectedOwner, repoName, isPrivate) },
                enabled = repoName.isNotBlank() && !uiState.isBusy,
                modifier = Modifier.padding(top = DevoraSpacing.md)
            ) {
                Text(if (uiState.isBusy) "Pushing..." else "Create & Push")
            }

            if (uiState.success) {
                Text("Pushed successfully!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = DevoraSpacing.sm))
            }

            LazyColumn(modifier = Modifier.padding(top = DevoraSpacing.md)) {
                items(uiState.outputLines) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}