package dev.devora.feature.github.data

/**
 * GitHub's official OAuth Device Flow endpoints. No client_secret is
 * used anywhere in this module — Device Flow's token-polling step
 * for public clients does not require one, which is exactly why it
 * is the correct choice for a client-only app like Devora that has
 * no backend server to keep a secret safe.
 *
 * REQUIRED SETUP: register an OAuth App at https://github.com/settings/developers,
 * enable "Device Flow" in its settings, and put its Client ID below.
 * This ID is public by design (it identifies the app, not a secret).
 */
object GitHubOAuthContract {
    const val CLIENT_ID = "REPLACE_WITH_YOUR_GITHUB_OAUTH_APP_CLIENT_ID"

    const val DEVICE_CODE_URL = "https://github.com/login/device/code"
    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    const val VERIFICATION_URI = "https://github.com/login/device"

    const val SCOPES = "repo workflow"

    const val GRANT_TYPE_DEVICE_CODE = "urn:ietf:params:oauth:grant-type:device_code"
}