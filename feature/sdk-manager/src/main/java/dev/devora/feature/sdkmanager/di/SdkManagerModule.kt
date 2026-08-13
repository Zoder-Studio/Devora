package dev.devora.feature.sdkmanager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.sdkmanager.data.repository.DefaultSdkRepository
import dev.devora.feature.sdkmanager.domain.repository.SdkRepository
import dev.devora.feature.sdkmanager.domain.usecase.InstallSdkPackageUseCase
import dev.devora.feature.sdkmanager.domain.usecase.ListSdkPackagesUseCase
import dev.devora.feature.sdkmanager.domain.usecase.SetupSdkToolingUseCase
import dev.devora.feature.sdkmanager.domain.usecase.UninstallSdkPackageUseCase
import dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Singleton
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object SdkManagerModule {

    @Provides
    fun provideSetupSdkToolingUseCase(repository: SdkRepository) = SetupSdkToolingUseCase(repository)

    @Provides
    fun provideListSdkPackagesUseCase(repository: SdkRepository) = ListSdkPackagesUseCase(repository)

    @Provides
    fun provideInstallSdkPackageUseCase(repository: SdkRepository) = InstallSdkPackageUseCase(repository)

    @Provides
    fun provideUninstallSdkPackageUseCase(repository: SdkRepository) = UninstallSdkPackageUseCase(repository)

    @Provides
    @Singleton
    fun providePinnedCmdlineToolsVersionStore(
        @ApplicationContext context: Context
    ): PinnedCmdlineToolsVersionStore =
        PinnedCmdlineToolsVersionStore(File(context.filesDir, "devora/pinned_cmdline_tools_version.json"))

    @Provides
    @Singleton
    fun provideCmdlineToolsVersionCheckRepository(
        versionStore: PinnedCmdlineToolsVersionStore
    ): CmdlineToolsVersionCheckRepository = DefaultCmdlineToolsVersionCheckRepository(versionStore)

    @Provides
    @Singleton
    fun provideSdkRepositoryWithVersionStore(
        prefixManager: EmbeddedPrefixManager,
        commandRunner: EmbeddedCommandRunner,
        versionStore: PinnedCmdlineToolsVersionStore
    ): SdkRepository = DefaultSdkRepository(prefixManager, commandRunner, versionStore)
}