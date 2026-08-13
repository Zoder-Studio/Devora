package dev.devora.feature.workflowengine.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowPermissionEntry

interface WorkflowPermissionRepository {
    suspend fun listForProject(projectRootPath: String): List<WorkflowPermissionEntry>

    /** Returns null if no permission has ever been explicitly set — callers must treat null as "denied", never as an implicit default (spec section 13: "Jangan otomatis memberikan permission"). */
    suspend fun get(projectRootPath: String, workflowId: String): WorkflowPermission?

    suspend fun set(projectRootPath: String, workflowId: String, permission: WorkflowPermission): DevoraResult<Unit>

    suspend fun remove(projectRootPath: String, workflowId: String): DevoraResult<Unit>
}