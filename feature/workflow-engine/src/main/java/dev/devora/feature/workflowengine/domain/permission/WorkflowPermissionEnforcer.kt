package dev.devora.feature.workflowengine.domain.permission

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission

/**
 * Applies real POSIX write-permission enforcement for the duration of
 * a job run. READ-only permission removes write bits from every file
 * under the project root before steps execute, and restores them
 * afterward regardless of success or failure. This means a step that
 * tries to write with only READ permission gets a genuine OS
 * "Permission denied" — Devora does not intercept or fake this
 * decision at the application layer.
 *
 * Enforcement caveat that must stay visible to the developer: this
 * only blocks direct filesystem writes. A step could still perform
 * writes elsewhere (e.g. outside the project root, or via network
 * calls) that this mechanism cannot see or block — POSIX permission
 * bits are not a full sandbox. Devora does not claim otherwise.
 */
class WorkflowPermissionEnforcer(
    private val engine: CommandExecutionEngine
) {
    suspend fun <T> withEnforcement(
        projectRootPath: String,
        permission: WorkflowPermission,
        block: suspend () -> T
    ): DevoraResult<T> {
        val needsReadOnly = permission == WorkflowPermission.READ

        if (needsReadOnly) {
            val lockResult = engine.run(
                workingDirectory = projectRootPath,
                script = "chmod -R a-w '$projectRootPath'",
                onOutputLine = {}
            )
            if (lockResult is DevoraResult.Failure) {
                return DevoraResult.Failure(
                    message = "Failed to apply READ-only enforcement before running workflow: ${lockResult.message}"
                )
            }
        }

        return try {
            DevoraResult.Success(block())
        } finally {
            if (needsReadOnly) {
                engine.run(
                    workingDirectory = projectRootPath,
                    script = "chmod -R u+w '$projectRootPath'",
                    onOutputLine = {}
                )
            }
        }
    }
}