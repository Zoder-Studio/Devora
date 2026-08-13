package dev.devora.feature.workflowengine.domain.model

/**
 * Devora's own sandbox permission for the Built-in Runner — separate
 * from GitHub Actions' "permissions: contents: write" YAML key (spec
 * section 14). Enforced via real POSIX write permission bits on the
 * project directory (see WorkflowPermissionEnforcer), not simulated.
 */
enum class WorkflowPermission {
    READ,
    WRITE,
    READ_WRITE
}

data class WorkflowPermissionEntry(
    val projectRootPath: String,
    val workflowId: String,
    val permission: WorkflowPermission
)