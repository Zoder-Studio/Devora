package dev.devora.feature.signing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.model.SigningRequest
import dev.devora.feature.signing.domain.usecase.ListKeystoresUseCase
import dev.devora.feature.signing.domain.usecase.SignApkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignApkUiState(
    val keystores: List<KeystoreEntry> = emptyList(),
    val isSigning: Boolean = false,
    val outputLines: List<String> = emptyList(),
    val signedApkPath: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SignApkViewModel @Inject constructor(
    private val listKeystoresUseCase: ListKeystoresUseCase,
    private val signApkUseCase: SignApkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignApkUiState())
    val uiState: StateFlow<SignApkUiState> = _uiState.asStateFlow()

    fun loadKeystores() {
        viewModelScope.launch {
            _uiState.update { it.copy(keystores = listKeystoresUseCase()) }
        }
    }

    fun sign(apkFilePath: String, keystoreId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigning = true, outputLines = emptyList(), errorMessage = null) }

            val outputApkPath = apkFilePath.substringBeforeLast(".apk") + "-signed.apk"
            val request = SigningRequest(
                apkFilePath = apkFilePath,
                outputApkFilePath = outputApkPath,
                keystoreId = keystoreId
            )

            val result = signApkUseCase(request) { line ->
                _uiState.update { it.copy(outputLines = it.outputLines + line) }
            }

            _uiState.update { current ->
                when (result) {
                    is DevoraResult.Success -> current.copy(isSigning = false, signedApkPath = outputApkPath)
                    is DevoraResult.Failure -> current.copy(isSigning = false, errorMessage = result.message)
                }
            }
        }
    }
}