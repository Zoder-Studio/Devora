package dev.devora.feature.terminal.data.embedded

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dev.devora.core.common.result.DevoraResult
import kotlinx.coroutines.delay

/**
 * Runs a shell command inside the embedded engine's own prefix and
 * waits for it to finish, streaming output lines as they appear.
 * Extracted as a shared utility so every feature module that needs to
 * run a background embedded command (nano setup, SDK installation,
 * Gradle builds, etc.) does not duplicate this session/polling logic.
 */
class EmbeddedCommandRunner(
    private val prefixManager: EmbeddedPrefixManager,
    private val sessionFactory: EmbeddedSessionFactory
) {
    suspend fun run(
        script: String,
        workingDirectory: String = prefixManager.prefixPath,
        timeoutMillis: Long = 300_000L,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        if (!prefixManager.isPrepared()) {
            return DevoraResult.Failure(message = "Embedded engine is not prepared.")
        }

        return try {
            val outputBuilder = StringBuilder()
            var finished = false
            var succeeded = false

            val client = object : TerminalSessionClient {
                override fun onTextChanged(changedSession: TerminalSession) {
                    val transcript = changedSession.emulator?.screen?.transcriptText.orEmpty()
                    if (transcript.length > outputBuilder.length) {
                        val newText = transcript.substring(outputBuilder.length)
                        outputBuilder.append(newText)
                        newText.lines().filter { it.isNotBlank() }.forEach(onOutputLine)
                    }
                }
                override fun onTitleChanged(changedSession: TerminalSession) {}
                override fun onSessionFinished(finishedSession: TerminalSession) {
                    finished = true
                    succeeded = finishedSession.exitStatus == 0
                }
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

            val sessionResult = sessionFactory.createSession(workingDirectory, client, script)
            if (sessionResult is DevoraResult.Failure) {
                return DevoraResult.Failure(message = sessionResult.message, cause = sessionResult.cause)
            }

            val pollIntervalMillis = 200L
            var waited = 0L
            while (!finished && waited < timeoutMillis) {
                delay(pollIntervalMillis)
                waited += pollIntervalMillis
            }

            if (!finished) {
                return DevoraResult.Failure(message = "Timed out running script:\n$script")
            }
            if (!succeeded) {
                return DevoraResult.Failure(
                    message = "Script failed. Raw output:\n$outputBuilder",
                    rawOutput = outputBuilder.toString()
                )
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to run embedded script", cause = e)
        }
    }
}