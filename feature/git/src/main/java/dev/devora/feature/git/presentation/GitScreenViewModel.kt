package dev.devora.feature.git.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.git.domain.model.GitStatusResult
import dev.devora.feature.git.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GitScreenUiState(
    val status: GitStatusResult? = null,
    val isBusy: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class GitScreenViewModel @Inject constructor(
    private val statusUseCase: GitStatusUseCase,
    private val addUseCase: GitAddUseCase,
    private val unstageUseCase: GitUnstageUseCase,
    private val commitUseCase: GitCommitUseCase,
    private val pushUseCase: GitPushUseCase,
    private val pullUseCase: GitPullUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GitScreenUiState())
    val uiState: StateFlow<GitScreenUiState> = _uiState.asStateFlow()

    private var projectRootPath: String = ""

    fun load(rootPath: String) {
        projectRootPath = rootPath
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = statusUseCase(projectRootPath)) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(status = result.data, errorMessage = null)
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun stage(path: String) {
        viewModelScope.launch {
            addUseCase(projectRootPath, listOf(path))
            refresh()
        }
    }

    fun unstage(path: String) {
        viewModelScope.launch {
            unstageUseCase(projectRootPath, listOf(path))
            refresh()
        }
    }

    fun commit(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = commitUseCase(projectRootPath, message)) {
                is DevoraResult.Success -> {
                    _uiState.value = _uiState.value.copy(isBusy = false)
                    refresh()
                }
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(isBusy = false, errorMessage = result.message)
            }
        }
    }

    fun push(remote: String, branch: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, outputLines = emptyList())
            val result = pushUseCase(projectRootPath, remote, branch) { line ->
                _uiState.value = _uiState.value.copy(outputLines = _uiState.value.outputLines + line)
            }
            _uiState.value = when (result) {
                is DevoraResult.Success -> _uiState.value.copy(isBusy = false)
                is DevoraResult.Failure -> _uiState.value.copy(isBusy = false, errorMessage = result.message)
            }
        }
    }

    fun pull(remote: String, branch: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, outputLines = emptyList())
            val result = pullUseCase(projectRootPath, remote, branch) { line ->
                _uiState.value = _uiState.value.copy(outputLines = _uiState.value.outputLines + line)
            }
            _uiState.value = when (result) {
                is DevoraResult.Success -> { refresh(); _uiState.value.copy(isBusy = false) }
                is DevoraResult.Failure -> _uiState.value.copy(isBusy = false, errorMessage = result.message)
            }
        }
    }
}