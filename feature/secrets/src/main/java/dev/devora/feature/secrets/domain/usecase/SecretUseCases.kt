package dev.devora.feature.secrets.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.secrets.domain.model.SecretEntry
import dev.devora.feature.secrets.domain.repository.SecretRepository

class ListSecretsUseCase(private val repository: SecretRepository) {
    suspend operator fun invoke(projectRootPath: String): List<SecretEntry> = repository.listForProject(projectRootPath)
}

class AddSecretUseCase(private val repository: SecretRepository) {
    suspend operator fun invoke(projectRootPath: String, name: String, value: String): DevoraResult<SecretEntry> =
        repository.addSecret(projectRootPath, name, value)
}

class DeleteSecretUseCase(private val repository: SecretRepository) {
    suspend operator fun invoke(secretId: String): DevoraResult<Unit> = repository.deleteSecret(secretId)
}

class PushSecretToGitHubUseCase(private val repository: SecretRepository) {
    suspend operator fun invoke(secretId: String, owner: String, repo: String): DevoraResult<Unit> =
        repository.pushToGitHub(secretId, owner, repo)
}