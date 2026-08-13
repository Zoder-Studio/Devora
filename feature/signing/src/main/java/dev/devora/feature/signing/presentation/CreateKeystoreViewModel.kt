package dev.devora.feature.signing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.usecase.CreateKeystoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateKeystoreUiState(
    val isCreating: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val createdEntry: KeystoreEntry? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CreateKeystoreViewModel @Inject constructor(
    private val createKeystoreUseCase: CreateKeystoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateKeystoreUiState())
    val uiState: StateFlow<CreateKeystoreUiState> = _uiState.asStateFlow()

    fun create(request: KeystoreCreationRequest, destinationDir: String) {
        viewModelScope.launch {
            _uiState.value = CreateKeystoreUiState(isCreating = true)
            val result = createKeystoreUseCase(request, destinationDir) { line ->
                _uiState.update { it.copy(outputLines = it.outputLines + line) }
            }
            _uiState.update { current ->
                when (result) {
                    is DevoraResult.Success -> current.copy(isCreating = false, createdEntry = result.data)
                    is DevoraResult.Failure -> current.copy(isCreating = false, errorMessage = result.message)
                }
            }
        }
    }
}