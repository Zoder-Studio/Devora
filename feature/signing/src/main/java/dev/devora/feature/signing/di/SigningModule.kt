package dev.devora.feature.signing.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.signing.data.KeystoreRegistryStore
import dev.devora.feature.signing.data.SecureKeystoreCredentialStore
import dev.devora.feature.signing.data.repository.DefaultSigningRepository
import dev.devora.feature.signing.domain.repository.SigningRepository
import dev.devora.feature.signing.domain.usecase.CreateKeystoreUseCase
import dev.devora.feature.signing.domain.usecase.ListKeystoresUseCase
import dev.devora.feature.signing.domain.usecase.SignApkUseCase
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SigningModule {

    @Provides
    @Singleton
    fun provideKeystoreRegistryStore(@ApplicationContext context: Context): KeystoreRegistryStore =
        KeystoreRegistryStore(File(context.filesDir, "devora/keystore_registry.json"))

    @Provides
    @Singleton
    fun provideSecureKeystoreCredentialStore(@ApplicationContext context: Context): SecureKeystoreCredentialStore =
        SecureKeystoreCredentialStore(context)

    @Provides
    @Singleton
    fun provideSigningRepository(
        engineProvider: CommandExecutionEngineProvider,
        enginePaths: EnginePaths,
        registryStore: KeystoreRegistryStore,
        credentialStore: SecureKeystoreCredentialStore
    ): SigningRepository = DefaultSigningRepository(engineProvider, enginePaths, registryStore, credentialStore)

    @Provides
    fun provideListKeystoresUseCase(repository: SigningRepository) = ListKeystoresUseCase(repository)

    @Provides
    fun provideCreateKeystoreUseCase(repository: SigningRepository) = CreateKeystoreUseCase(repository)

    @Provides
    fun provideSignApkUseCase(repository: SigningRepository) = SignApkUseCase(repository)
}