package dev.devora.feature.pluginsystem.domain.usecase

import dev.devora.feature.pluginsystem.api.YamlValidationIssue
import dev.devora.feature.pluginsystem.domain.PluginRegistry

class ValidateWorkflowYamlUseCase(private val registry: PluginRegistry) {
    /** Runs every registered WorkflowYamlToolingPlugin's validator and merges results. */
    operator fun invoke(yamlContent: String): List<YamlValidationIssue> =
        registry.workflowYamlToolingPlugins().flatMap { it.validate(yamlContent) }
}