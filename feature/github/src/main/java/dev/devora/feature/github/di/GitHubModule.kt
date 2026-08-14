package dev.devora.feature.github.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.git.domain.repository.GitRepository
import dev.devora.feature.github.data.GitHubTokenStore
import dev.devora.feature.github.data.repository.DefaultGitHubApiRepository
import dev.devora.feature.github.data.repository.DefaultGitHubAuthRepository
import dev.devora.feature.github.domain.repository.GitHubApiRepository
import dev.devora.feature.github.domain.repository.GitHubAuthRepository
import dev.devora.feature.github.domain.usecase.PushToGitHubUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GitHubModule {

    @Provides
    @Singleton
    fun provideGitHubTokenStore(@ApplicationContext context: Context): GitHubTokenStore =
        GitHubTokenStore(context)

    @Provides
    @Singleton
    fun provideGitHubAuthRepository(tokenStore: GitHubTokenStore): GitHubAuthRepository =
        DefaultGitHubAuthRepository(tokenStore)

    @Provides
    @Singleton
    fun provideGitHubApiRepository(tokenStore: GitHubTokenStore): GitHubApiRepository =
        DefaultGitHubApiRepository(tokenStore)

    @Provides
    @Singleton
    fun providePushToGitHubUseCase(
        gitRepository: GitRepository,
        tokenStore: GitHubTokenStore
    ): PushToGitHubUseCase = PushToGitHubUseCase(gitRepository, tokenStore)
}