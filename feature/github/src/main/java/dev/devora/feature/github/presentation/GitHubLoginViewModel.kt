package dev.devora.feature.github.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.github.domain.model.DeviceCodeInfo
import dev.devora.feature.github.domain.model.DeviceFlowPollResult
import dev.devora.feature.github.domain.repository.GitHubAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GitHubLoginUiState {
    data object Idle : GitHubLoginUiState()
    data object RequestingCode : GitHubLoginUiState()
    data class WaitingForUser(val info: DeviceCodeInfo) : GitHubLoginUiState()
    data object Success : GitHubLoginUiState()
    data class Error(val message: String) : GitHubLoginUiState()
}

@HiltViewModel
class GitHubLoginViewModel @Inject constructor(
    private val authRepository: GitHubAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GitHubLoginUiState>(GitHubLoginUiState.Idle)
    val uiState: StateFlow<GitHubLoginUiState> = _uiState.asStateFlow()

    fun startLogin() {
        viewModelScope.launch {
            _uiState.value = GitHubLoginUiState.RequestingCode
            when (val result = authRepository.requestDeviceCode()) {
                is DevoraResult.Success -> {
                    _uiState.value = GitHubLoginUiState.WaitingForUser(result.data)
                    pollForToken(result.data)
                }
                is DevoraResult.Failure -> _uiState.value = GitHubLoginUiState.Error(result.message)
            }
        }
    }

    private suspend fun pollForToken(info: DeviceCodeInfo) {
        var intervalSeconds = info.pollIntervalSeconds
        var elapsedSeconds = 0

        while (elapsedSeconds < info.expiresInSeconds) {
            delay(intervalSeconds * 1000L)
            elapsedSeconds += intervalSeconds

            when (val result = authRepository.pollForToken(info.deviceCode)) {
                is DeviceFlowPollResult.Success -> {
                    _uiState.value = GitHubLoginUiState.Success
                    return
                }
                is DeviceFlowPollResult.AuthorizationPending -> { /* keep polling */ }
                is DeviceFlowPollResult.SlowDown -> intervalSeconds += 5
                is DeviceFlowPollResult.ExpiredToken -> {
                    _uiState.value = GitHubLoginUiState.Error("The login code expired. Try again.")
                    return
                }
                is DeviceFlowPollResult.AccessDenied -> {
                    _uiState.value = GitHubLoginUiState.Error("Login was denied.")
                    return
                }
                is DeviceFlowPollResult.OtherError -> {
                    _uiState.value = GitHubLoginUiState.Error(result.message)
                    return
                }
            }
        }
        _uiState.value = GitHubLoginUiState.Error("Login timed out.")
    }
}