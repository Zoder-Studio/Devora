package dev.devora.feature.sdkmanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.data.SdkManagerContract
import dev.devora.feature.sdkmanager.domain.model.SdkComponent
import dev.devora.feature.sdkmanager.domain.model.SdkListResult
import dev.devora.feature.sdkmanager.domain.repository.SdkRepository
import dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import java.io.File

class DefaultSdkRepository(
    private val enginePaths: EnginePaths,
    private val engineProvider: CommandExecutionEngineProvider,
    private val versionStore: PinnedCmdlineToolsVersionStore
) : SdkRepository {

    override fun isSdkToolingInstalled(): Boolean {
        val sdkRoot = SdkManagerContract.sdkRoot(enginePaths.currentPrefixPath())
        return File(sdkRoot, SdkManagerContract.SDKMANAGER_RELATIVE_PATH).exists()
    }

    override suspend fun setupSdkTooling(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        if (isSdkToolingInstalled()) return DevoraResult.Success(Unit)

        val pinned = versionStore.read(
            PinnedCmdlineToolsVersion(
                downloadUrl = SdkManagerContract.DEFAULT_CMDLINE_TOOLS_DOWNLOAD_URL,
                revision = SdkManagerContract.DEFAULT_CMDLINE_TOOLS_REVISION
            )
        )
        val prefixPath = enginePaths.currentPrefixPath()
        val sdkRoot = SdkManagerContract.sdkRoot(prefixPath)
        val script = buildString {
            append("apt update && apt install -y ${SdkManagerContract.JDK_APT_PACKAGE} unzip curl && ")
            append("mkdir -p '$sdkRoot/${SdkManagerContract.CMDLINE_TOOLS_DIR_NAME}' && ")
            append("cd '$sdkRoot/${SdkManagerContract.CMDLINE_TOOLS_DIR_NAME}' && ")
            append("curl -fsSL '${pinned.downloadUrl}' -o cmdline-tools.zip && ")
            append("unzip -o cmdline-tools.zip && rm cmdline-tools.zip && ")
            append("mv cmdline-tools ${SdkManagerContract.CMDLINE_TOOLS_LATEST_SUBDIR}")
        }
        onOutputLine("Setting up Android SDK Command-line Tools (${pinned.revision})...")
        return engineProvider.current().run(prefixPath, script, 600_000L, onOutputLine)
    }

    override suspend fun listPackages(onOutputLine: (String) -> Unit): DevoraResult<SdkListResult> {
        if (!isSdkToolingInstalled()) {
            return DevoraResult.Failure(message = "SDK tooling not set up yet. Call setupSdkTooling() first.")
        }
        val sdkRoot = SdkManagerContract.sdkRoot(enginePaths.currentPrefixPath())
        val sdkmanagerPath = "$sdkRoot/${SdkManagerContract.SDKMANAGER_RELATIVE_PATH}"
        val lines = mutableListOf<String>()
        val result = engineProvider.current().run(
            workingDirectory = sdkRoot,
            script = "yes | '$sdkmanagerPath' --sdk_root='$sdkRoot' --list",
            onOutputLine = { line -> lines.add(line); onOutputLine(line) }
        )
        if (result is DevoraResult.Failure) return DevoraResult.Failure(message = result.message, cause = result.cause)
        return DevoraResult.Success(parseSdkManagerListOutput(lines))
    }

    override suspend fun installPackage(
        packagePath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        if (!isSdkToolingInstalled()) {
            return DevoraResult.Failure(message = "SDK tooling not set up yet.")
        }
        val sdkRoot = SdkManagerContract.sdkRoot(prefixManager.prefixPath)
        val sdkmanagerPath = "$sdkRoot/${SdkManagerContract.SDKMANAGER_RELATIVE_PATH}"
        return commandRunner.run(
            script = "yes | '$sdkmanagerPath' --sdk_root='$sdkRoot' '$packagePath'",
            timeoutMillis = 600_000L,
            onOutputLine = onOutputLine
        )
    }

    override suspend fun uninstallPackage(
        packagePath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        if (!isSdkToolingInstalled()) {
            return DevoraResult.Failure(message = "SDK tooling not set up yet.")
        }
        val sdkRoot = SdkManagerContract.sdkRoot(prefixManager.prefixPath)
        val sdkmanagerPath = "$sdkRoot/${SdkManagerContract.SDKMANAGER_RELATIVE_PATH}"
        return commandRunner.run(
            script = "'$sdkmanagerPath' --sdk_root='$sdkRoot' --uninstall '$packagePath'",
            onOutputLine = onOutputLine
        )
    }

    /**
     * Parses the real "sdkmanager --list" output format:
     *
     * Installed packages:
     *   Path                        | Version | Description | Location
     *   -------                     | ------- | -------     | -------
     *   platforms;android-34        | 1       | Android SDK Platform 34 | platforms/android-34
     *
     * Available Packages:
     *   Path                        | Version | Description
     *   -------                     | ------- | -------
     *   platforms;android-35        | 1       | Android SDK Platform 35
     *
     * This parser reads the real pipe-delimited table sdkmanager prints;
     * it does not guess or hardcode which SDKs exist.
     */
    private fun parseSdkManagerListOutput(lines: List<String>): SdkListResult {
        val installed = mutableListOf<SdkComponent>()
        val available = mutableListOf<SdkComponent>()

        var section = ""
        for (rawLine in lines) {
            val line = rawLine.trim()
            when {
                line.startsWith("Installed packages", ignoreCase = true) -> section = "installed"
                line.startsWith("Available Packages", ignoreCase = true) -> section = "available"
                line.startsWith("Available Updates", ignoreCase = true) -> section = "updates"
                line.contains("Path") && line.contains("Version") -> continue
                line.startsWith("---") || line.startsWith("=") -> continue
                line.isBlank() -> continue
                section == "installed" || section == "available" -> {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 2 && parts[0].isNotBlank() && parts[0] != "Path") {
                        val component = SdkComponent(
                            packagePath = parts[0],
                            displayName = parts.getOrElse(2) { parts[0] },
                            version = parts.getOrElse(1) { "" },
                            isInstalled = section == "installed"
                        )
                        if (section == "installed") installed.add(component) else available.add(component)
                    }
                }
            }
        }

        return SdkListResult(installed = installed, available = available)
    }
}