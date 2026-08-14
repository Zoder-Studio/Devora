package dev.devora.feature.pluginsystem.domain

import dev.devora.feature.pluginsystem.api.DevoraPlugin
import dev.devora.feature.pluginsystem.api.WorkflowYamlToolingPlugin

interface PluginRegistry {
    fun allPlugins(): List<DevoraPlugin>

    fun workflowYamlToolingPlugins(): List<WorkflowYamlToolingPlugin>

    fun findById(id: String): DevoraPlugin?
}