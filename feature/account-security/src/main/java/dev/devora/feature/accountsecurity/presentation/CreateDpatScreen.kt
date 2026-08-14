package dev.devora.feature.accountsecurity.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.accountsecurity.domain.model.DpatExpirationOption

@Composable
fun CreateDpatScreen(viewModel: CreateDpatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedExpiration by remember { mutableStateOf(DpatExpirationOption.THIRTY_DAYS) }
    var phoneLabel by remember { mutableStateOf("") }
    var makePrimary by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA PAT") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            if (!uiState.crossDeviceEnforcementAvailable) {
                Text(
                    "No Devora account server is configured on this build. This device's DPAT is local-only.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("Expiration", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = DevoraSpacing.md))
            DpatExpirationOption.entries.forEach { option ->
                TextButton(onClick = { selectedExpiration = option }) {
                    val label = when (option) {
                        DpatExpirationOption.TWO_DAYS -> "2 days"
                        DpatExpirationOption.SEVEN_DAYS -> "7 days"
                        DpatExpirationOption.THIRTY_DAYS -> "30 days"
                        DpatExpirationOption.NINETY_DAYS -> "90 days"
                        DpatExpirationOption.ONE_YEAR -> "1 year"
                        DpatExpirationOption.NEVER -> "Never"
                    }
                    Text(if (option == selectedExpiration) "● $label" else "○ $label")
                }
            }

            OutlinedTextField(
                value = phoneLabel,
                onValueChange = { phoneLabel = it },
                label = { Text("Your phone name or number (optional)") },
                modifier = Modifier.fillMaxWidth().padding(top = DevoraSpacing.md)
            )

            Row(modifier = Modifier.padding(top = DevoraSpacing.sm)) {
                Text("Make this phone your primary phone for secure account recovery")
                Switch(checked = makePrimary, onCheckedChange = { makePrimary = it })
            }

            Button(
                onClick = { viewModel.create(selectedExpiration, phoneLabel.ifBlank { null }, makePrimary) },
                enabled = !uiState.isCreating,
                modifier = Modifier.padding(top = DevoraSpacing.md)
            ) {
                Text(if (uiState.isCreating) "Creating..." else "Create DPAT")
            }

            if (uiState.createdDpat != null) {
                Text("DPAT created.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = DevoraSpacing.sm))
            }
        }
    }
}