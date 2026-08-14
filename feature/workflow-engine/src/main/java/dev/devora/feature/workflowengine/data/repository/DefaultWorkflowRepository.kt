package dev.devora.feature.workflowengine.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.notifications.domain.model.WorkflowNotification
import dev.devora.feature.notifications.domain.model.WorkflowNotificationStatus
import dev.devora.feature.notifications.domain.usecase.PostWorkflowNotificationUseCase
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngine
import dev.devora.feature.terminal.domain.execution.EnginePaths
import dev.devora.feature.workflowengine.data.LocalActionResolver
import dev.devora.feature.workflowengine.data.WorkflowYamlParser
import dev.devora.feature.workflowengine.domain.action.SupportedAction
import dev.devora.feature.workflowengine.domain.model.WorkflowDefinition
import dev.devora.feature.workflowengine.domain.model.WorkflowPermission
import dev.devora.feature.workflowengine.domain.model.WorkflowRunResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStep
import dev.devora.feature.workflowengine.domain.model.WorkflowStepResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStepStatus
import dev.devora.feature.workflowengine.domain.repository.WorkflowEnvironmentRepository
import dev.devora.feature.workflowengine.domain.repository.WorkflowPermissionRepository
import dev.devora.feature.workflowengine.domain.repository.WorkflowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DefaultWorkflowRepository(
    private val parser: WorkflowYamlParser,
    private val environmentRepository: WorkflowEnvironmentRepository,
    private val enginePaths: EnginePaths,
    private val artifactStagingRoot: File,
    private val localActionResolver: LocalActionResolver,
    private val permissionRepository: WorkflowPermissionRepository,
    private val postNotificationUseCase: PostWorkflowNotificationUseCase
) : WorkflowRepository {

    override fun listWorkflowFiles(projectRootPath: String): DevoraResult<List<String>> {
        val workflowsDir = File(projectRootPath, ".devora/workflows")
        if (!workflowsDir.exists() || !workflowsDir.isDirectory) {
            return DevoraResult.Success(emptyList())
        }
        val files = workflowsDir.listFiles { file -> file.extension == "yml" || file.extension == "yaml" }
            ?.map { it.absolutePath }
            ?: emptyList()
        return DevoraResult.Success(files)
    }

    override fun parseWorkflow(workflowFilePath: String): DevoraResult<WorkflowDefinition> =
        parser.parse(File(workflowFilePath))

    override fun runJob(
        projectRootPath: String,
        workflow: WorkflowDefinition,
        jobId: String
    ): Flow<WorkflowRunResult> = flow {
        val workflowId = File(workflow.filePath).nameWithoutExtension
        val notificationId = workflowId.hashCode()
        val deepLinkPath = "workflow_run/${java.net.URLEncoder.encode(projectRootPath, "UTF-8")}/" +
            "${java.net.URLEncoder.encode(workflow.filePath, "UTF-8")}/$jobId"

        // --- Permission check (Stage 11) ---
        val permission = permissionRepository.get(projectRootPath, workflowId)
        if (permission == null) {
            val failureResult = WorkflowRunResult(
                workflowFilePath = workflow.filePath,
                jobId = jobId,
                stepResults = listOf(
                    WorkflowStepResult(
                        stepIndex = 0,
                        stepName = "Permission check",
                        status = WorkflowStepStatus.FAILED,
                        errorMessage = "Permission denied\n\nOperation:\nRUN\n\nResource:\n$workflowId\n\n" +
                            "No permission has been granted for this workflow. Set one in " +
                            "Workflow Permissions before running."
                    )
                ),
                overallSuccess = false
            )
            postNotificationUseCase(
                WorkflowNotification(
                    id = notificationId,
                    title = "Devora — Workflow failed",
                    message = "${workflow.name ?: workflowId}: no permission granted.",
                    status = WorkflowNotificationStatus.FAILED,
                    deepLinkPath = deepLinkPath
                )
            )
            emit(failureResult)
            return@flow
        }

        // --- Per-workflow environment (Stage 10) ---
        postNotificationUseCase(
            WorkflowNotification(
                id = notificationId,
                title = "Devora — Workflow queued",
                message = "${workflow.name ?: workflowId} is starting.",
                status = WorkflowNotificationStatus.RUNNING,
                deepLinkPath = deepLinkPath
            )
        )

        val engineResult = environmentRepository.getOrPrepareEngine(workflowId) { }
        if (engineResult is DevoraResult.Failure) {
            val failureResult = WorkflowRunResult(
                workflowFilePath = workflow.filePath,
                jobId = jobId,
                stepResults = listOf(
                    WorkflowStepResult(0, "Environment setup", WorkflowStepStatus.FAILED, errorMessage = engineResult.message)
                ),
                overallSuccess = false
            )
            postNotificationUseCase(
                WorkflowNotification(
                    id = notificationId,
                    title = "Devora — Workflow failed",
                    message = "${workflow.name ?: workflowId}: environment setup failed.",
                    status = WorkflowNotificationStatus.FAILED,
                    deepLinkPath = deepLinkPath
                )
            )
            emit(failureResult)
            return@flow
        }
        val engine = (engineResult as DevoraResult.Success).data

        val job = workflow.jobs.find { it.id == jobId }
        if (job == null) {
            emit(WorkflowRunResult(workflow.filePath, jobId, emptyList(), overallSuccess = false))
            return@flow
        }

        val results = mutableListOf<WorkflowStepResult>()
        var jobFailed = false

        // --- Permission enforcement (Stage 11): real POSIX write lock for READ-only permission ---
        val needsReadOnlyLock = permission == WorkflowPermission.READ
        if (needsReadOnlyLock) {
            engine.run(
                workingDirectory = projectRootPath,
                script = "chmod -R a-w '$projectRootPath'",
                onOutputLine = {}
            )
        }

        try {
            job.steps.forEachIndexed { index, step ->
                if (jobFailed) {
                    results.add(WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.SKIPPED))
                    emit(currentResult(workflow, jobId, results))
                    return@forEachIndexed
                }

                val stepOutput = mutableListOf<String>()
                val stepResult = when (step) {
                    is WorkflowStep.RunStep -> executeRunStep(engine, projectRootPath, step, index, stepOutput)
                    is WorkflowStep.UsesStep -> executeUsesStep(engine, projectRootPath, step, index, stepOutput)
                }

                results.add(stepResult)
                if (stepResult.status == WorkflowStepStatus.FAILED) jobFailed = true
                emit(currentResult(workflow, jobId, results))
            }
        } finally {
            if (needsReadOnlyLock) {
                engine.run(
                    workingDirectory = projectRootPath,
                    script = "chmod -R u+w '$projectRootPath'",
                    onOutputLine = {}
                )
            }
        }

        postNotificationUseCase(
            WorkflowNotification(
                id = notificationId,
                title = if (!jobFailed) "Devora — Workflow succeeded" else "Devora — Workflow failed",
                message = if (!jobFailed) {
                    "${workflow.name ?: workflowId} completed successfully."
                } else {
                    "${workflow.name ?: workflowId} failed. Task: " +
                        "${results.lastOrNull { it.status == WorkflowStepStatus.FAILED }?.stepName ?: "unknown"}"
                },
                status = if (!jobFailed) WorkflowNotificationStatus.SUCCESS else WorkflowNotificationStatus.FAILED,
                deepLinkPath = deepLinkPath
            )
        )

        emit(currentResult(workflow, jobId, results).copy(overallSuccess = !jobFailed))
    }

    private fun currentResult(
        workflow: WorkflowDefinition,
        jobId: String,
        results: List<WorkflowStepResult>
    ) = WorkflowRunResult(
        workflowFilePath = workflow.filePath,
        jobId = jobId,
        stepResults = results.toList(),
        overallSuccess = results.none { it.status == WorkflowStepStatus.FAILED }
    )

    private suspend fun executeRunStep(
        engine: CommandExecutionEngine,
        projectRootPath: String,
        step: WorkflowStep.RunStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val workingDirectory = step.workingDirectory?.let { File(projectRootPath, it).absolutePath }
            ?: projectRootPath
        val envPrefix = step.env.entries.joinToString(" ") { (key, value) -> "$key='$value'" }
        val script = if (envPrefix.isBlank()) step.run else "$envPrefix ${step.run}"

        val result = engine.run(
            workingDirectory = workingDirectory,
            script = script,
            timeoutMillis = 1_800_000L,
            onOutputLine = { line -> output.add(line) }
        )

        return when (result) {
            is DevoraResult.Success -> WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.SUCCESS, output)
            is DevoraResult.Failure -> WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED, output, result.message
            )
        }
    }

    private suspend fun executeUsesStep(
        engine: CommandExecutionEngine,
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        if (step.uses.startsWith("./") || step.uses.startsWith("../")) {
            return executeLocalActionStep(engine, projectRootPath, step, index, output)
        }

        val matched = SupportedAction.match(step.uses)
            ?: return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = "Action '${step.uses}' is not supported by Devora's Built-in Runner. " +
                    "This workflow will still run correctly on real GitHub Actions — this limitation " +
                    "only applies to local execution inside Devora. Supported: " +
                    SupportedAction.entries.joinToString(", ") { it.actionPrefix } +
                    ", or a local composite action via 'uses: ./actions/<name>'."
            )

        return when (matched) {
            SupportedAction.CHECKOUT -> executeCheckoutStep(projectRootPath, step, index, output)
            SupportedAction.SETUP_JAVA -> executeSetupJavaStep(engine, step, index, output)
            SupportedAction.UPLOAD_ARTIFACT -> executeUploadArtifactStep(projectRootPath, step, index, output)
        }
    }

    private suspend fun executeCheckoutStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val gitDir = File(projectRootPath, ".git")
        return if (gitDir.exists() && gitDir.isDirectory) {
            output.add("Local checkout equivalent: $projectRootPath is a git repository. Skipping network clone.")
            WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.SUCCESS, output)
        } else {
            WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED, output,
                errorMessage = "actions/checkout requires a git repository, but $projectRootPath has no .git directory."
            )
        }
    }

    private suspend fun executeSetupJavaStep(
        engine: CommandExecutionEngine,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val requestedVersion = step.with["java-version"]
        val result = engine.run(
            workingDirectory = enginePaths.currentPrefixPath(),
            script = "java -version",
            onOutputLine = { line -> output.add(line) }
        )

        return when (result) {
            is DevoraResult.Success -> WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.SUCCESS, output)
            is DevoraResult.Failure -> WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED, output,
                errorMessage = "No JDK found (requested java-version: ${requestedVersion ?: "unspecified"}). " +
                    "Install a JDK via SDK Manager first."
            )
        }
    }

    private suspend fun executeUploadArtifactStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val artifactName = step.with["name"] ?: "artifact"
        val sourcePath = step.with["path"]
            ?: return WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED,
                errorMessage = "actions/upload-artifact requires a 'path' input."
            )

        val sourceFile = File(projectRootPath, sourcePath).let { if (it.exists()) it else File(sourcePath) }
        if (!sourceFile.exists()) {
            return WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED,
                errorMessage = "Artifact source path does not exist: $sourcePath"
            )
        }

        return try {
            val stagingDir = File(artifactStagingRoot, artifactName).apply { mkdirs() }
            val destination = File(stagingDir, sourceFile.name)
            if (sourceFile.isDirectory) {
                sourceFile.copyRecursively(destination, overwrite = true)
            } else {
                sourceFile.copyTo(destination, overwrite = true)
            }
            output.add("Staged artifact '$artifactName': ${destination.absolutePath}")
            WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.SUCCESS, output)
        } catch (e: Exception) {
            WorkflowStepResult(
                index, stepDisplayName(step, index), WorkflowStepStatus.FAILED,
                errorMessage = "Failed to stage artifact: ${e.message}"
            )
        }
    }

    private suspend fun executeLocalActionStep(
        engine: CommandExecutionEngine,
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val actionResult = localActionResolver.resolve(projectRootPath, step.uses)
        if (actionResult is DevoraResult.Failure) {
            return WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.FAILED, errorMessage = actionResult.message)
        }
        val action = (actionResult as DevoraResult.Success).data

        val substitutedStepsResult = localActionResolver.resolveStepsWithInputs(action, step.with)
        if (substitutedStepsResult is DevoraResult.Failure) {
            return WorkflowStepResult(index, stepDisplayName(step, index), WorkflowStepStatus.FAILED, errorMessage = substitutedStepsResult.message)
        }
        val substitutedSteps = (substitutedStepsResult as DevoraResult.Success).data

        for ((subIndex, subStep) in substitutedSteps.withIndex()) {
            if (subStep !is WorkflowStep.RunStep) continue // nested "uses:" inside composite actions not supported yet

            val subResult = executeRunStep(engine, projectRootPath, subStep, subIndex, output)
            output.add("--- end of composite sub-step ${subIndex + 1} (${subResult.status}) ---")
            if (subResult.status == WorkflowStepStatus.FAILED) {
                return WorkflowStepResult(
                    index,
                    "${stepDisplayName(step, index)} [local action: ${action.name ?: step.uses}]",
                    WorkflowStepStatus.FAILED,
                    output,
                    errorMessage = "Composite sub-step ${subIndex + 1} failed: ${subResult.errorMessage}"
                )
            }
        }

        return WorkflowStepResult(
            index,
            "${stepDisplayName(step, index)} [local action: ${action.name ?: step.uses}]",
            WorkflowStepStatus.SUCCESS,
            output
        )
    }

    private fun stepDisplayName(step: WorkflowStep, index: Int): String =
        step.name ?: when (step) {
            is WorkflowStep.RunStep -> "run: ${step.run.take(40)}"
            is WorkflowStep.UsesStep -> "uses: ${step.uses}"
        }.let { "Step ${index + 1}: $it" }
}