package dev.devora.feature.workflowengine.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine
import dev.devora.feature.workflowengine.domain.model.WorkflowEnvironmentInfo

interface WorkflowEnvironmentRepository {
    fun getInfo(workflowId: String): WorkflowEnvironmentInfo

    /** Returns the CommandExecutionEngine scoped to this workflow's own environment (embedded mode) or the shared engine (Termux app mode — see getInfo().isIsolated). */
    suspend fun getOrPrepareEngine(workflowId: String, onProgress: (String) -> Unit = {}): DevoraResult<CommandExecutionEngine>

    suspend fun resetEnvironment(workflowId: String): DevoraResult<Unit>

    suspend fun deleteEnvironment(workflowId: String): DevoraResult<Unit>
}