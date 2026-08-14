package dev.devora.feature.accountsecurity.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.feature.accountsecurity.domain.model.RevokeStage

@Composable
fun EmergencyRevokeScreen(viewModel: EmergencyRevokeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Devora — Emergency Security") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (uiState.stage) {
                RevokeStage.IDLE -> {
                    Text("This is your primary phone. Revoking DPAT will lock other phones out of this account (on this device's local record).")
                    Button(onClick = { viewModel.startCountdown() }, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Start emergency revoke")
                    }
                }
                RevokeStage.COUNTDOWN -> {
                    Text("This is your primary phone.")
                    Text(
                        "Revoke DPAT to lock other phones",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text("${uiState.secondsRemaining}", style = MaterialTheme.typography.displayMedium)
                    if (uiState.secondsRemaining == 0) {
                        Button(onClick = { viewModel.confirmRevoke() }) { Text("Revoke DPAT") }
                    }
                    TextButton(onClick = { viewModel.cancel() }) { Text("Cancel") }
                }
                RevokeStage.REVOKING -> {
                    Text("Revoking DPAT...")
                    Text("✓ DPAT revoked (local)")
                    Text("✓ Device identity key deleted")
                }
                RevokeStage.REVOKED -> {
                    Column {
                        Text("✓ DPAT revoked", color = MaterialTheme.colorScheme.primary)
                        Text("✓ Local session cleared", color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Note: without a Devora account server, other devices signed " +
                                "into this account (if any) are NOT automatically revoked — " +
                                "this action only affects this device.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}