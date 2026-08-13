package dev.devora.feature.editor.data.repository

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.editor.data.NanoContract
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.data.embedded.EmbeddedSessionFactory
import dev.devora.feature.editor.domain.model.NanoLaunchTarget
import dev.devora.feature.editor.domain.repository.NanoRepository
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.model.TerminalEngineMode
import kotlinx.coroutines.delay
import java.io.File

class DefaultNanoRepository(
    private val engineSelector: TerminalEngineSelector,
    private val prefixManager: EmbeddedPrefixManager,
    private val embeddedSessionFactory: EmbeddedSessionFactory
) : NanoRepository {

    override fun resolveLaunchTarget(filePath: String): NanoLaunchTarget {
        return when (engineSelector.selectMode()) {
            TerminalEngineMode.TERMUX_APP -> NanoLaunchTarget.TermuxAppWindow(filePath)
            TerminalEngineMode.EMBEDDED_BOOTSTRAP -> {
                if (!prefixManager.isPrepared()) {
                    return NanoLaunchTarget.Unavailable(
                        "Embedded engine is not set up yet. Open the terminal once first."
                    )
                }
                val nanoBinary = File(prefixManager.prefixPath, NanoContract.NANO_BINARY_RELATIVE_PATH)
                if (nanoBinary.exists()) {
                    NanoLaunchTarget.EmbeddedSession(filePath)
                } else {
                    NanoLaunchTarget.EmbeddedNeedsInstall(filePath)
                }
            }
        }
    }

    override suspend fun installNanoInEmbeddedEngine(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        writeAptSourcesIfMissing()
        val packages = (listOf("nano", "curl") + NanoContract.SYNTAX_INSTALL_DEPENDENCIES).distinct()
        val installCommand = "apt update && apt install -y ${packages.joinToString(" ")}"
        onOutputLine("Running: $installCommand")
        return commandRunner.run(installCommand, onOutputLine = onOutputLine)
    }

    override suspend fun configureSyntaxHighlightingEmbedded(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        val dependenciesToInstall = NanoContract.SYNTAX_INSTALL_DEPENDENCIES.joinToString(" ")
        val script = buildString {
            append("apt install -y $dependenciesToInstall && ")
            append("curl -fsSL '${NanoContract.SYNTAX_INSTALL_SCRIPT_URL}' -o /tmp/install-nanorc.sh && ")
            append("bash /tmp/install-nanorc.sh && ")
            append("(grep -q 'set linenumbers' ~/.nanorc || echo 'set linenumbers' >> ~/.nanorc)")
        }
        return commandRunner.run(script, onOutputLine = onOutputLine)
    }

    private fun writeAptSourcesIfMissing() {
        val sourcesFile = File(prefixManager.prefixPath, NanoContract.APT_SOURCES_LIST_RELATIVE_PATH)
        if (!sourcesFile.exists() || sourcesFile.readText().isBlank()) {
            sourcesFile.parentFile?.mkdirs()
            sourcesFile.writeText(NanoContract.APT_SOURCES_LINE + "\n")
        }
    }

    override fun buildTermuxAppSyntaxConfigureCommand(): String {
        val dependenciesToInstall = NanoContract.SYNTAX_INSTALL_DEPENDENCIES.joinToString(" ")
        return buildString {
            append("pkg install -y $dependenciesToInstall && ")
            append("curl -fsSL '${NanoContract.SYNTAX_INSTALL_SCRIPT_URL}' -o /data/data/com.termux/files/usr/tmp/install-nanorc.sh && ")
            append("bash /data/data/com.termux/files/usr/tmp/install-nanorc.sh && ")
            append("(grep -q 'set linenumbers' ~/.nanorc || echo 'set linenumbers' >> ~/.nanorc)")
        }
    }
}