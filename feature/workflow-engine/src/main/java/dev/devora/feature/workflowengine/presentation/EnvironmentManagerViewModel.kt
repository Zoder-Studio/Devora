package dev.devora.feature.workflowengine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowEnvironmentInfo
import dev.devora.feature.workflowengine.domain.repository.WorkflowEnvironmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnvironmentManagerUiState(
    val info: WorkflowEnvironmentInfo? = null,
    val isBusy: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EnvironmentManagerViewModel @Inject constructor(
    private val repository: WorkflowEnvironmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnvironmentManagerUiState())
    val uiState: StateFlow<EnvironmentManagerUiState> = _uiState.asStateFlow()

    fun load(workflowId: String) {
        _uiState.value = EnvironmentManagerUiState(info = repository.getInfo(workflowId))
    }

    fun reset(workflowId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = repository.resetEnvironment(workflowId)) {
                is DevoraResult.Success -> _uiState.value = EnvironmentManagerUiState(
                    info = repository.getInfo(workflowId)
                )
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun delete(workflowId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = repository.deleteEnvironment(workflowId)) {
                is DevoraResult.Success -> _uiState.value = EnvironmentManagerUiState(
                    info = repository.getInfo(workflowId)
                )
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = result.message
                )
            }
        }
    }
}