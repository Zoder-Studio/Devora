package dev.devora.feature.github.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.github.domain.model.GitHubOrg
import dev.devora.feature.github.domain.repository.GitHubApiRepository
import dev.devora.feature.github.domain.usecase.PushToGitHubUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PushToGitHubUiState(
    val orgs: List<GitHubOrg> = emptyList(),
    val isBusy: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val success: Boolean = false
)

@HiltViewModel
class PushToGitHubViewModel @Inject constructor(
    private val apiRepository: GitHubApiRepository,
    private val pushToGitHubUseCase: PushToGitHubUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PushToGitHubUiState())
    val uiState: StateFlow<PushToGitHubUiState> = _uiState.asStateFlow()

    fun loadOrgs() {
        viewModelScope.launch {
            when (val result = apiRepository.listOrgs()) {
                is DevoraResult.Success -> _uiState.update { it.copy(orgs = result.data) }
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }

    fun createAndPush(projectRootPath: String, owner: String?, repoName: String, private: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, outputLines = emptyList()) }

            val createResult = apiRepository.createRepo(owner, repoName, private)
            if (createResult is DevoraResult.Failure) {
                _uiState.update { it.copy(isBusy = false) }
                snackbarController.show("Error: ${createResult.message}", DevoraSnackbarSeverity.ERROR)
                return@launch
            }
            val repo = (createResult as DevoraResult.Success).data

            val pushResult = pushToGitHubUseCase(projectRootPath, repo) { line ->
                _uiState.update { it.copy(outputLines = it.outputLines + line) }
            }

            when (pushResult) {
                is DevoraResult.Success -> {
                    _uiState.update { it.copy(isBusy = false, success = true) }
                    snackbarController.show("Pushed to GitHub", DevoraSnackbarSeverity.SUCCESS)
                }
                is DevoraResult.Failure -> {
                    _uiState.update { it.copy(isBusy = false) }
                    snackbarController.show("Error: ${pushResult.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}