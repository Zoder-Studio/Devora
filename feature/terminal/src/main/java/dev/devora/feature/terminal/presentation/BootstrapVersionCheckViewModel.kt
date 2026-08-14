package dev.devora.feature.terminal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.domain.model.BootstrapVersionCheckResult
import dev.devora.feature.terminal.domain.repository.BootstrapVersionCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BootstrapVersionUiState(
    val isChecking: Boolean = false,
    val isUpdating: Boolean = false,
    val checkResult: BootstrapVersionCheckResult? = null,
    val progressMessage: String? = null
)

@HiltViewModel
class BootstrapVersionCheckViewModel @Inject constructor(
    private val checkRepository: BootstrapVersionCheckRepository,
    private val prefixManager: EmbeddedPrefixManager,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(BootstrapVersionUiState())
    val uiState: StateFlow<BootstrapVersionUiState> = _uiState.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true)
            when (val result = checkRepository.checkForUpdate()) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(isChecking = false, checkResult = result.data)
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isChecking = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun applyUpdate(newReleaseTag: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            val result = prefixManager.reinstallWithVersion(newReleaseTag) { progress ->
                _uiState.value = _uiState.value.copy(progressMessage = progress)
            }
            _uiState.value = when (result) {
                is DevoraResult.Success -> _uiState.value.copy(isUpdating = false, progressMessage = null)
                is DevoraResult.Failure -> {
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                    _uiState.value.copy(isUpdating = false)
                }
            }
        }
    }
}