package dev.devora.feature.notifications.domain.repository

import dev.devora.feature.notifications.domain.model.WorkflowNotification

interface NotificationRepository {
    fun ensureChannelCreated()

    /** Posts or updates a notification with the given id — same id means the notification updates in place (queued -> running -> success/failed), matching real Android notification update semantics. */
    fun notify(notification: WorkflowNotification)

    fun cancel(notificationId: Int)
}