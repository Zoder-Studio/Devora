package dev.devora.feature.terminal.domain.model

data class BootstrapVersionCheckResult(
    val pinnedReleaseTag: String,
    val latestReleaseTag: String,
    val isUpdateAvailable: Boolean
)