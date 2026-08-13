package dev.devora.feature.terminal.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.domain.model.BootstrapVersionCheckResult

interface BootstrapVersionCheckRepository {
    /** Read-only: fetches the latest bootstrap release tag and compares against the pinned one. Never installs anything. */
    suspend fun checkForUpdate(): DevoraResult<BootstrapVersionCheckResult>
}