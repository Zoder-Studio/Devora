package dev.devora.feature.accountsecurity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.accountsecurity.domain.model.Dpat
import dev.devora.feature.accountsecurity.domain.model.DpatExpirationOption
import dev.devora.feature.accountsecurity.domain.usecase.CreateDpatUseCase
import dev.devora.feature.accountsecurity.domain.usecase.IsCrossDeviceEnforcementAvailableUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateDpatUiState(
    val isCreating: Boolean = false,
    val createdDpat: Dpat? = null,
    val crossDeviceEnforcementAvailable: Boolean = false
)

@HiltViewModel
class CreateDpatViewModel @Inject constructor(
    private val createDpatUseCase: CreateDpatUseCase,
    private val isCrossDeviceEnforcementAvailableUseCase: IsCrossDeviceEnforcementAvailableUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateDpatUiState(crossDeviceEnforcementAvailable = isCrossDeviceEnforcementAvailableUseCase())
    )
    val uiState: StateFlow<CreateDpatUiState> = _uiState.asStateFlow()

    fun create(expiration: DpatExpirationOption, phoneLabel: String?, makePrimary: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            when (val result = createDpatUseCase(expiration, phoneLabel, makePrimary)) {
                is DevoraResult.Success -> {
                    _uiState.value = _uiState.value.copy(isCreating = false, createdDpat = result.data)
                    snackbarController.show("DPAT created", DevoraSnackbarSeverity.SUCCESS)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}