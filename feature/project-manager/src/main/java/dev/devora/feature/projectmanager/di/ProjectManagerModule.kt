package dev.devora.feature.projectmanager.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.projectmanager.data.local.ProjectRegistryStore
import dev.devora.feature.projectmanager.data.repository.DefaultProjectRepository
import dev.devora.feature.projectmanager.domain.repository.ProjectRepository
import dev.devora.feature.projectmanager.domain.usecase.ImportProjectUseCase
import dev.devora.feature.projectmanager.domain.usecase.ListProjectsUseCase
import dev.devora.feature.projectmanager.domain.usecase.OpenProjectUseCase
import dev.devora.feature.projectmanager.domain.usecase.RemoveProjectUseCase
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProjectManagerModule {

    @Provides
    @Singleton
    fun provideProjectRegistryStore(@ApplicationContext context: Context): ProjectRegistryStore {
        val registryFile = File(context.filesDir, "devora/project_registry.json")
        return ProjectRegistryStore(registryFile)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(store: ProjectRegistryStore): ProjectRepository =
        DefaultProjectRepository(store)

    @Provides
    fun provideListProjectsUseCase(repository: ProjectRepository) =
        ListProjectsUseCase(repository)

    @Provides
    fun provideImportProjectUseCase(repository: ProjectRepository) =
        ImportProjectUseCase(repository)

    @Provides
    fun provideRemoveProjectUseCase(repository: ProjectRepository) =
        RemoveProjectUseCase(repository)

    @Provides
    fun provideOpenProjectUseCase(repository: ProjectRepository) =
        OpenProjectUseCase(repository)
}