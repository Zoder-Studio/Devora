package dev.devora.feature.buildsystem.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
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
    val logTail: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BuildScreenViewModel @Inject constructor(
    private val runGradleTaskUseCase: RunGradleTaskUseCase,
    private val readBuildLogTailUseCase: ReadBuildLogTailUseCase,
    private val exportBuildLogUseCase: ExportBuildLogUseCase
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
                }
                is DevoraResult.Failure -> _uiState.value = BuildScreenUiState(
                    isRunning = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun exportLog(destinationFile: File) {
        val run = _uiState.value.currentRun ?: return
        when (val result = exportBuildLogUseCase(run, destinationFile)) {
            is DevoraResult.Success -> { /* UI can show a confirmation via Snackbar in polish pass */ }
            is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
        }
    }
}