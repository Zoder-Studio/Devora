package dev.devora.feature.workflowengine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowPermissionEntry
import dev.devora.feature.workflowengine.domain.repository.WorkflowPermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkflowPermissionsUiState(
    val entries: List<WorkflowPermissionEntry> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class WorkflowPermissionsViewModel @Inject constructor(
    private val repository: WorkflowPermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowPermissionsUiState())
    val uiState: StateFlow<WorkflowPermissionsUiState> = _uiState.asStateFlow()

    fun load(projectRootPath: String) {
        viewModelScope.launch {
            _uiState.value = WorkflowPermissionsUiState(entries = repository.listForProject(projectRootPath))
        }
    }

    fun setPermission(projectRootPath: String, workflowId: String, permission: WorkflowPermission) {
        viewModelScope.launch {
            when (val result = repository.set(projectRootPath, workflowId, permission)) {
                is DevoraResult.Success -> load(projectRootPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun removePermission(projectRootPath: String, workflowId: String) {
        viewModelScope.launch {
            when (val result = repository.remove(projectRootPath, workflowId)) {
                is DevoraResult.Success -> load(projectRootPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}