package dev.devora.feature.workflowengine.domain.model

enum class WorkflowStepStatus { PENDING, RUNNING, SUCCESS, FAILED, SKIPPED }

data class WorkflowStepResult(
    val stepIndex: Int,
    val stepName: String,
    val status: WorkflowStepStatus,
    val outputLines: List<String> = emptyList(),
    val errorMessage: String? = null
)

data class WorkflowRunResult(
    val workflowFilePath: String,
    val jobId: String,
    val stepResults: List<WorkflowStepResult>,
    val overallSuccess: Boolean
)