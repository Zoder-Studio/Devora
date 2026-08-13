package dev.devora.feature.workflowengine.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.LocalActionDefinition
import dev.devora.feature.workflowengine.domain.model.WorkflowStep
import java.io.File

/**
 * Resolves "uses: ./<path>" exactly the way real GitHub Actions does:
 * the path is relative to the repository root, nothing more. No
 * Devora-specific fallback location — a local action authored this
 * way resolves identically whether run inside Devora or on real
 * GitHub Actions (spec section 11: workflow-related files must stay
 * portable, applied here to actions too).
 */
class LocalActionResolver(private val actionYamlParser: ActionYamlParser) {

    fun resolve(projectRootPath: String, usesPath: String): DevoraResult<LocalActionDefinition> {
        val actionDir = File(projectRootPath, usesPath.removePrefix("./"))

        if (!actionDir.exists() || !actionDir.isDirectory) {
            return DevoraResult.Failure(
                message = "Local action not found at ${actionDir.absolutePath}"
            )
        }

        val actionYmlFile = listOf("action.yml", "action.yaml")
            .map { File(actionDir, it) }
            .firstOrNull { it.exists() }
            ?: return DevoraResult.Failure(
                message = "No action.yml or action.yaml found in ${actionDir.absolutePath}"
            )

        return actionYamlParser.parse(actionYmlFile)
    }

    fun resolveStepsWithInputs(
        action: LocalActionDefinition,
        providedInputs: Map<String, String>
    ): DevoraResult<List<WorkflowStep>> {
        val resolvedInputs = mutableMapOf<String, String>()
        for ((inputName, inputDef) in action.inputs) {
            val value = providedInputs[inputName] ?: inputDef.default
            if (value == null && inputDef.required) {
                return DevoraResult.Failure(
                    message = "Required input '$inputName' was not provided and has no default."
                )
            }
            if (value != null) resolvedInputs[inputName] = value
        }

        val substitutedSteps = action.steps.map { step ->
            when (step) {
                is WorkflowStep.RunStep -> step.copy(run = substitute(step.run, resolvedInputs))
                is WorkflowStep.UsesStep -> step
            }
        }
        return DevoraResult.Success(substitutedSteps)
    }

    private fun substitute(template: String, inputs: Map<String, String>): String {
        var result = template
        for ((key, value) in inputs) {
            result = result.replace("\${{ inputs.$key }}", value)
            result = result.replace("\${{inputs.$key}}", value)
        }
        return result
    }
}