package dev.devora.feature.editor.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.editor.data.repository.DefaultNanoRepository
import dev.devora.feature.editor.domain.repository.NanoRepository
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.data.embedded.EmbeddedSessionFactory
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EditorModule {

    @Provides
    @Singleton
    fun provideNanoRepository(
        engineSelector: TerminalEngineSelector,
        prefixManager: EmbeddedPrefixManager,
        embeddedSessionFactory: EmbeddedSessionFactory
    ): NanoRepository = DefaultNanoRepository(engineSelector, prefixManager, embeddedSessionFactory)

    @Provides
    fun provideSetupNanoEmbeddedUseCase(repository: NanoRepository) =
        dev.devora.feature.editor.domain.usecase.SetupNanoEmbeddedUseCase(repository)
}