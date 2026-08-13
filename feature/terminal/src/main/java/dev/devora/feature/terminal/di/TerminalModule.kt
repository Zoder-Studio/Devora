package dev.devora.feature.terminal.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.terminal.data.engine.DefaultTerminalEngineSelector
import dev.devora.feature.terminal.data.repository.DefaultTerminalRepository
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.event.DefaultTerminalLaunchEventBus
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import dev.devora.feature.terminal.domain.repository.TerminalRepository
import dev.devora.feature.terminal.data.embedded.*
import javax.inject.Singleton
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object TerminalModule {

    @Provides
    @Singleton
    fun provideTerminalEngineSelector(@ApplicationContext context: Context): TerminalEngineSelector =
        DefaultTerminalEngineSelector(context)

    @Provides
    @Singleton
    fun provideEmbeddedPrefixManager(@ApplicationContext context: Context): EmbeddedPrefixManager =
        EmbeddedPrefixManager(context)

    @Provides
    @Singleton
    fun provideEmbeddedSessionFactory(prefixManager: EmbeddedPrefixManager): EmbeddedSessionFactory =
        EmbeddedSessionFactory(prefixManager)

    @Provides
    @Singleton
    fun provideTerminalLaunchEventBus(): TerminalLaunchEventBus = DefaultTerminalLaunchEventBus()

    @Provides
    @Singleton
    fun provideTerminalRepository(
        @ApplicationContext context: Context,
        engineSelector: TerminalEngineSelector,
        embeddedPrefixManager: EmbeddedPrefixManager
    ): TerminalRepository = DefaultTerminalRepository(context, engineSelector, embeddedPrefixManager)

    @Provides
    @Singleton
    fun provideEmbeddedCommandRunner(
        prefixManager: EmbeddedPrefixManager,
        sessionFactory: EmbeddedSessionFactory
    ): dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner =
        dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner(prefixManager, sessionFactory)

    @Provides
    @Singleton
    fun providePinnedBootstrapVersionStore(@ApplicationContext context: Context): PinnedBootstrapVersionStore =
        PinnedBootstrapVersionStore(File(context.filesDir, "devora/pinned_bootstrap_version.json"))

    @Provides
    @Singleton
    fun provideBootstrapVersionCheckRepository(
        versionStore: PinnedBootstrapVersionStore
    ): dev.devora.feature.terminal.domain.repository.BootstrapVersionCheckRepository =
        DefaultBootstrapVersionCheckRepository(versionStore)

    @Provides
    @Singleton
    fun provideEmbeddedCommandExecutionEngine(
        commandRunner: dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
    ): dev.devora.feature.terminal.data.execution.EmbeddedCommandExecutionEngine =
        dev.devora.feature.terminal.data.execution.EmbeddedCommandExecutionEngine(commandRunner)

    @Provides
    @Singleton
    fun provideTermuxAppCommandExecutionEngine(
        @ApplicationContext context: Context
    ): dev.devora.feature.terminal.data.execution.TermuxAppCommandExecutionEngine =
        dev.devora.feature.terminal.data.execution.TermuxAppCommandExecutionEngine(context)

    @Provides
    @Singleton
    fun provideCommandExecutionEngineProvider(
        engineSelector: TerminalEngineSelector,
        embeddedEngine: dev.devora.feature.terminal.data.execution.EmbeddedCommandExecutionEngine,
        termuxAppEngine: dev.devora.feature.terminal.data.execution.TermuxAppCommandExecutionEngine
    ): dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider =
        dev.devora.feature.terminal.data.execution.DefaultCommandExecutionEngineProvider(
            engineSelector, embeddedEngine, termuxAppEngine
        )

    @Provides
    @Singleton
    fun provideEmbeddedPrefixManager(
        @ApplicationContext context: Context,
        versionStore: PinnedBootstrapVersionStore
    ): EmbeddedPrefixManager = EmbeddedPrefixManager(context.filesDir, versionStore)
}