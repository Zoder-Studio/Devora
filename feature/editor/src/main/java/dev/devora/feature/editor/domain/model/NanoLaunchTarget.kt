package dev.devora.feature.editor.domain.model

sealed class NanoLaunchTarget {
    data class TermuxAppWindow(val filePath: String) : NanoLaunchTarget()
    data class EmbeddedSession(val filePath: String) : NanoLaunchTarget()
    data class EmbeddedNeedsInstall(val filePath: String) : NanoLaunchTarget()
    data class Unavailable(val reason: String) : NanoLaunchTarget()
}