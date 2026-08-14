package dev.devora.feature.notifications.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.devora.feature.notifications.data.DevoraNotificationChannels
import dev.devora.feature.notifications.domain.model.WorkflowNotification
import dev.devora.feature.notifications.domain.model.WorkflowNotificationStatus
import dev.devora.feature.notifications.domain.repository.NotificationRepository

class DefaultNotificationRepository(
    private val context: Context,
    private val deepLinkActivityClass: Class<*>
) : NotificationRepository {

    override fun ensureChannelCreated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                DevoraNotificationChannels.WORKFLOW_CHANNEL_ID,
                DevoraNotificationChannels.WORKFLOW_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = DevoraNotificationChannels.WORKFLOW_CHANNEL_DESCRIPTION
            }
            manager.createNotificationChannel(channel)
        }
    }

    override fun notify(notification: WorkflowNotification) {
        val intent = Intent(context, deepLinkActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            notification.deepLinkPath?.let { putExtra(DevoraNotificationChannels.EXTRA_DEEP_LINK_PATH, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = when (notification.status) {
            WorkflowNotificationStatus.SUCCESS -> android.R.drawable.stat_sys_download_done
            WorkflowNotificationStatus.FAILED -> android.R.drawable.stat_notify_error
            else -> android.R.drawable.stat_sys_download
        }

        val ongoing = notification.status == WorkflowNotificationStatus.QUEUED ||
            notification.status == WorkflowNotificationStatus.RUNNING

        val builder = NotificationCompat.Builder(context, DevoraNotificationChannels.WORKFLOW_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(!ongoing)
            .setOngoing(ongoing)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(notification.id, builder.build())
    }

    override fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}