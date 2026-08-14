package dev.devora.feature.secrets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.secrets.domain.model.SecretEntry
import dev.devora.feature.secrets.domain.usecase.AddSecretUseCase
import dev.devora.feature.secrets.domain.usecase.DeleteSecretUseCase
import dev.devora.feature.secrets.domain.usecase.ListSecretsUseCase
import dev.devora.feature.secrets.domain.usecase.PushSecretToGitHubUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecretsUiState(
    val secrets: List<SecretEntry> = emptyList(),
    val isBusy: Boolean = false
)

@HiltViewModel
class SecretsViewModel @Inject constructor(
    private val listSecretsUseCase: ListSecretsUseCase,
    private val addSecretUseCase: AddSecretUseCase,
    private val deleteSecretUseCase: DeleteSecretUseCase,
    private val pushSecretToGitHubUseCase: PushSecretToGitHubUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretsUiState())
    val uiState: StateFlow<SecretsUiState> = _uiState.asStateFlow()

    private var projectRootPath: String = ""

    fun load(rootPath: String) {
        projectRootPath = rootPath
        viewModelScope.launch {
            _uiState.value = SecretsUiState(secrets = listSecretsUseCase(rootPath))
        }
    }

    fun add(name: String, value: String) {
        viewModelScope.launch {
            when (val result = addSecretUseCase(projectRootPath, name, value)) {
                is DevoraResult.Success -> load(projectRootPath)
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }

    fun delete(secretId: String) {
        viewModelScope.launch {
            deleteSecretUseCase(secretId)
            load(projectRootPath)
        }
    }

    fun pushToGitHub(secretId: String, owner: String, repo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = pushSecretToGitHubUseCase(secretId, owner, repo)) {
                is DevoraResult.Success -> {
                    _uiState.value = _uiState.value.copy(isBusy = false)
                    snackbarController.show("Secret pushed to GitHub", DevoraSnackbarSeverity.SUCCESS)
                    load(projectRootPath)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isBusy = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}