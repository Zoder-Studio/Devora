package dev.devora.feature.pluginsystem.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.feature.pluginsystem.api.DevoraPlugin

@Composable
fun PluginListScreen(viewModel: PluginListViewModel = hiltViewModel()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Plugins") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(viewModel.plugins) { plugin -> PluginRow(plugin) }
            }
        }
    }
}

@Composable
private fun PluginRow(plugin: DevoraPlugin) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(plugin.displayName, style = MaterialTheme.typography.titleSmall)
            Text(plugin.description, style = MaterialTheme.typography.bodySmall)
            Text(plugin.id, style = MaterialTheme.typography.labelSmall)
        }
    }
}