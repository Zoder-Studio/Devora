package dev.devora.feature.workflowengine.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.LocalActionDefinition
import dev.devora.feature.workflowengine.domain.model.LocalActionInput
import dev.devora.feature.workflowengine.domain.model.WorkflowStep
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Parses action.yml using GitHub's real composite action schema:
 *   name, description, inputs: { <id>: { description, required, default } }
 *   runs: { using: "composite", steps: [ ... same shape as workflow steps ... ] }
 * Only "using: composite" is supported locally — "using: docker" and
 * "using: node20"/"node16" JavaScript actions require a runtime
 * (Docker, or the Actions Toolkit Node.js API) that Devora does not
 * embed, so those fail clearly rather than being faked.
 */
class ActionYamlParser {

    private val yaml = Yaml()

    fun parse(actionFile: File): DevoraResult<LocalActionDefinition> {
        if (!actionFile.exists()) {
            return DevoraResult.Failure(message = "action.yml does not exist: ${actionFile.absolutePath}")
        }

        return try {
            @Suppress("UNCHECKED_CAST")
            val root = yaml.load<Map<String, Any?>>(actionFile.readText())
                ?: return DevoraResult.Failure(message = "action.yml is empty: ${actionFile.absolutePath}")

            val name = root["name"] as? String
            val description = root["description"] as? String

            @Suppress("UNCHECKED_CAST")
            val inputsMap = (root["inputs"] as? Map<String, Any?>)?.mapValues { (_, value) ->
                @Suppress("UNCHECKED_CAST")
                val inputMap = value as? Map<String, Any?> ?: emptyMap()
                LocalActionInput(
                    description = inputMap["description"] as? String,
                    required = (inputMap["required"] as? Boolean) ?: false,
                    default = inputMap["default"]?.toString()
                )
            } ?: emptyMap()

            @Suppress("UNCHECKED_CAST")
            val runsMap = root["runs"] as? Map<String, Any?>
                ?: return DevoraResult.Failure(message = "No 'runs:' section in ${actionFile.absolutePath}")

            val using = runsMap["using"] as? String
            if (using != "composite") {
                return DevoraResult.Failure(
                    message = "Devora's Built-in Runner only supports 'using: composite' local actions. " +
                        "${actionFile.absolutePath} declares 'using: $using', which requires a runtime " +
                        "(Docker or Node.js Actions Toolkit) Devora does not embed locally. This action " +
                        "still works correctly on real GitHub Actions — this limitation is local-execution only."
                )
            }

            @Suppress("UNCHECKED_CAST")
            val stepsList = runsMap["steps"] as? List<Map<String, Any?>>
                ?: return DevoraResult.Failure(message = "No 'runs.steps:' list in ${actionFile.absolutePath}")

            val steps = stepsList.map { stepMap -> parseCompositeStep(stepMap) }

            DevoraResult.Success(
                LocalActionDefinition(name = name, description = description, inputs = inputsMap, steps = steps)
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to parse action.yml: ${actionFile.absolutePath}", cause = e)
        }
    }

    private fun parseCompositeStep(stepMap: Map<String, Any?>): WorkflowStep {
        val name = stepMap["name"] as? String
        val runValue = stepMap["run"] as? String

        @Suppress("UNCHECKED_CAST")
        val envMap = (stepMap["env"] as? Map<String, Any?>)?.mapValues { it.value.toString() } ?: emptyMap()

        return if (runValue != null) {
            WorkflowStep.RunStep(
                name = name,
                run = runValue,
                workingDirectory = stepMap["working-directory"] as? String,
                env = envMap
            )
        } else {
            WorkflowStep.RunStep(name = name, run = "true")
        }
    }
}