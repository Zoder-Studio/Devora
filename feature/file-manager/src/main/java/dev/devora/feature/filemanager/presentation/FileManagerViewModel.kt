package dev.devora.feature.filemanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.filemanager.domain.action.OpenFileInNanoAction
import dev.devora.feature.filemanager.domain.action.OpenTerminalAtPathAction
import dev.devora.feature.filemanager.domain.model.FileNode
import dev.devora.feature.filemanager.domain.model.FileNodeType
import dev.devora.feature.filemanager.domain.usecase.CreateDirectoryUseCase
import dev.devora.feature.filemanager.domain.usecase.CreateFileUseCase
import dev.devora.feature.filemanager.domain.usecase.DeleteFileUseCase
import dev.devora.feature.filemanager.domain.usecase.ListDirectoryUseCase
import dev.devora.feature.filemanager.domain.usecase.RenameFileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FileManagerUiState(
    val currentPath: String = "",
    val parentPath: String? = null,
    val visibleEntries: List<FileNode> = emptyList(),
    val showHiddenFiles: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val listDirectoryUseCase: ListDirectoryUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val createDirectoryUseCase: CreateDirectoryUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val openTerminalAtPathAction: OpenTerminalAtPathAction,
    private val openFileInNanoAction: OpenFileInNanoAction
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    private var allEntries: List<FileNode> = emptyList()

    fun open(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = listDirectoryUseCase(path)) {
                is DevoraResult.Success -> {
                    allEntries = result.data.entries
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentPath = result.data.currentPath,
                        parentPath = result.data.parentPath,
                        visibleEntries = applyHiddenFilter(allEntries, _uiState.value.showHiddenFiles)
                    )
                }
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun onEntryClicked(entry: FileNode) {
        when (entry.type) {
            FileNodeType.DIRECTORY -> open(entry.absolutePath)
            FileNodeType.FILE, FileNodeType.SYMLINK -> openFileInNanoAction.openInNano(entry.absolutePath)
        }
    }

    fun openTerminalHere() {
        openTerminalAtPathAction.openTerminal(_uiState.value.currentPath)
    }

    fun toggleShowHiddenFiles() {
        val newValue = !_uiState.value.showHiddenFiles
        _uiState.value = _uiState.value.copy(
            showHiddenFiles = newValue,
            visibleEntries = applyHiddenFilter(allEntries, newValue)
        )
    }

    fun createFile(name: String) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            when (val result = createFileUseCase(currentPath, name)) {
                is DevoraResult.Success -> open(currentPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            when (val result = createDirectoryUseCase(currentPath, name)) {
                is DevoraResult.Success -> open(currentPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun rename(path: String, newName: String) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            when (val result = renameFileUseCase(path, newName)) {
                is DevoraResult.Success -> open(currentPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun delete(path: String) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            when (val result = deleteFileUseCase(path)) {
                is DevoraResult.Success -> open(currentPath)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    private fun applyHiddenFilter(entries: List<FileNode>, showHidden: Boolean): List<FileNode> =
        if (showHidden) entries else entries.filterNot { it.isHidden }
}