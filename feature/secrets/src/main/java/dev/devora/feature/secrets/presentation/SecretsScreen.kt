package dev.devora.feature.secrets.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import dev.devora.core.ui.components.DevoraEmptyState
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.secrets.domain.model.SecretEntry

@Composable
fun SecretsScreen(
    projectRootPath: String,
    githubOwner: String,
    githubRepo: String,
    viewModel: SecretsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var secretName by remember { mutableStateOf("") }
    var secretValue by remember { mutableStateOf("") }

    LaunchedEffect(projectRootPath) {
        viewModel.load(projectRootPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Secrets") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            OutlinedTextField(secretName, { secretName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                secretValue, { secretValue = it }, label = { Text("Value") },
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.add(secretName, secretValue); secretName = ""; secretValue = "" },
                enabled = secretName.isNotBlank() && secretValue.isNotBlank(),
                modifier = Modifier.padding(top = DevoraSpacing.sm)
            ) {
                Text("Add Secret")
            }

            if (uiState.secrets.isEmpty()) {
                DevoraEmptyState("No secrets added yet.")
            } else {
                LazyColumn(modifier = Modifier.padding(top = DevoraSpacing.md)) {
                    items(uiState.secrets, key = { it.id }) { secret ->
                        SecretRow(
                            secret = secret,
                            isBusy = uiState.isBusy,
                            onPush = { viewModel.pushToGitHub(secret.id, githubOwner, githubRepo) },
                            onDelete = { viewModel.delete(secret.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecretRow(secret: SecretEntry, isBusy: Boolean, onPush: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp())) {
        Column(modifier = Modifier.padding(12.dp())) {
            Text(secret.name, style = MaterialTheme.typography.titleSmall)
            Text(
                if (secret.pushedToGitHub) "Pushed to GitHub" else "Not pushed",
                style = MaterialTheme.typography.bodySmall,
                color = if (secret.pushedToGitHub) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row {
                TextButton(onClick = onPush, enabled = !isBusy) { Text("Push to GitHub") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())