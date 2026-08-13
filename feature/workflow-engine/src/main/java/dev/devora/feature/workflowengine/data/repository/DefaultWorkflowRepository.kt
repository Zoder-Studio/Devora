package dev.devora.feature.workflowengine.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.data.WorkflowYamlParser
import dev.devora.feature.workflowengine.domain.action.SupportedAction
import dev.devora.feature.workflowengine.domain.model.WorkflowDefinition
import dev.devora.feature.workflowengine.domain.model.WorkflowRunResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStep
import dev.devora.feature.workflowengine.domain.model.WorkflowStepResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStepStatus
import dev.devora.feature.workflowengine.domain.repository.WorkflowRepository
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DefaultWorkflowRepository(
    private val parser: WorkflowYamlParser,
    private val environmentRepository: WorkflowEnvironmentRepository
    private val enginePaths: EnginePaths,
    private val artifactStagingRoot: File
    private val localActionResolver: LocalActionResolver
    private val permissionRepository: WorkflowPermissionRepository
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

        val permission = permissionRepository.get(projectRootPath, workflowId)
        if (permission == null) {
            emit(
                WorkflowRunResult(
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
            )
            return@flow
        }

        val engineResult = environmentRepository.getOrPrepareEngine(workflowId) { }
        if (engineResult is DevoraResult.Failure) {
            emit(
                WorkflowRunResult(
                    workflow.filePath, jobId,
                    listOf(WorkflowStepResult(0, "Environment setup", WorkflowStepStatus.FAILED, errorMessage = engineResult.message)),
                    overallSuccess = false
                )
            )
            return@flow
        }
        val engine = (engineResult as DevoraResult.Success).data
        val enforcer = WorkflowPermissionEnforcer(engine)

        val job = workflow.jobs.find { it.id == jobId }
        if (job == null) {
            emit(WorkflowRunResult(workflow.filePath, jobId, emptyList(), overallSuccess = false))
            return@flow
        }

        val results = mutableListOf<WorkflowStepResult>()
        var jobFailed = false

        val enforcementResult = enforcer.withEnforcement(projectRootPath, permission) {
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
        }

        if (enforcementResult is DevoraResult.Failure) {
            results.add(
                WorkflowStepResult(
                    stepIndex = results.size,
                    stepName = "Permission enforcement",
                    status = WorkflowStepStatus.FAILED,
                    errorMessage = enforcementResult.message
                )
            )
            jobFailed = true
        }

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

    private suspend fun kotlinx.coroutines.flow.FlowCollector<WorkflowRunResult>.emitIntermediate(
        workflow: WorkflowDefinition,
        jobId: String,
        results: List<WorkflowStepResult>
    ) {
        emit(
            WorkflowRunResult(
                workflowFilePath = workflow.filePath,
                jobId = jobId,
                stepResults = results.toList(),
                overallSuccess = results.none { it.status == WorkflowStepStatus.FAILED }
            )
        )
    }

    private suspend fun executeRunStep(
        projectRootPath: String,
        step: WorkflowStep.RunStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val workingDirectory = step.workingDirectory?.let { File(projectRootPath, it).absolutePath }
            ?: projectRootPath

        val envPrefix = step.env.entries.joinToString(" ") { (key, value) -> "$key='$value'" }
        val script = if (envPrefix.isBlank()) step.run else "$envPrefix ${step.run}"

        val result = engine: CommandExecutionEngine().run(
            workingDirectory = workingDirectory,
            script = script,
            timeoutMillis = 1_800_000L,
            onOutputLine = { line -> output.add(line) }
        )

        return when (result) {
            is DevoraResult.Success -> WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.SUCCESS,
                outputLines = output
            )
            is DevoraResult.Failure -> WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                outputLines = output,
                errorMessage = result.message
            )
        }
    }

    private suspend fun executeUsesStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        if (step.uses.startsWith("./") || step.uses.startsWith("../")) {
            return executeLocalActionStep(projectRootPath, step, index, output)
        }

        val matched = SupportedAction.match(step.uses)
            ?: return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = "Action '${step.uses}' is not supported by Devora's Built-in Runner. " +
                    "This workflow will still run correctly on real GitHub Actions — this limitation " +
                    "only applies to local execution inside Devora. Supported: " +
                    dev.devora.feature.workflowengine.domain.action.SupportedAction.entries
                        .joinToString(", ") { it.actionPrefix } +
                    ", or a local composite action via 'uses: ./actions/<name>'."
            )

        return when (matched) {
            SupportedAction.CHECKOUT -> executeCheckoutStep(projectRootPath, step, index, output)
            SupportedAction.SETUP_JAVA -> executeSetupJavaStep(projectRootPath, step, index, output)
            SupportedAction.UPLOAD_ARTIFACT -> executeUploadArtifactStep(projectRootPath, step, index, output)
        }
    }

    private suspend fun executeLocalActionStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val actionResult = localActionResolver.resolve(projectRootPath, step.uses)
        if (actionResult is DevoraResult.Failure) {
            return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = actionResult.message
            )
        }
        val action = (actionResult as DevoraResult.Success).data

        val substitutedStepsResult = localActionResolver.resolveStepsWithInputs(action, step.with)
        if (substitutedStepsResult is DevoraResult.Failure) {
            return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = substitutedStepsResult.message
            )
        }
        val substitutedSteps = (substitutedStepsResult as DevoraResult.Success).data

        var allSucceeded = true
        for ((subIndex, subStep) in substitutedSteps.withIndex()) {
            if (subStep !is WorkflowStep.RunStep) continue // nested "uses:" inside composite actions not supported yet

            val subResult = executeRunStep(projectRootPath, subStep, subIndex, output)
            output.add("--- end of composite sub-step ${subIndex + 1} (${subResult.status}) ---")
            if (subResult.status == WorkflowStepStatus.FAILED) {
                allSucceeded = false
                return WorkflowStepResult(
                    stepIndex = index,
                    stepName = "${stepDisplayName(step, index)} [local action: ${action.name ?: step.uses}]",
                    status = WorkflowStepStatus.FAILED,
                    outputLines = output,
                    errorMessage = "Composite sub-step ${subIndex + 1} failed: ${subResult.errorMessage}"
                )
            }
        }

        return WorkflowStepResult(
            stepIndex = index,
            stepName = "${stepDisplayName(step, index)} [local action: ${action.name ?: step.uses}]",
            status = if (allSucceeded) WorkflowStepStatus.SUCCESS else WorkflowStepStatus.FAILED,
            outputLines = output
        )
    }

    /**
     * Local equivalent of actions/checkout: the project is already
     * present on-device (that's the whole premise of Devora), so this
     * step verifies the working directory is a real git repository
     * rather than performing a network checkout. It does not clone or
     * reset anything — Devora never silently mutates project state.
     */
    private suspend fun executeCheckoutStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val gitDir = File(projectRootPath, ".git")
        return if (gitDir.exists() && gitDir.isDirectory) {
            output.add("Local checkout equivalent: $projectRootPath is a git repository. Skipping network clone.")
            WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.SUCCESS,
                outputLines = output
            )
        } else {
            WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                outputLines = output,
                errorMessage = "actions/checkout requires a git repository, but $projectRootPath has no .git directory."
            )
        }
    }

    /**
     * Local equivalent of actions/setup-java: verifies a JDK is present
     * in the current engine's prefix rather than downloading one itself
     * — JDK installation belongs to SDK Manager (Stage 6), not the
     * workflow engine, to avoid duplicating that responsibility.
     */
    private suspend fun executeSetupJavaStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val requestedVersion = step.with["java-version"]
        val result = engineProvider.current().run(
            workingDirectory = enginePaths.currentPrefixPath(),
            script = "java -version",
            onOutputLine = { line -> output.add(line) }
        )

        return when (result) {
            is DevoraResult.Success -> WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.SUCCESS,
                outputLines = output
            )
            is DevoraResult.Failure -> WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                outputLines = output,
                errorMessage = "No JDK found (requested java-version: ${requestedVersion ?: "unspecified"}). " +
                    "Install a JDK via SDK Manager first."
            )
        }
    }

    /**
     * Local equivalent of actions/upload-artifact: copies the file(s)
     * at "path" into Devora's own artifact staging directory instead
     * of uploading to GitHub's artifact storage (which does not exist
     * locally). The staged files become visible to Stage 12's Artifact
     * Manager. "name" input becomes the staging subfolder name, same
     * as the real action's grouping behavior.
     */
    private suspend fun executeUploadArtifactStep(
        projectRootPath: String,
        step: WorkflowStep.UsesStep,
        index: Int,
        output: MutableList<String>
    ): WorkflowStepResult {
        val artifactName = step.with["name"] ?: "artifact"
        val sourcePath = step.with["path"]
            ?: return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = "actions/upload-artifact requires a 'path' input."
            )

        val sourceFile = File(projectRootPath, sourcePath).let {
            if (it.exists()) it else File(sourcePath)
        }
        if (!sourceFile.exists()) {
            return WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
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
            WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.SUCCESS,
                outputLines = output
            )
        } catch (e: Exception) {
            WorkflowStepResult(
                stepIndex = index,
                stepName = stepDisplayName(step, index),
                status = WorkflowStepStatus.FAILED,
                errorMessage = "Failed to stage artifact: ${e.message}"
            )
        }
    }

    private fun stepDisplayName(step: WorkflowStep, index: Int): String =
        step.name ?: when (step) {
            is WorkflowStep.RunStep -> "run: ${step.run.take(40)}"
            is WorkflowStep.UsesStep -> "uses: ${step.uses}"
        }.let { "Step ${index + 1}: $it" }
}