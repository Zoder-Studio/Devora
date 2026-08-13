package dev.devora.feature.editor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.editor.domain.usecase.SetupNanoEmbeddedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NanoInstallUiState(
    val isInstalling: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class NanoInstallViewModel @Inject constructor(
    private val setupNanoEmbeddedUseCase: SetupNanoEmbeddedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NanoInstallUiState())
    val uiState: StateFlow<NanoInstallUiState> = _uiState.asStateFlow()

    fun install(filePath: String, onInstalled: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = NanoInstallUiState(isInstalling = true)
            val result = setupNanoEmbeddedUseCase { line ->
                _uiState.value = _uiState.value.copy(
                    outputLines = _uiState.value.outputLines + line
                )
            }
            when (result) {
                is DevoraResult.Success -> onInstalled(filePath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isInstalling = false,
                    errorMessage = result.message
                )
            }
        }
    }
}