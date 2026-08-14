package dev.devora.feature.apkinspector.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import dev.devora.core.ui.components.DevoraLoadingState
import dev.devora.core.ui.theme.DevoraSpacing
import dev.devora.feature.apkinspector.domain.model.ApkInspectionResult

@Composable
fun ApkInspectorScreen(
    apkFilePath: String,
    viewModel: ApkInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(apkFilePath) {
        viewModel.inspect(apkFilePath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("APK Inspector") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(DevoraSpacing.md)) {
            if (uiState.isLoading) DevoraLoadingState(label = "Inspecting APK...")
            uiState.result?.let { result -> ApkInspectionContent(result) }
        }
    }
}

@Composable
private fun ApkInspectionContent(result: ApkInspectionResult) {
    LazyColumn {
        item { SectionTitle("General") }
        item { InfoLine("File name", result.general.fileName) }
        item { InfoLine("Size", "%.2f MB".format(result.general.fileSizeBytes / (1024.0 * 1024.0))) }
        item { InfoLine("SHA-256", result.general.sha256) }

        item { SectionTitle("Application") }
        item { InfoLine("Package", result.application.packageName) }
        item { InfoLine("Label", result.application.appLabel) }
        item { InfoLine("Version", "${result.application.versionName} (${result.application.versionCode})") }

        item { SectionTitle("Android") }
        item { InfoLine("Min SDK", result.android.minSdk.toString()) }
        item { InfoLine("Target SDK", result.android.targetSdk.toString()) }
        item { InfoLine("Supported ABIs", result.android.supportedAbis.joinToString(", ").ifEmpty { "none (java-only)" }) }

        item { SectionTitle("Signing") }
        item { InfoLine("Signed", result.signing.isSigned.toString()) }
        item { InfoLine("Schemes", result.signing.schemesUsed.joinToString(", ").ifEmpty { "none" }) }
        result.signing.certificates.forEach { cert ->
            item { InfoLine("Certificate", cert.sha256Fingerprint) }
        }

        item { SectionTitle("Components") }
        item { InfoLine("Activities", result.components.activities.size.toString()) }
        item { InfoLine("Services", result.components.services.size.toString()) }
        item { InfoLine("Receivers", result.components.receivers.size.toString()) }
        item { InfoLine("Providers", result.components.providers.size.toString()) }

        item { SectionTitle("Permissions") }
        result.permissions.requested.forEach { permission ->
            item { InfoLine("Requested", permission) }
        }

        item { SectionTitle("Files") }
        item { InfoLine("classes.dex count", result.files.classesDexCount.toString()) }
        item { InfoLine("resources.arsc", result.files.hasResourcesArsc.toString()) }
        item { InfoLine("lib/", result.files.libDirectories.joinToString(", ").ifEmpty { "none" }) }
        item { InfoLine("assets/", result.files.hasAssets.toString()) }
        item { InfoLine("res/", result.files.hasRes.toString()) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = DevoraSpacing.md, bottom = 4.dp()))
}

@Composable
private fun InfoLine(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodySmall)
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())