package dev.devora.feature.apkinspector.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.components.DevoraLoadingState
import dev.devora.core.ui.theme.DevoraSpacing

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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            if (uiState.isLoading) DevoraLoadingState(label = "Running bundletool...")
            LazyColumn {
                items(uiState.lines) { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }
}