package dev.devora.feature.github.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.github.domain.model.DeviceCodeInfo
import dev.devora.feature.github.domain.model.DeviceFlowPollResult
import dev.devora.feature.github.domain.model.GitHubUser

interface GitHubAuthRepository {
    fun isLoggedIn(): Boolean

    suspend fun requestDeviceCode(): DevoraResult<DeviceCodeInfo>

    suspend fun pollForToken(deviceCode: String): DeviceFlowPollResult

    suspend fun getCurrentUser(): DevoraResult<GitHubUser>

    fun logout()
}