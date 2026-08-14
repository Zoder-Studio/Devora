package dev.devora.core.ui.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.core.ui.snackbar.DefaultDevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreUiModule {

    @Provides
    @Singleton
    fun provideDevoraSnackbarController(): DevoraSnackbarController = DefaultDevoraSnackbarController()
}