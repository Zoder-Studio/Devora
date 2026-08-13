package dev.devora.feature.workflowengine.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowPermissionEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class StoredPermission(
    val projectRootPath: String,
    val workflowId: String,
    val permission: String
)

@Serializable
private data class PermissionRegistry(val entries: List<StoredPermission> = emptyList())

/**
 * Persists Devora's own workflow permissions, keyed by
 * (projectRootPath, workflowId) exactly as spec section 13 requires:
 * "Permission harus terkait dengan Project/Repository + Workflow.
 * Bukan global." This file lives in Devora's own app-private storage,
 * never inside the tracked project directory.
 */
class WorkflowPermissionStore(private val storeFile: File) {
    private val mutex = Mutex()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun readAll(): List<WorkflowPermissionEntry> = mutex.withLock {
        if (!storeFile.exists()) return emptyList()
        return try {
            val registry = json.decodeFromString(PermissionRegistry.serializer(), storeFile.readText())
            registry.entries.map {
                WorkflowPermissionEntry(it.projectRootPath, it.workflowId, WorkflowPermission.valueOf(it.permission))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun get(projectRootPath: String, workflowId: String): WorkflowPermission? =
        readAll().find { it.projectRootPath == projectRootPath && it.workflowId == workflowId }?.permission

    suspend fun set(entry: WorkflowPermissionEntry): DevoraResult<Unit> = mutex.withLock {
        return try {
            val existing = if (storeFile.exists()) {
                json.decodeFromString(PermissionRegistry.serializer(), storeFile.readText())
            } else {
                PermissionRegistry()
            }
            val filtered = existing.entries.filterNot {
                it.projectRootPath == entry.projectRootPath && it.workflowId == entry.workflowId
            }
            val updated = filtered + StoredPermission(entry.projectRootPath, entry.workflowId, entry.permission.name)
            storeFile.parentFile?.mkdirs()
            storeFile.writeText(json.encodeToString(PermissionRegistry.serializer(), PermissionRegistry(updated)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to save workflow permission", cause = e)
        }
    }

    suspend fun remove(projectRootPath: String, workflowId: String): DevoraResult<Unit> = mutex.withLock {
        return try {
            if (!storeFile.exists()) return DevoraResult.Success(Unit)
            val existing = json.decodeFromString(PermissionRegistry.serializer(), storeFile.readText())
            val filtered = existing.entries.filterNot {
                it.projectRootPath == projectRootPath && it.workflowId == workflowId
            }
            storeFile.writeText(json.encodeToString(PermissionRegistry.serializer(), PermissionRegistry(filtered)))
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to remove workflow permission", cause = e)
        }
    }
}