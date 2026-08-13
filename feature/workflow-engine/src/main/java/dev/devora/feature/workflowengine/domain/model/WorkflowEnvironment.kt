package dev.devora.feature.workflowengine.domain.model

enum class WorkflowEnvironmentStatus {
    NOT_INITIALIZED,
    READY,
    RUNNING
}

data class WorkflowEnvironmentInfo(
    val workflowId: String,
    val isIsolated: Boolean,
    val status: WorkflowEnvironmentStatus,
    val installedBootstrapVersion: String?,
    val storageSizeBytes: Long
)