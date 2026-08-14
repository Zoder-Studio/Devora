package dev.devora.feature.buildsystem.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.buildsystem.domain.model.BuildRun
import dev.devora.feature.buildsystem.domain.usecase.ExportBuildLogUseCase
import dev.devora.feature.buildsystem.domain.usecase.ReadBuildLogTailUseCase
import dev.devora.feature.buildsystem.domain.usecase.RunGradleTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BuildScreenUiState(
    val isRunning: Boolean = false,
    val currentRun: BuildRun? = null,
    val logTail: List<String> = emptyList()
)

@HiltViewModel
class BuildScreenViewModel @Inject constructor(
    private val runGradleTaskUseCase: RunGradleTaskUseCase,
    private val readBuildLogTailUseCase: ReadBuildLogTailUseCase,
    private val exportBuildLogUseCase: ExportBuildLogUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuildScreenUiState())
    val uiState: StateFlow<BuildScreenUiState> = _uiState.asStateFlow()

    fun runTask(projectRootPath: String, gradleTask: String) {
        viewModelScope.launch {
            _uiState.value = BuildScreenUiState(isRunning = true)
            when (val result = runGradleTaskUseCase(projectRootPath, gradleTask)) {
                is DevoraResult.Success -> {
                    val run = result.data
                    _uiState.value = BuildScreenUiState(
                        isRunning = false,
                        currentRun = run,
                        logTail = readBuildLogTailUseCase(run)
                    )
                    snackbarController.show(
                        text = if (run.status.name == "SUCCESS") "Build succeeded" else "Build failed",
                        severity = if (run.status.name == "SUCCESS") DevoraSnackbarSeverity.SUCCESS else DevoraSnackbarSeverity.ERROR
                    )
                }
                is DevoraResult.Failure -> {
                    _uiState.value = BuildScreenUiState(isRunning = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun exportLog(destinationFile: File) {
        val run = _uiState.value.currentRun ?: return
        viewModelScope.launch {
            when (val result = exportBuildLogUseCase(run, destinationFile)) {
                is DevoraResult.Success -> snackbarController.show("Log exported", DevoraSnackbarSeverity.SUCCESS)
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }
}