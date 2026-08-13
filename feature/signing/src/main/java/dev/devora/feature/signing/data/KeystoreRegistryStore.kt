package dev.devora.feature.signing.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.domain.model.KeystoreEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class StoredKeystoreEntry(
    val id: String,
    val keystoreFilePath: String,
    val alias: String,
    val createdAtEpochMillis: Long
)

@Serializable
private data class KeystoreRegistry(val entries: List<StoredKeystoreEntry> = emptyList())

class KeystoreRegistryStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun readAll(): List<KeystoreEntry> = mutex.withLock {
        if (!storeFile.exists()) return emptyList()
        return try {
            json.decodeFromString(KeystoreRegistry.serializer(), storeFile.readText())
                .entries.map { KeystoreEntry(it.id, it.keystoreFilePath, it.alias, it.createdAtEpochMillis) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun add(entry: KeystoreEntry): DevoraResult<Unit> = mutex.withLock {
        return try {
            val existing = if (storeFile.exists()) {
                json.decodeFromString(KeystoreRegistry.serializer(), storeFile.readText())
            } else {
                KeystoreRegistry()
            }
            val updated = existing.entries + StoredKeystoreEntry(
                entry.id, entry.keystoreFilePath, entry.alias, entry.createdAtEpochMillis
            )
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(json.encodeToString(KeystoreRegistry.serializer(), KeystoreRegistry(updated)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save keystore registry entry", cause = e)
        }
    }
}