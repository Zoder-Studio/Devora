package dev.devora.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.app.MainActivity
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object DeepLinkModule {

    @Provides
    @Named("deepLinkActivityClass")
    fun provideDeepLinkActivityClass(): Class<*> = MainActivity::class.java
}