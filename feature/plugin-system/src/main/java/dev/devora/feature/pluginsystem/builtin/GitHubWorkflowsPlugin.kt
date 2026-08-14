package dev.devora.feature.pluginsystem.builtin

import dev.devora.feature.pluginsystem.api.WorkflowYamlToolingPlugin
import dev.devora.feature.pluginsystem.api.YamlKeyHint
import dev.devora.feature.pluginsystem.api.YamlValidationIssue
import dev.devora.feature.pluginsystem.api.YamlValidationSeverity
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

/**
 * Official Devora plugin providing tooling for GitHub Actions-shaped
 * workflow YAML (spec section 12). This mirrors real GitHub Actions
 * syntax exactly — jobs, steps, uses, with, env, secrets, expressions
 * — it does not invent new keys of its own.
 */
class GitHubWorkflowsPlugin : WorkflowYamlToolingPlugin {

    override val id: String = "devora.builtin.github-workflows"
    override val displayName: String = "GitHub Workflows"
    override val description: String = "Syntax hints, autocomplete, and validation for GitHub Actions-compatible workflow YAML."

    private val yaml = Yaml()

    override fun provideKnownKeys(): List<YamlKeyHint> = listOf(
        YamlKeyHint("name", "The name of the workflow, shown in the GitHub Actions UI.", null),
        YamlKeyHint("on", "Events that trigger the workflow.", null),
        YamlKeyHint("jobs", "One or more jobs the workflow runs.", null),
        YamlKeyHint("runs-on", "The runner environment for a job.", "jobs.<job_id>"),
        YamlKeyHint("steps", "The sequence of tasks a job performs.", "jobs.<job_id>"),
        YamlKeyHint("uses", "Runs a reusable action, either from the marketplace or a local path.", "steps[]"),
        YamlKeyHint("run", "Runs a shell command.", "steps[]"),
        YamlKeyHint("with", "Input parameters passed to an action.", "steps[]"),
        YamlKeyHint("env", "Environment variables for a step or job.", "steps[] or jobs.<job_id>"),
        YamlKeyHint("working-directory", "The working directory for a run step.", "steps[]"),
        YamlKeyHint("secrets", "References to repository secrets, e.g. \${{ secrets.NAME }}.", null),
        YamlKeyHint("permissions", "GitHub Actions token permissions (not Devora's own Workflow Permissions).", null)
    )

    override fun provideKnownActionPrefixes(): List<String> = listOf(
        "actions/checkout",
        "actions/setup-java",
        "actions/upload-artifact"
        // Kept in sync with SupportedAction in :feature:workflow-engine.
        // Anything outside this list is still valid YAML — it just won't
        // execute locally in Devora's Built-in Runner (see spec section 12:
        // this plugin provides tooling only, not execution).
    )

    override fun validate(yamlContent: String): List<YamlValidationIssue> {
        val issues = mutableListOf<YamlValidationIssue>()

        val parsed = try {
            @Suppress("UNCHECKED_CAST")
            yaml.load<Map<String, Any?>>(yamlContent) as? Map<String, Any?>
        } catch (e: YAMLException) {
            issues.add(
                YamlValidationIssue(
                    lineNumber = extractLineNumber(e.message),
                    message = "YAML syntax error: ${e.message}",
                    severity = YamlValidationSeverity.ERROR
                )
            )
            return issues
        }

        if (parsed == null) {
            issues.add(YamlValidationIssue(1, "Workflow file is empty.", YamlValidationSeverity.ERROR))
            return issues
        }

        if (!parsed.containsKey("jobs")) {
            issues.add(YamlValidationIssue(1, "Missing required 'jobs:' section.", YamlValidationSeverity.ERROR))
        }
        if (!parsed.containsKey("on")) {
            issues.add(YamlValidationIssue(1, "Missing 'on:' trigger — GitHub Actions requires at least one.", YamlValidationSeverity.WARNING))
        }

        @Suppress("UNCHECKED_CAST")
        val jobs = parsed["jobs"] as? Map<String, Any?>
        jobs?.forEach { (jobId, jobValue) ->
            @Suppress("UNCHECKED_CAST")
            val jobMap = jobValue as? Map<String, Any?>
            if (jobMap == null) {
                issues.add(YamlValidationIssue(1, "Job '$jobId' is not a valid mapping.", YamlValidationSeverity.ERROR))
                return@forEach
            }
            if (!jobMap.containsKey("steps")) {
                issues.add(YamlValidationIssue(1, "Job '$jobId' has no 'steps:'.", YamlValidationSeverity.ERROR))
            }
            if (!jobMap.containsKey("runs-on")) {
                issues.add(YamlValidationIssue(1, "Job '$jobId' has no 'runs-on:'.", YamlValidationSeverity.WARNING))
            }
        }

        return issues
    }

    /** SnakeYAML error messages include "line N" — best-effort extraction, falls back to line 1 if the format changes. */
    private fun extractLineNumber(message: String?): Int {
        val match = Regex("""line (\d+)""").find(message.orEmpty())
        return match?.groupValues?.get(1)?.toIntOrNull()?.plus(1) ?: 1
    }
}