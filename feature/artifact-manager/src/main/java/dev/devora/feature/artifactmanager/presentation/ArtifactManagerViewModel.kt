package dev.devora.feature.artifactmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.artifactmanager.domain.install.ArtifactInstaller
import dev.devora.feature.artifactmanager.domain.model.Artifact
import dev.devora.feature.artifactmanager.domain.usecase.DeleteArtifactUseCase
import dev.devora.feature.artifactmanager.domain.usecase.ListArtifactsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtifactManagerUiState(
    val artifacts: List<Artifact> = emptyList(),
    val installingArtifactId: String? = null
)

@HiltViewModel
class ArtifactManagerViewModel @Inject constructor(
    private val listArtifactsUseCase: ListArtifactsUseCase,
    private val deleteArtifactUseCase: DeleteArtifactUseCase,
    private val installer: ArtifactInstaller,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtifactManagerUiState())
    val uiState: StateFlow<ArtifactManagerUiState> = _uiState.asStateFlow()

    fun load(projectRootPath: String) {
        when (val result = listArtifactsUseCase(projectRootPath)) {
            is DevoraResult.Success -> _uiState.value = ArtifactManagerUiState(artifacts = result.data)
            is DevoraResult.Failure -> {
                _uiState.value = ArtifactManagerUiState()
                viewModelScope.launch { snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR) }
            }
        }
    }

    fun install(artifact: Artifact) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(installingArtifactId = artifact.id)
            when (val result = installer.install(artifact)) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(installingArtifactId = null)
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(installingArtifactId = null)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }

    fun delete(artifact: Artifact, projectRootPath: String) {
        viewModelScope.launch {
            when (val result = deleteArtifactUseCase(artifact)) {
                is DevoraResult.Success -> load(projectRootPath)
                is DevoraResult.Failure -> snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
            }
        }
    }
}