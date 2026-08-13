package dev.devora.feature.workflowengine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import dev.devora.feature.workflowengine.data.ActionYamlParser
import dev.devora.feature.workflowengine.data.LocalActionResolver
import dev.devora.feature.workflowengine.data.WorkflowYamlParser
import dev.devora.feature.workflowengine.data.repository.DefaultWorkflowEnvironmentRepository
import dev.devora.feature.workflowengine.data.repository.DefaultWorkflowRepository
import dev.devora.feature.workflowengine.domain.repository.WorkflowEnvironmentRepository
import dev.devora.feature.workflowengine.domain.repository.WorkflowRepository
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkflowEngineModule {

    @Provides
    @Singleton
    fun provideWorkflowYamlParser(): WorkflowYamlParser = WorkflowYamlParser()

    @Provides
    @Singleton
    fun provideActionYamlParser(): ActionYamlParser = ActionYamlParser()

    @Provides
    @Singleton
    fun provideLocalActionResolver(actionYamlParser: ActionYamlParser): LocalActionResolver =
        LocalActionResolver(actionYamlParser)

    @Provides
    @Singleton
    fun provideWorkflowEnvironmentRepository(
        @ApplicationContext context: Context,
        engineSelector: TerminalEngineSelector,
        sharedEngineProvider: CommandExecutionEngineProvider
    ): WorkflowEnvironmentRepository =
        DefaultWorkflowEnvironmentRepository(context, engineSelector, sharedEngineProvider)

    @Provides
    @Singleton
    fun provideWorkflowRepository(
        parser: WorkflowYamlParser,
        environmentRepository: WorkflowEnvironmentRepository,
        enginePaths: EnginePaths,
        localActionResolver: LocalActionResolver,
        permissionRepository: WorkflowPermissionRepository,
        @ApplicationContext context: Context
    ): WorkflowRepository = DefaultWorkflowRepository(
        parser,
        environmentRepository,
        enginePaths,
        File(context.filesDir, "devora/artifact_staging"),
        localActionResolver,
        premissionRepository
    )

    @Provides
    @Singleton
    fun provideWorkflowPermissionStore(@ApplicationContext context: Context): WorkflowPermissionStore =
        WorkflowPermissionStore(File(context.filesDir, "devora/workflow_permissions.json"))

    @Provides
    @Singleton
    fun provideWorkflowPermissionRepository(
        store: WorkflowPermissionStore
    ): WorkflowPermissionRepository = DefaultWorkflowPermissionRepository(store)
}