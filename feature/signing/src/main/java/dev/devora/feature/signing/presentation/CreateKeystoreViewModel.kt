package dev.devora.feature.signing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.usecase.CreateKeystoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateKeystoreUiState(
    val isCreating: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val createdEntry: KeystoreEntry? = null
)

@HiltViewModel
class CreateKeystoreViewModel @Inject constructor(
    private val createKeystoreUseCase: CreateKeystoreUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateKeystoreUiState())
    val uiState: StateFlow<CreateKeystoreUiState> = _uiState.asStateFlow()

    fun create(request: KeystoreCreationRequest, destinationDir: String) {
        viewModelScope.launch {
            _uiState.value = CreateKeystoreUiState(isCreating = true)
            val result = createKeystoreUseCase(request, destinationDir) { line ->
                _uiState.value = _uiState.value.copy(outputLines = _uiState.value.outputLines + line)
            }
            when (result) {
                is DevoraResult.Success -> {
                    _uiState.value = _uiState.value.copy(isCreating = false, createdEntry = result.data)
                    snackbarController.show("Keystore created", DevoraSnackbarSeverity.SUCCESS)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}