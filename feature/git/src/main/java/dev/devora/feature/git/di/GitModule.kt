package dev.devora.feature.git.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.git.data.repository.DefaultGitRepository
import dev.devora.feature.git.domain.repository.GitRepository
import dev.devora.feature.git.domain.usecase.*
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GitModule {

    @Provides
    @Singleton
    fun provideGitRepository(
        engineProvider: CommandExecutionEngineProvider,
        enginePaths: EnginePaths
    ): GitRepository = DefaultGitRepository(engineProvider, enginePaths)

    @Provides fun provideGitStatusUseCase(r: GitRepository) = GitStatusUseCase(r)
    @Provides fun provideGitAddUseCase(r: GitRepository) = GitAddUseCase(r)
    @Provides fun provideGitUnstageUseCase(r: GitRepository) = GitUnstageUseCase(r)
    @Provides fun provideGitCommitUseCase(r: GitRepository) = GitCommitUseCase(r)
    @Provides fun provideGitLogUseCase(r: GitRepository) = GitLogUseCase(r)
    @Provides fun provideGitListBranchesUseCase(r: GitRepository) = GitListBranchesUseCase(r)
    @Provides fun provideGitCheckoutUseCase(r: GitRepository) = GitCheckoutUseCase(r)
    @Provides fun provideGitPushUseCase(r: GitRepository) = GitPushUseCase(r)
    @Provides fun provideGitPullUseCase(r: GitRepository) = GitPullUseCase(r)
    @Provides fun provideGitDiffUseCase(r: GitRepository) = GitDiffUseCase(r)
}