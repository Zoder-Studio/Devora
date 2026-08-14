package dev.devora.feature.accountsecurity.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.accountsecurity.domain.model.Dpat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class StoredDpat(
    val id: String,
    val phoneLabel: String?,
    val isPrimaryPhone: Boolean,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?
)

class DpatStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun read(): Dpat? = mutex.withLock {
        if (!storeFile.exists()) return null
        return try {
            val stored = json.decodeFromString(StoredDpat.serializer(), storeFile.readText())
            Dpat(stored.id, stored.phoneLabel, stored.isPrimaryPhone, stored.createdAtEpochMillis, stored.expiresAtEpochMillis)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(dpat: Dpat): DevoraResult<Unit> = mutex.withLock {
        return try {
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(
                json.encodeToString(
                    StoredDpat.serializer(),
                    StoredDpat(dpat.id, dpat.phoneLabel, dpat.isPrimaryPhone, dpat.createdAtEpochMillis, dpat.expiresAtEpochMillis)
                )
            )
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save DPAT", cause = e)
        }
    }

    suspend fun clear(): DevoraResult<Unit> = mutex.withLock {
        return try {
            if (storeFile.exists()) storeFile.delete()
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to clear DPAT", cause = e)
        }
    }
}