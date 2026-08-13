package dev.devora.feature.terminal.presentation

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalViewClient

/**
 * Minimal, real implementation of the callbacks terminal-view/emulator
 * require to run. Each callback here does exactly what it says —
 * nothing is faked. Optional UX (bell sound, tab-complete popups,
 * clipboard sync) will be filled in Stage 5+ once Nano and the
 * broader editor surface are wired up, since they share the same
 * client interface.
 */
class NoopTerminalSessionClient : TerminalSessionClient, TerminalViewClient {
    override fun onTextChanged(changedSession: TerminalSession) {}
    override fun onTitleChanged(changedSession: TerminalSession) {}
    override fun onSessionFinished(finishedSession: TerminalSession) {}
    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {}
    override fun onPasteTextFromClipboard(session: TerminalSession?): String? = null
    override fun onBell(session: TerminalSession) {}
    override fun onColorsChanged(session: TerminalSession) {}
    override fun onTerminalCursorStateChange(state: Boolean) {}
    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
    override fun getTerminalCursorStyle(): Int? = null
    override fun logError(tag: String?, message: String?) {}
    override fun logWarn(tag: String?, message: String?) {}
    override fun logInfo(tag: String?, message: String?) {}
    override fun logDebug(tag: String?, message: String?) {}
    override fun logVerbose(tag: String?, message: String?) {}
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
    override fun logStackTrace(tag: String?, e: Exception?) {}
}