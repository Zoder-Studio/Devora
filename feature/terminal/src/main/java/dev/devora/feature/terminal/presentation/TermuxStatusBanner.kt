package dev.devora.feature.terminal.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.devora.feature.terminal.domain.model.TermuxIntegrationState

@Composable
fun TermuxStatusBanner(state: TermuxIntegrationState) {
    val (title, description) = when (state) {
        TermuxIntegrationState.Checking -> "Checking Termux..." to ""
        TermuxIntegrationState.TermuxNotInstalled ->
            "Termux is not installed" to
                "Install Termux from F-Droid or GitHub releases (not Play Store)."
        TermuxIntegrationState.RunCommandPermissionMissing ->
            "RUN_COMMAND permission missing" to
                "Grant Devora the \"Run commands in Termux\" permission in Android Settings."
        TermuxIntegrationState.ExternalAppsUnverified ->
            "External apps setting unverified" to
                "Add allow-external-apps=true to ~/.termux/termux.properties in Termux, then restart Termux."
        TermuxIntegrationState.Ready -> "Termux ready" to ""
    }

    if (title.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (description.isNotEmpty()) {
                    Text(description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}