package dev.devora.feature.terminal.data.embedded

import dev.devora.core.common.result.DevoraResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PinnedBootstrapVersion(val releaseTag: String)

/**
 * Persists which bootstrap release Devora is pinned to. This is what
 * actually gets used to build the download URL — the compile-time
 * default in BootstrapContract is only the initial value on first run.
 * Changing this file is the only way the pinned version changes, and
 * it only changes when a developer explicitly taps "Update" (see
 * BootstrapVersionCheckViewModel) — never automatically.
 */
class PinnedBootstrapVersionStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun read(default: String): PinnedBootstrapVersion = mutex.withLock {
        if (!storeFile.exists()) return PinnedBootstrapVersion(default)
        return try {
            json.decodeFromString(PinnedBootstrapVersion.serializer(), storeFile.readText())
        } catch (e: Exception) {
            PinnedBootstrapVersion(default)
        }
    }

    suspend fun write(version: PinnedBootstrapVersion): DevoraResult<Unit> = mutex.withLock {
        return try {
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(json.encodeToString(PinnedBootstrapVersion.serializer(), version))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save pinned bootstrap version", cause = e)
        }
    }
}