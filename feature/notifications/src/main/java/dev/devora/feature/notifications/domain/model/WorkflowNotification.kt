package dev.devora.feature.notifications.domain.model

enum class WorkflowNotificationStatus { QUEUED, RUNNING, SUCCESS, FAILED }

/**
 * deepLinkPath maps directly to a DevoraDestinations route string
 * (e.g. "workflow_run/<rootPath>/<workflowFilePath>/<jobId>") so
 * tapping the notification can open exactly the relevant screen
 * (spec section 21: "Tap notification membuka workflow/log/artifact
 * yang relevan"). This module does not depend on :app's nav graph
 * directly — it only carries the path string, and MainActivity
 * resolves it into an actual navigation call.
 */
data class WorkflowNotification(
    val id: Int,
    val title: String,
    val message: String,
    val status: WorkflowNotificationStatus,
    val deepLinkPath: String?
)