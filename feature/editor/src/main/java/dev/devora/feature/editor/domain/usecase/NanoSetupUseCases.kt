package dev.devora.feature.editor.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.editor.domain.repository.NanoRepository

/**
 * Runs the full embedded-engine nano setup in one step: install nano
 * itself, then configure syntax highlighting. Exposed as a single use
 * case so the UI only has to trigger one action instead of chaining
 * two repository calls and handling partial-failure states itself.
 */
class SetupNanoEmbeddedUseCase(private val repository: NanoRepository) {
    suspend operator fun invoke(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        val installResult = repository.installNanoInEmbeddedEngine(onOutputLine)
        if (installResult is DevoraResult.Failure) return installResult

        return repository.configureSyntaxHighlightingEmbedded(onOutputLine)
    }
}