package dev.devora.feature.sdkmanager.domain.model

data class CmdlineToolsVersionCheckResult(
    val pinnedRevision: String,
    val latestRevision: String,
    val latestDownloadUrl: String,
    val isUpdateAvailable: Boolean
)