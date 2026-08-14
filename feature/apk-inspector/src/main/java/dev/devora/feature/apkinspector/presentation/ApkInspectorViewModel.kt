package dev.devora.feature.apkinspector.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.apkinspector.domain.model.ApkInspectionResult
import dev.devora.feature.apkinspector.domain.usecase.InspectApkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApkInspectorUiState(
    val isLoading: Boolean = false,
    val result: ApkInspectionResult? = null
)

@HiltViewModel
class ApkInspectorViewModel @Inject constructor(
    private val inspectApkUseCase: InspectApkUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApkInspectorUiState())
    val uiState: StateFlow<ApkInspectorUiState> = _uiState.asStateFlow()

    fun inspect(apkFilePath: String) {
        viewModelScope.launch {
            _uiState.value = ApkInspectorUiState(isLoading = true)
            when (val result = inspectApkUseCase(apkFilePath)) {
                is DevoraResult.Success -> _uiState.value = ApkInspectorUiState(result = result.data)
                is DevoraResult.Failure -> {
                    _uiState.value = ApkInspectorUiState()
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}