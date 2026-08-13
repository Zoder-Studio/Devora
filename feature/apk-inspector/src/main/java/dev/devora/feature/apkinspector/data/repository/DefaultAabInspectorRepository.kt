package dev.devora.feature.apkinspector.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.apkinspector.data.AabInspectorContract
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import dev.devora.feature.apkinspector.domain.repository.AabInspectorRepository
import java.io.File

class DefaultAabInspectorRepository(
    private val engineProvider: CommandExecutionEngineProvider,
    private val enginePaths: EnginePaths
) : AabInspectorRepository {

    private fun bundletoolJarPath(): String =
        "${enginePaths.currentPrefixPath()}/opt/${AabInspectorContract.BUNDLETOOL_JAR_NAME}"

    override fun isBundletoolInstalled(): Boolean = File(bundletoolJarPath()).exists()

    override suspend fun setupBundletool(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        if (isBundletoolInstalled()) return DevoraResult.Success(Unit)

        val prefixPath = enginePaths.currentPrefixPath()
        val script = buildString {
            append("apt update && apt install -y openjdk-17 curl && ")
            append("mkdir -p '$prefixPath/opt' && ")
            append("curl -fsSL '${AabInspectorContract.BUNDLETOOL_DOWNLOAD_URL}' -o '${bundletoolJarPath()}'")
        }
        onOutputLine("Setting up bundletool ${AabInspectorContract.BUNDLETOOL_VERSION}...")
        return engineProvider.current().run(prefixPath, script, 300_000L, onOutputLine)
    }

    override suspend fun dumpManifest(
        aabFilePath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<String> = runBundletoolDump("manifest", aabFilePath, onOutputLine)

    override suspend fun dumpConfig(
        aabFilePath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<String> = runBundletoolDump("config", aabFilePath, onOutputLine)

    private suspend fun runBundletoolDump(
        target: String,
        aabFilePath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<String> {
        if (!isBundletoolInstalled()) {
            return DevoraResult.Failure(message = "bundletool is not set up yet. Call setupBundletool() first.")
        }
        val output = StringBuilder()
        val result = engineProvider.current().run(
            workingDirectory = enginePaths.currentPrefixPath(),
            script = "java -jar '${bundletoolJarPath()}' dump $target --bundle='$aabFilePath'",
            onOutputLine = { line -> output.appendLine(line); onOutputLine(line) }
        )
        return when (result) {
            is DevoraResult.Success -> DevoraResult.Success(output.toString())
            is DevoraResult.Failure -> DevoraResult.Failure(message = result.message, cause = result.cause)
        }
    }
}