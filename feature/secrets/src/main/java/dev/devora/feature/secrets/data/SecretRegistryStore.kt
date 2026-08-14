package dev.devora.feature.secrets.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.secrets.domain.model.SecretEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class StoredSecretEntry(
    val id: String,
    val projectRootPath: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val pushedToGitHub: Boolean
)

@Serializable
private data class SecretRegistry(val entries: List<StoredSecretEntry> = emptyList())

/**
 * Metadata-only registry (name, timestamps, push status) — this file
 * is never encrypted because it never holds a secret value; values
 * live exclusively in SecureSecretStore.
 */
class SecretRegistryStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun readAll(): List<SecretEntry> = mutex.withLock {
        if (!storeFile.exists()) return emptyList()
        return try {
            json.decodeFromString(SecretRegistry.serializer(), storeFile.readText()).entries.map {
                SecretEntry(it.id, it.projectRootPath, it.name, it.createdAtEpochMillis, it.pushedToGitHub)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun add(entry: SecretEntry): DevoraResult<Unit> = mutex.withLock {
        return try {
            val existing = if (storeFile.exists()) {
                json.decodeFromString(SecretRegistry.serializer(), storeFile.readText())
            } else SecretRegistry()
            val updated = existing.entries + StoredSecretEntry(
                entry.id, entry.projectRootPath, entry.name, entry.createdAtEpochMillis, entry.pushedToGitHub
            )
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(json.encodeToString(SecretRegistry.serializer(), SecretRegistry(updated)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save secret registry entry", cause = e)
        }
    }

    suspend fun markPushed(secretId: String): DevoraResult<Unit> = mutex.withLock {
        return try {
            if (!storeFile.exists()) return DevoraResult.Failure(message = "Registry file does not exist")
            val existing = json.decodeFromString(SecretRegistry.serializer(), storeFile.readText())
            val updated = existing.entries.map { if (it.id == secretId) it.copy(pushedToGitHub = true) else it }
            storeFile.writeText(json.encodeToString(SecretRegistry.serializer(), SecretRegistry(updated)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to update secret registry", cause = e)
        }
    }

    suspend fun remove(secretId: String): DevoraResult<Unit> = mutex.withLock {
        return try {
            if (!storeFile.exists()) return DevoraResult.Success(Unit)
            val existing = json.decodeFromString(SecretRegistry.serializer(), storeFile.readText())
            val filtered = existing.entries.filterNot { it.id == secretId }
            storeFile.writeText(json.encodeToString(SecretRegistry.serializer(), SecretRegistry(filtered)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to remove secret registry entry", cause = e)
        }
    }
}