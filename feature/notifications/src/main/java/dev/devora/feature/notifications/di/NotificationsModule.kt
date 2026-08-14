package dev.devora.feature.notifications.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.notifications.data.repository.DefaultNotificationRepository
import dev.devora.feature.notifications.domain.repository.NotificationRepository
import dev.devora.feature.notifications.domain.usecase.PostWorkflowNotificationUseCase
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(
        @ApplicationContext context: Context,
        @Named("deepLinkActivityClass") deepLinkActivityClass: Class<*>
    ): NotificationRepository = DefaultNotificationRepository(context, deepLinkActivityClass).also { it.ensureChannelCreated() }

    @Provides
    fun providePostWorkflowNotificationUseCase(repository: NotificationRepository) =
        PostWorkflowNotificationUseCase(repository)
}