package dev.devora.feature.workflowengine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.workflowengine.domain.model.WorkflowRunResult
import dev.devora.feature.workflowengine.domain.repository.WorkflowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkflowRunUiState(
    val isRunning: Boolean = false,
    val runResult: WorkflowRunResult? = null
)

@HiltViewModel
class WorkflowRunViewModel @Inject constructor(
    private val repository: WorkflowRepository,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowRunUiState())
    val uiState: StateFlow<WorkflowRunUiState> = _uiState.asStateFlow()

    fun run(projectRootPath: String, workflowFilePath: String, jobId: String) {
        viewModelScope.launch {
            _uiState.value = WorkflowRunUiState(isRunning = true)
            when (val parseResult = repository.parseWorkflow(workflowFilePath)) {
                is DevoraResult.Success -> {
                    repository.runJob(projectRootPath, parseResult.data, jobId).collect { result ->
                        _uiState.value = WorkflowRunUiState(
                            isRunning = result.stepResults.size < (parseResult.data.jobs.find { it.id == jobId }?.steps?.size ?: 0),
                            runResult = result
                        )
                    }
                    _uiState.value = _uiState.value.copy(isRunning = false)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = WorkflowRunUiState(isRunning = false)
                    snackbarController.show("Error: ${parseResult.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}