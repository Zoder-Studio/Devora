package dev.devora.feature.sdkmanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.domain.model.CmdlineToolsVersionCheckResult

interface CmdlineToolsVersionCheckRepository {
    /** Read-only: fetches the latest cmdline-tools revision and compares against the pinned one. Never installs anything. */
    suspend fun checkForUpdate(): DevoraResult<CmdlineToolsVersionCheckResult>
}