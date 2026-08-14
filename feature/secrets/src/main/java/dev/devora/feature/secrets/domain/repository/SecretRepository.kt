package dev.devora.feature.secrets.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.secrets.domain.model.SecretEntry

interface SecretRepository {
    suspend fun listForProject(projectRootPath: String): List<SecretEntry>

    suspend fun addSecret(projectRootPath: String, name: String, value: String): DevoraResult<SecretEntry>

    suspend fun deleteSecret(secretId: String): DevoraResult<Unit>

    /** Pushes to GitHub via the real Secrets API (sealed box encryption) — the value never leaves this call path in plaintext except in-memory during the request. */
    suspend fun pushToGitHub(secretId: String, owner: String, repo: String): DevoraResult<Unit>
}