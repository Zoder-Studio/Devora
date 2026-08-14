package dev.devora.core.ui.snackbar

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

enum class DevoraSnackbarSeverity { INFO, SUCCESS, ERROR }

data class DevoraSnackbarMessage(
    val text: String,
    val severity: DevoraSnackbarSeverity = DevoraSnackbarSeverity.INFO
)

/**
 * App-wide Snackbar dispatch. Every feature module's ViewModel can
 * inject this and call show(...) instead of holding its own
 * "errorMessage: String?" UI-state field and rendering a raw Text in
 * error color — this was duplicated across nearly every screen from
 * Stage 2 onward. New screens should prefer this; existing screens
 * can be migrated incrementally without breaking anything (both
 * patterns can coexist).
 */
interface DevoraSnackbarController {
    val messages: SharedFlow<DevoraSnackbarMessage>
    suspend fun show(text: String, severity: DevoraSnackbarSeverity = DevoraSnackbarSeverity.INFO)
}

class DefaultDevoraSnackbarController : DevoraSnackbarController {
    private val _messages = MutableSharedFlow<DevoraSnackbarMessage>(extraBufferCapacity = 8)
    override val messages: SharedFlow<DevoraSnackbarMessage> = _messages
    override suspend fun show(text: String, severity: DevoraSnackbarSeverity) {
        _messages.emit(DevoraSnackbarMessage(text, severity))
    }
}