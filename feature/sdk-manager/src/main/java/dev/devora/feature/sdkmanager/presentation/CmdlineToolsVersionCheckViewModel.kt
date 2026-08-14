package dev.devora.feature.sdkmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.data.PinnedCmdlineToolsVersion
import dev.devora.feature.sdkmanager.data.PinnedCmdlineToolsVersionStore
import dev.devora.feature.sdkmanager.domain.model.CmdlineToolsVersionCheckResult
import dev.devora.feature.sdkmanager.domain.repository.CmdlineToolsVersionCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CmdlineToolsVersionUiState(
    val isChecking: Boolean = false,
    val isApplying: Boolean = false,
    val checkResult: CmdlineToolsVersionCheckResult? = null,
    val appliedMessage: String? = null
)

@HiltViewModel
class CmdlineToolsVersionCheckViewModel @Inject constructor(
    private val checkRepository: CmdlineToolsVersionCheckRepository,
    private val versionStore: PinnedCmdlineToolsVersionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CmdlineToolsVersionUiState())
    val uiState: StateFlow<CmdlineToolsVersionUiState> = _uiState.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, appliedMessage = null)
            when (val result = checkRepository.checkForUpdate()) {
                is DevoraResult.Success -> _uiState.value = _uiState.value.copy(
                    isChecking = false,
                    checkResult = result.data
                )
                is DevoraResult.Failure -> _uiState.value = _uiState.value.copy(
                    isChecking = false,
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                )
            }
        }
    }

    /**
     * Only updates the pinned version pointer — it does NOT re-run
     * setup itself. The next time setupSdkTooling() runs (which only
     * happens if cmdline-tools is not already installed, see
     * DefaultSdkRepository.isSdkToolingInstalled), it will pick up
     * this new pinned URL. If cmdline-tools is already installed, the
     * developer must remove the existing installation themselves
     * (Devora never deletes SDK tooling automatically) before the new
     * pin takes effect — this mirrors how gradle-wrapper.properties
     * changes only take effect on the next Gradle invocation.
     */
    fun applyPin(result: CmdlineToolsVersionCheckResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            val writeResult = versionStore.write(
                PinnedCmdlineToolsVersion(
                    downloadUrl = result.latestDownloadUrl,
                    revision = result.latestRevision
                )
            )
            _uiState.value = when (writeResult) {
                is DevoraResult.Success -> _uiState.value.copy(
                    isApplying = false,
                    appliedMessage = "Pinned to ${result.latestRevision}. " +
                        "Remove the existing cmdline-tools install to apply it."
                )
                is DevoraResult.Failure -> _uiState.value.copy(
                    isApplying = false,
                    snackbarController.show("Error: ${writeResult.message}", DevoraSnackbarSeverity.ERROR)
                )
            }
        }
    }
}