package dev.devora.feature.artifactmanager.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.artifactmanager.data.install.DefaultArtifactInstaller
import dev.devora.feature.artifactmanager.data.repository.DefaultArtifactRepository
import dev.devora.feature.artifactmanager.domain.install.ArtifactInstaller
import dev.devora.feature.artifactmanager.domain.repository.ArtifactRepository
import dev.devora.feature.artifactmanager.domain.usecase.DeleteArtifactUseCase
import dev.devora.feature.artifactmanager.domain.usecase.ListArtifactsUseCase
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArtifactManagerModule {

    @Provides
    @Singleton
    fun provideArtifactRepository(@ApplicationContext context: Context): ArtifactRepository =
        DefaultArtifactRepository(File(context.filesDir, "devora/artifact_staging"))

    @Provides
    @Singleton
    fun provideArtifactInstaller(@ApplicationContext context: Context): ArtifactInstaller =
        DefaultArtifactInstaller(context)

    @Provides
    fun provideListArtifactsUseCase(repository: ArtifactRepository) = ListArtifactsUseCase(repository)

    @Provides
    fun provideDeleteArtifactUseCase(repository: ArtifactRepository) = DeleteArtifactUseCase(repository)
}