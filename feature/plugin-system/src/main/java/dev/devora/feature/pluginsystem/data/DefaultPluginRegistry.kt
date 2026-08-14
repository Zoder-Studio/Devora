package dev.devora.feature.pluginsystem.data

import dev.devora.core.logging.DevoraLogger
import dev.devora.feature.pluginsystem.api.DevoraPlugin
import dev.devora.feature.pluginsystem.api.WorkflowYamlToolingPlugin
import dev.devora.feature.pluginsystem.domain.PluginRegistry

private const val TAG = "PluginRegistry"

/**
 * Holds every plugin known to this build of Devora. Plugins are
 * passed in at construction time (via DI, see PluginSystemModule),
 * which is what makes this a compile-time registry — the set of
 * available plugins is fixed by what modules the developer chose to
 * include in the app, not by files present on the device at runtime.
 */
class DefaultPluginRegistry(
    private val plugins: List<DevoraPlugin>
) : PluginRegistry {

    init {
        plugins.forEach { plugin ->
            try {
                plugin.onRegistered()
            } catch (e: Exception) {
                DevoraLogger.e(TAG, "Plugin '${plugin.id}' failed during onRegistered()", e)
            }
        }
    }

    override fun allPlugins(): List<DevoraPlugin> = plugins

    override fun workflowYamlToolingPlugins(): List<WorkflowYamlToolingPlugin> =
        plugins.filterIsInstance<WorkflowYamlToolingPlugin>()

    override fun findById(id: String): DevoraPlugin? = plugins.find { it.id == id }
}