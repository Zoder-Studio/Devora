package dev.devora.feature.pluginsystem.api

/**
 * Public plugin contract. A plugin registers itself in
 * DevoraPluginRegistry (built into the app at compile time, since
 * Devora deliberately does not support dynamically loading
 * arbitrary code at runtime — spec section 12 asks for workflow
 * YAML tooling, not a general code-execution sandbox, and loading
 * untrusted .jar/.dex files at runtime would be a real security
 * hole with no corresponding benefit for that goal).
 *
 * Third-party contributors add a plugin by implementing this
 * interface in a new Gradle module and registering it in
 * DevoraPluginRegistry — the same mechanism the built-in GitHub
 * Workflows plugin (spec section 12) uses.
 */
interface DevoraPlugin {
    val id: String
    val displayName: String
    val description: String

    /** Called once when Devora starts, after all plugins are registered. */
    fun onRegistered() {}
}

/**
 * A plugin that provides YAML tooling for workflow files — syntax
 * highlighting hints, autocomplete suggestions, and validation.
 * This is the extension point spec section 12 describes; the plugin
 * never executes workflow steps itself (that stays in
 * :feature:workflow-engine's Built-in Runner, per spec section 12:
 * "Plugin ini bukan workflow execution engine").
 */
interface WorkflowYamlToolingPlugin : DevoraPlugin {
    fun provideKnownKeys(): List<YamlKeyHint>

    fun provideKnownActionPrefixes(): List<String>

    fun validate(yamlContent: String): List<YamlValidationIssue>
}

data class YamlKeyHint(
    val key: String,
    val description: String,
    val validParent: String? // e.g. "steps[].with" — null means top-level
)

enum class YamlValidationSeverity { WARNING, ERROR }

data class YamlValidationIssue(
    val lineNumber: Int,
    val message: String,
    val severity: YamlValidationSeverity
)