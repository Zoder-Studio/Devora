package dev.devora.feature.terminal.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.domain.model.TermuxIntegrationState
import dev.devora.feature.terminal.domain.model.TerminalCommandRequest
import dev.devora.feature.terminal.domain.model.TerminalEngineMode

interface TerminalRepository {
    fun currentEngineMode(): TerminalEngineMode
    fun checkIntegrationState(): TermuxIntegrationState
    fun openTerminalAt(directoryPath: String): DevoraResult<Unit>
    fun runCommand(request: TerminalCommandRequest): DevoraResult<Unit>
    suspend fun prepareEmbeddedEngineIfNeeded(onProgress: (String) -> Unit = {}): DevoraResult<Unit>
}