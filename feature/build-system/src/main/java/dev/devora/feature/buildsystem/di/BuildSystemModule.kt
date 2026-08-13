package dev.devora.feature.buildsystem.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.buildsystem.data.BuildLogStore
import dev.devora.feature.buildsystem.data.repository.DefaultBuildRepository
import dev.devora.feature.buildsystem.domain.repository.BuildRepository
import dev.devora.feature.buildsystem.domain.usecase.ExportBuildLogUseCase
import dev.devora.feature.buildsystem.domain.usecase.ReadBuildLogTailUseCase
import dev.devora.feature.buildsystem.domain.usecase.RunGradleTaskUseCase
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BuildSystemModule {

    @Provides
    @Singleton
    fun provideBuildLogStore(@ApplicationContext context: Context): BuildLogStore = BuildLogStore(context)

    @Provides
    @Singleton
    fun provideBuildRepository(
        engineProvider: CommandExecutionEngineProvider,
        enginePaths: EnginePaths,
        logStore: BuildLogStore
    ): BuildRepository = DefaultBuildRepository(engineProvider, enginePaths, logStore)

    @Provides
    fun provideRunGradleTaskUseCase(repository: BuildRepository) = RunGradleTaskUseCase(repository)

    @Provides
    fun provideReadBuildLogTailUseCase(repository: BuildRepository) = ReadBuildLogTailUseCase(repository)

    @Provides
    fun provideExportBuildLogUseCase(repository: BuildRepository) = ExportBuildLogUseCase(repository)
}