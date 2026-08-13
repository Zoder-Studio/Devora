package dev.devora.feature.workflowengine.domain.model

/**
 * Mirrors GitHub's real composite action format (action.yml with
 * runs.using: "composite"). Devora reads this exact structure — it
 * is not a Devora-specific format, so an action authored this way
 * also works correctly if pushed and referenced on real GitHub
 * Actions (spec section 11: workflow-related files must stay
 * portable to GitHub, applied here to actions as well).
 */
data class LocalActionDefinition(
    val name: String?,
    val description: String?,
    val inputs: Map<String, LocalActionInput>,
    val steps: List<WorkflowStep>
)

data class LocalActionInput(
    val description: String?,
    val required: Boolean,
    val default: String?
)