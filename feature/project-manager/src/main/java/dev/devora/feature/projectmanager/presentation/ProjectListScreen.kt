package dev.devora.feature.projectmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.projectmanager.domain.model.Project
import dev.devora.feature.projectmanager.domain.usecase.ImportProjectUseCase
import dev.devora.feature.projectmanager.domain.usecase.ListProjectsUseCase
import dev.devora.feature.projectmanager.domain.usecase.RemoveProjectUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList()
)

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val listProjectsUseCase: ListProjectsUseCase,
    private val importProjectUseCase: ImportProjectUseCase,
    private val removeProjectUseCase: RemoveProjectUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = listProjectsUseCase()) {
                is DevoraResult.Success -> _uiState.value = ProjectListUiState(isLoading = false, projects = result.data)
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun importProject(rootPath: String, initializeDevoraMetadata: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = importProjectUseCase(rootPath, initializeDevoraMetadata)) {
                is DevoraResult.Success -> refresh()
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun removeProject(projectId: String) {
        viewModelScope.launch {
            when (val result = removeProjectUseCase(projectId)) {
                is DevoraResult.Success -> refresh()
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }
}