package dev.devora.feature.secrets.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.github.data.GitHubTokenStore
import dev.devora.feature.secrets.data.GitHubSecretsApi
import dev.devora.feature.secrets.data.SecretRegistryStore
import dev.devora.feature.secrets.data.SecureSecretStore
import dev.devora.feature.secrets.domain.model.SecretEntry
import dev.devora.feature.secrets.domain.repository.SecretRepository
import java.util.UUID

class DefaultSecretRepository(
    private val registryStore: SecretRegistryStore,
    private val valueStore: SecureSecretStore,
    private val gitHubSecretsApi: GitHubSecretsApi,
    private val tokenStore: GitHubTokenStore
) : SecretRepository {

    override suspend fun listForProject(projectRootPath: String): List<SecretEntry> =
        registryStore.readAll().filter { it.projectRootPath == projectRootPath }

    override suspend fun addSecret(projectRootPath: String, name: String, value: String): DevoraResult<SecretEntry> {
        val id = UUID.randomUUID().toString()

        val saveResult = valueStore.saveValue(id, value)
        if (saveResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = saveResult.message, cause = saveResult.cause)
        }

        val entry = SecretEntry(
            id = id,
            projectRootPath = projectRootPath,
            name = name,
            createdAtEpochMillis = System.currentTimeMillis(),
            pushedToGitHub = false
        )

        val registryResult = registryStore.add(entry)
        if (registryResult is DevoraResult.Failure) {
            valueStore.deleteValue(id)
            return DevoraResult.Failure(message = registryResult.message, cause = registryResult.cause)
        }

        return DevoraResult.Success(entry)
    }

    override suspend fun deleteSecret(secretId: String): DevoraResult<Unit> {
        valueStore.deleteValue(secretId)
        return registryStore.remove(secretId)
    }

    override suspend fun pushToGitHub(secretId: String, owner: String, repo: String): DevoraResult<Unit> {
        val entry = registryStore.readAll().find { it.id == secretId }
            ?: return DevoraResult.Failure(message = "Secret not found: $secretId")

        val value = valueStore.readValue(secretId)
            ?: return DevoraResult.Failure(message = "Secret value not found locally: $secretId")

        val token = tokenStore.readToken()
            ?: return DevoraResult.Failure(message = "Not logged in to GitHub")

        val result = gitHubSecretsApi.putSecret(token, owner, repo, entry.name, value)
        if (result is DevoraResult.Success) {
            registryStore.markPushed(secretId)
        }
        return result
    }
}