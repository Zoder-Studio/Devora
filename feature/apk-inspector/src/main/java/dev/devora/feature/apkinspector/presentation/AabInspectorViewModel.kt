package dev.devora.feature.apkinspector.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.apkinspector.domain.usecase.InspectAabUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AabInspectorUiState(
    val isLoading: Boolean = false,
    val lines: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AabInspectorViewModel @Inject constructor(
    private val inspectAabUseCase: InspectAabUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AabInspectorUiState())
    val uiState: StateFlow<AabInspectorUiState> = _uiState.asStateFlow()

    fun inspect(aabFilePath: String) {
        viewModelScope.launch {
            _uiState.value = AabInspectorUiState(isLoading = true)
            val result = inspectAabUseCase(aabFilePath) { line ->
                _uiState.update { it.copy(lines = it.lines + line) }
            }
            _uiState.update { current ->
                when (result) {
                    is DevoraResult.Success -> current.copy(isLoading = false)
                    is DevoraResult.Failure -> current.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}