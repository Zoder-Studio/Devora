package dev.devora.feature.sdkmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.domain.model.SdkComponent
import dev.devora.feature.sdkmanager.domain.usecase.InstallSdkPackageUseCase
import dev.devora.feature.sdkmanager.domain.usecase.ListSdkPackagesUseCase
import dev.devora.feature.sdkmanager.domain.usecase.SetupSdkToolingUseCase
import dev.devora.feature.sdkmanager.domain.usecase.UninstallSdkPackageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SdkManagerUiState(
    val isBusy: Boolean = false,
    val statusLines: List<String> = emptyList(),
    val installed: List<SdkComponent> = emptyList(),
    val available: List<SdkComponent> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class SdkManagerViewModel @Inject constructor(
    private val setupSdkToolingUseCase: SetupSdkToolingUseCase,
    private val listSdkPackagesUseCase: ListSdkPackagesUseCase,
    private val installSdkPackageUseCase: InstallSdkPackageUseCase,
    private val uninstallSdkPackageUseCase: UninstallSdkPackageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SdkManagerUiState())
    val uiState: StateFlow<SdkManagerUiState> = _uiState.asStateFlow()

    fun setupAndRefresh() {
        viewModelScope.launch {
            _uiState.value = SdkManagerUiState(isBusy = true)
            val setupResult = setupSdkToolingUseCase { line -> appendLine(line) }
            if (setupResult is DevoraResult.Failure) {
                _uiState.value = _uiState.value.copy(isBusy = false, errorMessage = setupResult.message)
                return@launch
            }
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            when (val result = listSdkPackagesUseCase { line -> appendLine(line) }) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    installed = result.data.installed,
                    available = result.data.available
                )
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun install(packagePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = installSdkPackageUseCase(packagePath) { line -> appendLine(line) }) {
                is DevoraResult.Success -> refresh()
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun uninstall(packagePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = uninstallSdkPackageUseCase(packagePath) { line -> appendLine(line) }) {
                is DevoraResult.Success -> refresh()
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun appendLine(line: String) {
        _uiState.value = _uiState.value.copy(statusLines = _uiState.value.statusLines + line)
    }
}