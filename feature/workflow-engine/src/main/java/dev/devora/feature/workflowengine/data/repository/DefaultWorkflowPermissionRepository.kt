package dev.devora.feature.workflowengine.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.data.WorkflowPermissionStore
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowPermissionEntry
import dev.devora.feature.workflowengine.domain.repository.WorkflowPermissionRepository

class DefaultWorkflowPermissionRepository(
    private val store: WorkflowPermissionStore
) : WorkflowPermissionRepository {

    override suspend fun listForProject(projectRootPath: String): List<WorkflowPermissionEntry> =
        store.readAll().filter { it.projectRootPath == projectRootPath }

    override suspend fun get(projectRootPath: String, workflowId: String): WorkflowPermission? =
        store.get(projectRootPath, workflowId)

    override suspend fun set(
        projectRootPath: String,
        workflowId: String,
        permission: WorkflowPermission
    ): DevoraResult<Unit> = store.set(WorkflowPermissionEntry(projectRootPath, workflowId, permission))

    override suspend fun remove(projectRootPath: String, workflowId: String): DevoraResult<Unit> =
        store.remove(projectRootPath, workflowId)
}