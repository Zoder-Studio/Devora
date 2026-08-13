package dev.devora.feature.signing.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.model.SigningRequest

interface SigningRepository {
    suspend fun listKeystores(): List<KeystoreEntry>

    suspend fun createKeystore(
        request: KeystoreCreationRequest,
        destinationDir: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<KeystoreEntry>

    suspend fun signApk(request: SigningRequest, onOutputLine: (String) -> Unit): DevoraResult<Unit>
}