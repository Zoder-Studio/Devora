package dev.devora.feature.accountsecurity.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.accountsecurity.data.DeviceKeyManager
import dev.devora.feature.accountsecurity.data.DpatStore
import dev.devora.feature.accountsecurity.data.repository.DefaultAccountSecurityRepository
import dev.devora.feature.accountsecurity.domain.backend.DpatBackendApi
import dev.devora.feature.accountsecurity.domain.backend.NoBackendDpatApi
import dev.devora.feature.accountsecurity.domain.repository.AccountSecurityRepository
import dev.devora.feature.accountsecurity.domain.usecase.CreateDpatUseCase
import dev.devora.feature.accountsecurity.domain.usecase.GetCurrentDpatUseCase
import dev.devora.feature.accountsecurity.domain.usecase.IsCrossDeviceEnforcementAvailableUseCase
import dev.devora.feature.accountsecurity.domain.usecase.RevokeLocalDpatUseCase
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountSecurityModule {

    @Provides
    @Singleton
    fun provideDeviceKeyManager(): DeviceKeyManager = DeviceKeyManager()

    @Provides
    @Singleton
    fun provideDpatStore(@ApplicationContext context: Context): DpatStore =
        DpatStore(File(context.filesDir, "devora/dpat.json"))

    /**
     * Swap this binding for a real implementation once a Devora backend
     * exists. Until then, cross-device enforcement is honestly reported
     * as unavailable (see AccountSecurityRepository.isCrossDeviceEnforcementAvailable).
     */
    @Provides
    @Singleton
    fun provideDpatBackendApi(): DpatBackendApi = NoBackendDpatApi()

    @Provides
    @Singleton
    fun provideAccountSecurityRepository(
        deviceKeyManager: DeviceKeyManager,
        dpatStore: DpatStore,
        backendApi: DpatBackendApi
    ): AccountSecurityRepository = DefaultAccountSecurityRepository(deviceKeyManager, dpatStore, backendApi)

    @Provides fun provideGetCurrentDpatUseCase(r: AccountSecurityRepository) = GetCurrentDpatUseCase(r)
    @Provides fun provideCreateDpatUseCase(r: AccountSecurityRepository) = CreateDpatUseCase(r)
    @Provides fun provideRevokeLocalDpatUseCase(r: AccountSecurityRepository) = RevokeLocalDpatUseCase(r)
    @Provides fun provideIsCrossDeviceEnforcementAvailableUseCase(r: AccountSecurityRepository) =
        IsCrossDeviceEnforcementAvailableUseCase(r)
}