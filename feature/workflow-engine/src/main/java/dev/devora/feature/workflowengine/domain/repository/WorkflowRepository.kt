package dev.devora.feature.workflowengine.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowDefinition
import dev.devora.feature.workflowengine.domain.model.WorkflowRunResult
import kotlinx.coroutines.flow.Flow

interface WorkflowRepository {
    fun listWorkflowFiles(projectRootPath: String): DevoraResult<List<String>>

    fun parseWorkflow(workflowFilePath: String): DevoraResult<WorkflowDefinition>

    /** Emits step-by-step progress as it happens, then a terminal WorkflowRunResult. */
    fun runJob(projectRootPath: String, workflow: WorkflowDefinition, jobId: String): Flow<WorkflowRunResult>
}