package dev.devora.feature.signing.presentation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest

@Composable
fun CreateKeystoreScreen(
    destinationDir: String,
    onCreated: () -> Unit,
    viewModel: CreateKeystoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var keystoreName by remember { mutableStateOf("release") }
    var alias by remember { mutableStateOf("release-key") }
    var keystorePassword by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }
    var validityYears by remember { mutableStateOf("25") }
    var commonName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createdEntry) {
        if (uiState.createdEntry != null) onCreated()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Keystore") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            OutlinedTextField(keystoreName, { keystoreName = it }, label = { Text("Keystore name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(alias, { alias = it }, label = { Text("Alias") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                keystorePassword, { keystorePassword = it }, label = { Text("Keystore password") },
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                keyPassword, { keyPassword = it }, label = { Text("Key password") },
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(validityYears, { validityYears = it }, label = { Text("Validity (years)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(commonName, { commonName = it }, label = { Text("Common name (CN)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    viewModel.create(
                        KeystoreCreationRequest(
                            keystoreName = keystoreName,
                            alias = alias,
                            keystorePassword = keystorePassword,
                            keyPassword = keyPassword,
                            validityYears = validityYears.toIntOrNull() ?: 25,
                            commonName = commonName,
                            organizationalUnit = null,
                            organization = null,
                            locality = null,
                            state = null,
                            countryCode = null
                        ),
                        destinationDir = destinationDir
                    )
                },
                enabled = !uiState.isCreating,
                modifier = Modifier.padding(top = DevoraSpacing.md)
            ) {
                Text(if (uiState.isCreating) "Creating..." else "Create Keystore")
            }

            LazyColumn(modifier = Modifier.padding(top = DevoraSpacing.md)) {
                items(uiState.outputLines) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}