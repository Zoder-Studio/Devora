package dev.devora.feature.projectmanager.data.local

import dev.devora.core.common.result.DevoraResult
import dev.devora.core.logging.DevoraLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private const val TAG = "ProjectRegistryStore"

/**
 * Stores which project directories Devora knows about.
 *
 * This registry lives inside Devora's own app-private storage, never
 * inside a tracked project directory, so it never pollutes a
 * developer's repository or gets accidentally committed to Git.
 */
class ProjectRegistryStore(
    private val registryFile: File,
    private val json: Json = Json { prettyPrint = true; ignoreUnknownKeys = true }
) {
    private val mutex = Mutex()

    suspend fun read(): DevoraResult<ProjectRegistry> = mutex.withLock {
        if (!registryFile.exists()) {
            return DevoraResult.Success(ProjectRegistry())
        }
        return try {
            val text = registryFile.readText()
            if (text.isBlank()) {
                DevoraResult.Success(ProjectRegistry())
            } else {
                DevoraResult.Success(json.decodeFromString(ProjectRegistry.serializer(), text))
            }
        } catch (e: Exception) {
            DevoraLogger.e(TAG, "Failed to read project registry", e)
            DevoraResult.Failure(
                message = "Failed to read project registry at ${registryFile.absolutePath}",
                cause = e
            )
        }
    }

    suspend fun write(registry: ProjectRegistry): DevoraResult<Unit> = mutex.withLock {
        return try {
            registryFile.parentFile?.mkdirs()
            val tempFile = File(registryFile.parentFile, "${registryFile.name}.tmp")
            tempFile.writeText(json.encodeToString(registry))
            if (!tempFile.renameTo(registryFile)) {
                throw IOException("Atomic rename from ${tempFile.name} to ${registryFile.name} failed")
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraLogger.e(TAG, "Failed to write project registry", e)
            DevoraResult.Failure(
                message = "Failed to write project registry at ${registryFile.absolutePath}",
                cause = e
            )
        }
    }
}