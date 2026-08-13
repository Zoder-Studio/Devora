package dev.devora.feature.signing.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import dev.devora.feature.signing.domain.model.KeystoreEntry

@Composable
fun SignApkScreen(
    apkFilePath: String,
    viewModel: SignApkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedKeystoreId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadKeystores()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign APK") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(apkFilePath, style = MaterialTheme.typography.bodySmall)

            Text("Select keystore", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
            if (uiState.keystores.isEmpty()) {
                Text("No keystores yet. Create one first.", style = MaterialTheme.typography.bodySmall)
            }
            LazyColumn {
                items(uiState.keystores, key = { it.id }) { keystore ->
                    KeystoreSelectRow(
                        keystore = keystore,
                        selected = keystore.id == selectedKeystoreId,
                        onSelect = { selectedKeystoreId = keystore.id }
                    )
                }
            }

            Button(
                onClick = { selectedKeystoreId?.let { viewModel.sign(apkFilePath, it) } },
                enabled = selectedKeystoreId != null && !uiState.isSigning,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(if (uiState.isSigning) "Signing..." else "Sign APK")
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            uiState.signedApkPath?.let {
                Text(
                    "Signed APK: $it",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(uiState.outputLines) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun KeystoreSelectRow(keystore: KeystoreEntry, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onSelect
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                keystore.alias,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(keystore.keystoreFilePath, style = MaterialTheme.typography.bodySmall)
        }
    }
}