package dev.devora.feature.notifications.domain.usecase

import dev.devora.feature.notifications.domain.model.WorkflowNotification
import dev.devora.feature.notifications.domain.repository.NotificationRepository

class PostWorkflowNotificationUseCase(private val repository: NotificationRepository) {
    operator fun invoke(notification: WorkflowNotification) = repository.notify(notification)
}