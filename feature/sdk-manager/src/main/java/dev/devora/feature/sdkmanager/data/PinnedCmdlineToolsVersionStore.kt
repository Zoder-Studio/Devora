package dev.devora.feature.sdkmanager.data

import dev.devora.core.common.result.DevoraResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PinnedCmdlineToolsVersion(val downloadUrl: String, val revision: String)

class PinnedCmdlineToolsVersionStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun read(default: PinnedCmdlineToolsVersion): PinnedCmdlineToolsVersion = mutex.withLock {
        if (!storeFile.exists()) return default
        return try {
            json.decodeFromString(PinnedCmdlineToolsVersion.serializer(), storeFile.readText())
        } catch (e: Exception) {
            default
        }
    }

    suspend fun write(version: PinnedCmdlineToolsVersion): DevoraResult<Unit> = mutex.withLock {
        return try {
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(json.encodeToString(PinnedCmdlineToolsVersion.serializer(), version))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save pinned cmdline-tools version", cause = e)
        }
    }
}