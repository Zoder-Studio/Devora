package dev.devora.feature.accountsecurity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.accountsecurity.domain.model.RevokeStage
import dev.devora.feature.accountsecurity.domain.usecase.RevokeLocalDpatUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyRevokeUiState(
    val stage: RevokeStage = RevokeStage.IDLE,
    val secondsRemaining: Int = 10,
    val errorMessage: String? = null
)

@HiltViewModel
class EmergencyRevokeViewModel @Inject constructor(
    private val revokeLocalDpatUseCase: RevokeLocalDpatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyRevokeUiState())
    val uiState: StateFlow<EmergencyRevokeUiState> = _uiState.asStateFlow()

    fun startCountdown() {
        viewModelScope.launch {
            _uiState.value = EmergencyRevokeUiState(stage = RevokeStage.COUNTDOWN, secondsRemaining = 10)
            for (remaining in 9 downTo 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(secondsRemaining = remaining)
            }
        }
    }

    fun confirmRevoke() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stage = RevokeStage.REVOKING)
            when (val result = revokeLocalDpatUseCase()) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(stage = RevokeStage.REVOKED)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    stage = RevokeStage.IDLE,
                    errorMessage = result.message
                )
            }
        }
    }

    fun cancel() {
        _uiState.value = EmergencyRevokeUiState()
    }
}