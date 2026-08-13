package dev.devora.feature.terminal.data.embedded

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dev.devora.core.common.result.DevoraResult

class EmbeddedSessionFactory(
    private val prefixManager: EmbeddedPrefixManager
) {
    fun createSession(
        workingDirectory: String,
        client: TerminalSessionClient,
        initialCommand: String? = null
    ): DevoraResult<TerminalSession> {
        if (!prefixManager.isPrepared()) {
            return DevoraResult.Failure(
                message = "Embedded engine is not prepared yet. Call prepare() first."
            )
        }

        return try {
            val env = arrayOf(
                "HOME=${prefixManager.prefixPath}/home",
                "PREFIX=${prefixManager.prefixPath}",
                "PATH=${prefixManager.prefixPath}/bin",
                "TERM=xterm-256color",
                "LANG=en_US.UTF-8"
            )
            java.io.File("${prefixManager.prefixPath}/home").mkdirs()

            val args = if (initialCommand != null) {
                arrayOf("-c", initialCommand)
            } else {
                arrayOf("-l")
            }

            val session = TerminalSession(
                prefixManager.bashBinaryPath,
                workingDirectory,
                args,
                env,
                2000,
                client
            )
            DevoraResult.Success(session)
        } catch (e: Exception) {
            DevoraResult.Failure(
                message = "Failed to start embedded terminal session",
                cause = e
            )
        }
    }
}