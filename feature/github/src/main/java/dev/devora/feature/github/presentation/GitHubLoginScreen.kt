package dev.devora.feature.github.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GitHubLoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: GitHubLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startLogin()
    }

    LaunchedEffect(uiState) {
        if (uiState is GitHubLoginUiState.Success) onLoggedIn()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign in to GitHub") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (val state = uiState) {
                is GitHubLoginUiState.Idle, is GitHubLoginUiState.RequestingCode -> CircularProgressIndicator()
                is GitHubLoginUiState.WaitingForUser -> {
                    Text("Go to ${state.info.verificationUri} and enter this code:", style = MaterialTheme.typography.bodyMedium)
                    Text(state.info.userCode, style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.info.verificationUri))
                        context.startActivity(intent)
                    }) {
                        Text("Open in browser")
                    }
                    Text("Waiting for authorization...", style = MaterialTheme.typography.bodySmall)
                }
                is GitHubLoginUiState.Success -> Text("Signed in!")
                is GitHubLoginUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}