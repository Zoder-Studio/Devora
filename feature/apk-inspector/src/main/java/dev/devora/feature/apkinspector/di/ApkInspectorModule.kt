package dev.devora.feature.apkinspector.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.apkinspector.data.repository.DefaultAabInspectorRepository
import dev.devora.feature.apkinspector.data.repository.DefaultApkInspectorRepository
import dev.devora.feature.apkinspector.domain.repository.AabInspectorRepository
import dev.devora.feature.apkinspector.domain.repository.ApkInspectorRepository
import dev.devora.feature.apkinspector.domain.usecase.InspectAabUseCase
import dev.devora.feature.apkinspector.domain.usecase.InspectApkUseCase
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApkInspectorModule {

    @Provides
    @Singleton
    fun provideApkInspectorRepository(@ApplicationContext context: Context): ApkInspectorRepository =
        DefaultApkInspectorRepository(context)

    @Provides
    @Singleton
    fun provideAabInspectorRepository(
        engineProvider: CommandExecutionEngineProvider,
        enginePaths: EnginePaths
    ): AabInspectorRepository = DefaultAabInspectorRepository(engineProvider, enginePaths)

    @Provides
    fun provideInspectApkUseCase(repository: ApkInspectorRepository) = InspectApkUseCase(repository)

    @Provides
    fun provideInspectAabUseCase(repository: AabInspectorRepository) = InspectAabUseCase(repository)
}