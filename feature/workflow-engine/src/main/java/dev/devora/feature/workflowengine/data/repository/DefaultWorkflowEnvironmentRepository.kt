package dev.devora.feature.workflowengine.data.repository

import android.content.Context
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.data.embedded.EmbeddedCommandRunner
import dev.devora.feature.terminal.data.embedded.EmbeddedPrefixManager
import dev.devora.feature.terminal.data.embedded.EmbeddedSessionFactory
import dev.devora.feature.terminal.data.embedded.PinnedBootstrapVersionStore
import dev.devora.feature.terminal.data.execution.EmbeddedCommandExecutionEngine
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.model.TerminalEngineMode
import dev.devora.feature.workflowengine.domain.model.WorkflowEnvironmentInfo
import dev.devora.feature.workflowengine.domain.model.WorkflowEnvironmentStatus
import dev.devora.feature.workflowengine.domain.repository.WorkflowEnvironmentRepository
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages one isolated embedded environment per workflow file (spec
 * section 10). Each workflow gets its own rootDir under
 * "devora/environments/<workflowId>/", so a change made while running
 * one workflow's environment (installed packages, Gradle cache, JDK
 * version) never affects another workflow's environment.
 *
 * In TERMUX_APP mode, true isolation is not possible — see the class
 * doc in EmbeddedPrefixManager and the honesty note in getInfo()
 * below — so all workflows share the single Termux app installation
 * and isIsolated=false is reported plainly rather than pretending
 * otherwise.
 */
class DefaultWorkflowEnvironmentRepository(
    private val context: Context,
    private val engineSelector: TerminalEngineSelector,
    private val sharedEngineProvider: CommandExecutionEngineProvider
) : WorkflowEnvironmentRepository {

    private val environmentsRoot: File
        get() = File(context.filesDir, "devora/environments").apply { mkdirs() }

    private val prefixManagerCache = ConcurrentHashMap<String, EmbeddedPrefixManager>()
    private val engineCache = ConcurrentHashMap<String, EmbeddedCommandExecutionEngine>()

    override fun getInfo(workflowId: String): WorkflowEnvironmentInfo {
        return when (engineSelector.selectMode()) {
            TerminalEngineMode.TERMUX_APP -> WorkflowEnvironmentInfo(
                workflowId = workflowId,
                isIsolated = false,
                status = WorkflowEnvironmentStatus.READY, // shared Termux app is assumed ready; real readiness checked at run time
                installedBootstrapVersion = null,
                storageSizeBytes = 0L
            )
            TerminalEngineMode.EMBEDDED_BOOTSTRAP -> {
                val prefixManager = prefixManagerFor(workflowId)
                WorkflowEnvironmentInfo(
                    workflowId = workflowId,
                    isIsolated = true,
                    status = if (prefixManager.isPrepared()) {
                        WorkflowEnvironmentStatus.READY
                    } else {
                        WorkflowEnvironmentStatus.NOT_INITIALIZED
                    },
                    installedBootstrapVersion = prefixManager.installedReleaseTag(),
                    storageSizeBytes = prefixManager.storageSizeBytes()
                )
            }
        }
    }

    override suspend fun getOrPrepareEngine(
        workflowId: String,
        onProgress: (String) -> Unit
    ): DevoraResult<CommandExecutionEngine> {
        return when (engineSelector.selectMode()) {
            TerminalEngineMode.TERMUX_APP -> {
                onProgress(
                    "Warning: TERMUX_APP mode shares one Termux installation across all " +
                        "workflows — this workflow's environment is not isolated."
                )
                DevoraResult.Success(sharedEngineProvider.current())
            }
            TerminalEngineMode.EMBEDDED_BOOTSTRAP -> {
                val prefixManager = prefixManagerFor(workflowId)
                if (!prefixManager.isPrepared()) {
                    val prepareResult = prefixManager.prepare(onProgress)
                    if (prepareResult is DevoraResult.Failure) {
                        return DevoraResult.Failure(message = prepareResult.message, cause = prepareResult.cause)
                    }
                }
                DevoraResult.Success(engineFor(workflowId, prefixManager))
            }
        }
    }

    override suspend fun resetEnvironment(workflowId: String): DevoraResult<Unit> {
        if (engineSelector.selectMode() != TerminalEngineMode.EMBEDDED_BOOTSTRAP) {
            return DevoraResult.Failure(
                message = "Reset is only meaningful for isolated (EMBEDDED_BOOTSTRAP) environments."
            )
        }
        val prefixManager = prefixManagerFor(workflowId)
        val result = prefixManager.wipe()
        if (result is DevoraResult.Success) {
            prefixManagerCache.remove(workflowId)
            engineCache.remove(workflowId)
        }
        return result
    }

    override suspend fun deleteEnvironment(workflowId: String): DevoraResult<Unit> =
        resetEnvironment(workflowId) // same operation: delete = wipe rootDir entirely, nothing else to remove

    private fun prefixManagerFor(workflowId: String): EmbeddedPrefixManager =
        prefixManagerCache.getOrPut(workflowId) {
            val rootDir = File(environmentsRoot, sanitize(workflowId))
            val versionStore = PinnedBootstrapVersionStore(File(rootDir, "pinned_bootstrap_version.json"))
            EmbeddedPrefixManager(rootDir, versionStore)
        }

    private fun engineFor(workflowId: String, prefixManager: EmbeddedPrefixManager): EmbeddedCommandExecutionEngine =
        engineCache.getOrPut(workflowId) {
            val sessionFactory = EmbeddedSessionFactory(prefixManager)
            val commandRunner = EmbeddedCommandRunner(prefixManager, sessionFactory)
            EmbeddedCommandExecutionEngine(commandRunner)
        }

    private fun sanitize(id: String): String = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}