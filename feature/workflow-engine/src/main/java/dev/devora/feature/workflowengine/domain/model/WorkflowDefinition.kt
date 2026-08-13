package dev.devora.feature.workflowengine.domain.model

data class WorkflowDefinition(
    val name: String?,
    val filePath: String,
    val jobs: List<WorkflowJob>
)

data class WorkflowJob(
    val id: String,
    val runsOn: String?,
    val steps: List<WorkflowStep>
)

sealed class WorkflowStep {
    abstract val name: String?

    data class RunStep(
        override val name: String?,
        val run: String,
        val workingDirectory: String? = null,
        val env: Map<String, String> = emptyMap()
    ) : WorkflowStep()

    data class UsesStep(
        override val name: String?,
        val uses: String,
        val with: Map<String, String> = emptyMap()
    ) : WorkflowStep()
}