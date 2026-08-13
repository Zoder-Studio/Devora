package dev.devora.feature.terminal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.terminal.TerminalSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.data.embedded.EmbeddedSessionFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EmbeddedTerminalUiState {
    data object Preparing : EmbeddedTerminalUiState()
    data class PreparingProgress(val message: String) : EmbeddedTerminalUiState()
    data class Ready(val session: TerminalSession) : EmbeddedTerminalUiState()
    data class Error(val message: String) : EmbeddedTerminalUiState()
}

@HiltViewModel
class EmbeddedTerminalViewModel @Inject constructor(
    private val prefixManager: EmbeddedPrefixManager,
    private val sessionFactory: EmbeddedSessionFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmbeddedTerminalUiState>(EmbeddedTerminalUiState.Preparing)
    val uiState: StateFlow<EmbeddedTerminalUiState> = _uiState.asStateFlow()

    fun start(
        workingDirectory: String,
        client: com.termux.terminal.TerminalSessionClient,
        initialCommand: String? = null
    ) {
        viewModelScope.launch {
            if (!prefixManager.isPrepared()) {
                val prepareResult = prefixManager.prepare { progress ->
                    _uiState.value = EmbeddedTerminalUiState.PreparingProgress(progress)
                }
                if (prepareResult is DevoraResult.Failure) {
                    _uiState.value = EmbeddedTerminalUiState.Error(prepareResult.message)
                    return@launch
                }
            }

            when (val result = sessionFactory.createSession(workingDirectory, client, initialCommand)) {
                is DevoraResult.Success -> _uiState.value = EmbeddedTerminalUiState.Ready(result.data)
                is DevoraResult.Failure -> _uiState.value = EmbeddedTerminalUiState.Error(result.message)
            }
        }
    }
}