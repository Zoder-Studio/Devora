package dev.devora.feature.pluginsystem.builtin

import dev.devora.feature.pluginsystem.api.YamlValidationSeverity
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubWorkflowsPluginTest {

    private val plugin = GitHubWorkflowsPlugin()

    @Test
    fun `valid workflow produces no errors`() {
        val yaml = """
            name: CI
            on:
              workflow_dispatch:
            jobs:
              build:
                runs-on: ubuntu-latest
                steps:
                  - run: echo hello
        """.trimIndent()

        val issues = plugin.validate(yaml)
        assertTrue(issues.none { it.severity == YamlValidationSeverity.ERROR })
    }

    @Test
    fun `missing jobs section is an error`() {
        val yaml = """
            name: Broken
            on:
              workflow_dispatch:
        """.trimIndent()

        val issues = plugin.validate(yaml)
        assertTrue(issues.any { it.severity == YamlValidationSeverity.ERROR && it.message.contains("jobs") })
    }

    @Test
    fun `missing runs-on is only a warning`() {
        val yaml = """
            name: Test
            on:
              workflow_dispatch:
            jobs:
              build:
                steps:
                  - run: echo hi
        """.trimIndent()

        val issues = plugin.validate(yaml)
        assertTrue(issues.any { it.severity == YamlValidationSeverity.WARNING && it.message.contains("runs-on") })
        assertTrue(issues.none { it.severity == YamlValidationSeverity.ERROR })
    }
}