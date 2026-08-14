package dev.devora.feature.secrets.domain.model

/**
 * A secret tracked locally by Devora, scoped to a project + workflow
 * (spec section 16: secrets live in the workflow sidebar). The value
 * itself is never held in this model after creation — only metadata.
 * Actual values live only in SecureSecretStore (encrypted) and, if
 * pushed, inside GitHub's own encrypted secret storage.
 */
data class SecretEntry(
    val id: String,
    val projectRootPath: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val pushedToGitHub: Boolean
)