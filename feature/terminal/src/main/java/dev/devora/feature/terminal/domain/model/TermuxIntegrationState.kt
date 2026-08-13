package dev.devora.feature.terminal.domain.model

/**
 * Real integration states with Termux. "ExternalAppsUnverified" replaces
 * a naive file-read check: Devora cannot read Termux's private
 * termux.properties from a different app UID, so it does not pretend to
 * know that setting's value. The real proof is a successful RUN_COMMAND
 * dispatch, which the UI walks the developer through explicitly.
 */
sealed class TermuxIntegrationState {
    data object Checking : TermuxIntegrationState()
    data object TermuxNotInstalled : TermuxIntegrationState()
    data object RunCommandPermissionMissing : TermuxIntegrationState()
    data object ExternalAppsUnverified : TermuxIntegrationState()
    data object Ready : TermuxIntegrationState()
}

data class TerminalCommandRequest(
    val workingDirectory: String,
    val command: String,
    val arguments: List<String> = emptyList(),
    val sessionAction: SessionAction = SessionAction.OPEN_NEW_WINDOW
)

enum class SessionAction {
    RUN_IN_BACKGROUND,
    OPEN_NEW_WINDOW
}