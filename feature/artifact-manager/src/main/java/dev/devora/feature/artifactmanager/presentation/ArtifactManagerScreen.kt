package dev.devora.feature.artifactmanager.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.devora.core.ui.components.DevoraEmptyState
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.artifactmanager.domain.model.Artifact

@Composable
fun ArtifactManagerScreen(
    projectRootPath: String,
    viewModel: ArtifactManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectRootPath) {
        viewModel.load(projectRootPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DEVORA — Artifacts") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.artifacts.isEmpty()) {
                DevoraEmptyState("No artifacts found. Run a build first.")
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = DevoraSpacing.md)) {
                    items(uiState.artifacts, key = { it.id }) { artifact ->
                        ArtifactRow(
                            artifact = artifact,
                            isInstalling = uiState.installingArtifactId == artifact.id,
                            onInstall = { viewModel.install(artifact) },
                            onDelete = { viewModel.delete(artifact, projectRootPath) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtifactRow(
    artifact: Artifact,
    isInstalling: Boolean,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(artifact.fileName, style = MaterialTheme.typography.titleSmall)
            Text(
                "${artifact.type} · %.1f MB · ${artifact.source}".format(artifact.sizeBytes / (1024.0 * 1024.0)),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "SHA-256: ${artifact.sha256.take(16)}...",
                style = MaterialTheme.typography.labelSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(DevoraSpacing.sm), modifier = Modifier.padding(top = 4.dp)) {
                if (isInstalling) {
                    InstallingIndicator()
                    Text("Installing...", style = MaterialTheme.typography.bodySmall)
                } else if (artifact.type.name == "APK") {
                    TextButton(onClick = onInstall) { Text("Install") }
                }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun InstallingIndicator() {
    val transition = rememberInfiniteTransition(label = "installing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    CircularProgressIndicator(modifier = Modifier.rotate(rotation).padding(2.dp))
}