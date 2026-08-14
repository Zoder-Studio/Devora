package dev.devora.feature.secrets.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.github.data.GitHubTokenStore
import dev.devora.feature.secrets.data.GitHubSecretsApi
import dev.devora.feature.secrets.data.SecretRegistryStore
import dev.devora.feature.secrets.data.SecureSecretStore
import dev.devora.feature.secrets.data.repository.DefaultSecretRepository
import dev.devora.feature.secrets.domain.repository.SecretRepository
import dev.devora.feature.secrets.domain.usecase.AddSecretUseCase
import dev.devora.feature.secrets.domain.usecase.DeleteSecretUseCase
import dev.devora.feature.secrets.domain.usecase.ListSecretsUseCase
import dev.devora.feature.secrets.domain.usecase.PushSecretToGitHubUseCase
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecretsModule {

    @Provides
    @Singleton
    fun provideSecretRegistryStore(@ApplicationContext context: Context): SecretRegistryStore =
        SecretRegistryStore(File(context.filesDir, "devora/secret_registry.json"))

    @Provides
    @Singleton
    fun provideSecureSecretStore(@ApplicationContext context: Context): SecureSecretStore =
        SecureSecretStore(context)

    @Provides
    @Singleton
    fun provideGitHubSecretsApi(): GitHubSecretsApi = GitHubSecretsApi()

    @Provides
    @Singleton
    fun provideSecretRepository(
        registryStore: SecretRegistryStore,
        valueStore: SecureSecretStore,
        gitHubSecretsApi: GitHubSecretsApi,
        tokenStore: GitHubTokenStore
    ): SecretRepository = DefaultSecretRepository(registryStore, valueStore, gitHubSecretsApi, tokenStore)

    @Provides fun provideListSecretsUseCase(r: SecretRepository) = ListSecretsUseCase(r)
    @Provides fun provideAddSecretUseCase(r: SecretRepository) = AddSecretUseCase(r)
    @Provides fun provideDeleteSecretUseCase(r: SecretRepository) = DeleteSecretUseCase(r)
    @Provides fun providePushSecretToGitHubUseCase(r: SecretRepository) = PushSecretToGitHubUseCase(r)
}