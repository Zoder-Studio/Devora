package dev.devora.feature.signing.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.model.SigningRequest
import dev.devora.feature.signing.domain.repository.SigningRepository

class ListKeystoresUseCase(private val repository: SigningRepository) {
    suspend operator fun invoke(): List<KeystoreEntry> = repository.listKeystores()
}

class CreateKeystoreUseCase(private val repository: SigningRepository) {
    suspend operator fun invoke(
        request: KeystoreCreationRequest,
        destinationDir: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<KeystoreEntry> = repository.createKeystore(request, destinationDir, onOutputLine)
}

class SignApkUseCase(private val repository: SigningRepository) {
    suspend operator fun invoke(request: SigningRequest, onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        repository.signApk(request, onOutputLine)
}