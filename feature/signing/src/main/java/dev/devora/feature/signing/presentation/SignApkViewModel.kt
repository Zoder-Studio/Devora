package dev.devora.feature.signing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.snackbar.DevoraSnackbarSeverity
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.model.SigningRequest
import dev.devora.feature.signing.domain.usecase.ListKeystoresUseCase
import dev.devora.feature.signing.domain.usecase.SignApkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignApkUiState(
    val keystores: List<KeystoreEntry> = emptyList(),
    val isSigning: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val signedApkPath: String? = null
)

@HiltViewModel
class SignApkViewModel @Inject constructor(
    private val listKeystoresUseCase: ListKeystoresUseCase,
    private val signApkUseCase: SignApkUseCase,
    private val snackbarController: DevoraSnackbarController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignApkUiState())
    val uiState: StateFlow<SignApkUiState> = _uiState.asStateFlow()

    fun loadKeystores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(keystores = listKeystoresUseCase())
        }
    }

    fun sign(apkFilePath: String, keystoreId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigning = true, outputLines = emptyList())

            val outputApkPath = apkFilePath.substringBeforeLast(".apk") + "-signed.apk"
            val request = SigningRequest(
                apkFilePath = apkFilePath,
                outputApkFilePath = outputApkPath,
                keystoreId = keystoreId
            )

            val result = signApkUseCase(request) { line ->
                _uiState.value = _uiState.value.copy(outputLines = _uiState.value.outputLines + line)
            }

            when (result) {
                is DevoraResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSigning = false, signedApkPath = outputApkPath)
                    snackbarController.show("APK signed", DevoraSnackbarSeverity.SUCCESS)
                }
                is DevoraResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isSigning = false)
                    snackbarController.show("Error: ${result.message}", DevoraSnackbarSeverity.ERROR)
                }
            }
        }
    }
}