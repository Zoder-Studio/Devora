package dev.devora.feature.github.domain.model

data class DeviceCodeInfo(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Int,
    val pollIntervalSeconds: Int
)

sealed class DeviceFlowPollResult {
    data class Success(val accessToken: String) : DeviceFlowPollResult()
    data object AuthorizationPending : DeviceFlowPollResult()
    data object SlowDown : DeviceFlowPollResult()
    data object ExpiredToken : DeviceFlowPollResult()
    data object AccessDenied : DeviceFlowPollResult()
    data class OtherError(val message: String) : DeviceFlowPollResult()
}

data class GitHubUser(
    val login: String,
    val name: String?,
    val avatarUrl: String?
)

data class GitHubOrg(
    val login: String
)

data class GitHubRepo(
    val fullName: String,
    val cloneUrl: String,
    val defaultBranch: String,
    val private: Boolean
)