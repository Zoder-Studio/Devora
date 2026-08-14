package dev.devora.feature.gradlemanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.gradlemanager.domain.model.GradleCacheInfo
import dev.devora.feature.gradlemanager.domain.model.GradleWrapperInfo
import dev.devora.feature.gradlemanager.domain.usecase.ClearGradleCacheUseCase
import dev.devora.feature.gradlemanager.domain.usecase.GetGradleCacheInfoUseCase
import dev.devora.feature.gradlemanager.domain.usecase.GetGradleDaemonStatusUseCase
import dev.devora.feature.gradlemanager.domain.usecase.ReadGradleWrapperInfoUseCase
import dev.devora.feature.gradlemanager.domain.usecase.SetGradleOfflineModeUseCase
import dev.devora.feature.gradlemanager.domain.usecase.StopGradleDaemonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GradleManagerUiState(
    val wrapperInfo: GradleWrapperInfo? = null,
    val cacheInfo: GradleCacheInfo? = null,
    val isOfflineMode: Boolean = false,
    val statusLines: List<String> = emptyList(),
    val isBusy: Boolean = false
)

@HiltViewModel
class GradleManagerViewModel @Inject constructor(
    private val readWrapperInfoUseCase: ReadGradleWrapperInfoUseCase,
    private val getCacheInfoUseCase: GetGradleCacheInfoUseCase,
    private val clearCacheUseCase: ClearGradleCacheUseCase,
    private val getDaemonStatusUseCase: GetGradleDaemonStatusUseCase,
    private val stopDaemonUseCase: StopGradleDaemonUseCase,
    private val setOfflineModeUseCase: SetGradleOfflineModeUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradleManagerUiState())
    val uiState: StateFlow<GradleManagerUiState> = _uiState.asStateFlow()

    private var projectRootPath: String = ""

    fun load(rootPath: String) {
        projectRootPath = rootPath
        viewModelScope.launch {
            when (val result = readWrapperInfoUseCase(rootPath)) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(wrapperInfo = result.data)
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
            when (val cacheResult = getCacheInfoUseCase()) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(cacheInfo = cacheResult.data)
                is DevoraResult.Failure -> { /* non-fatal, cache info is best-effort */ }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = clearCacheUseCase()) {
                is DevoraResult.Success -> {
                    val cacheResult = getCacheInfoUseCase()
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        cacheInfo = (cacheResult as? DevoraResult.Success)?.data
                    )
                    snackbarController.show("Cache cleared", DevoraSnackbarSeverity.SUCCESS)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isBusy = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun checkDaemonStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, statusLines = emptyList())
            val result = getDaemonStatusUseCase(projectRootPath) { line ->
                _uiState.value = _uiState.value.copy(statusLines = _uiState.value.statusLines + line)
            }
            _uiState.value = _uiState.value.copy(isBusy = false)
            if (result is DevoraResult.Failure) {
                snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }

    fun stopDaemon() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            val result = stopDaemonUseCase(projectRootPath) { line ->
                _uiState.value = _uiState.value.copy(statusLines = _uiState.value.statusLines + line)
            }
            _uiState.value = _uiState.value.copy(isBusy = false)
            if (result is DevoraResult.Failure) {
                snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isOfflineMode
            when (val result = setOfflineModeUseCase(projectRootPath, newValue)) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(isOfflineMode = newValue)
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }
}