package dev.devora.feature.apkinspector.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AabInspectorScreen(
    aabFilePath: String,
    viewModel: AabInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(aabFilePath) {
        viewModel.inspect(aabFilePath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AAB Inspector (bundletool dump manifest)") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            LazyColumn {
                items(uiState.lines) { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}