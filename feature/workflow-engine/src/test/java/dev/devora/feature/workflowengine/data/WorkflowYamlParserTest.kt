package dev.devora.feature.workflowengine.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.workflowengine.domain.model.WorkflowStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WorkflowYamlParserTest {

    private val parser = WorkflowYamlParser()

    @Test
    fun `parses a real GitHub Actions style workflow`() {
        val tempFile = File.createTempFile("release", ".yml")
        tempFile.writeText(
            """
            name: Android Release
            on:
              workflow_dispatch:
            jobs:
              build:
                runs-on: ubuntu-latest
                steps:
                  - uses: actions/checkout@v4
                  - uses: actions/setup-java@v4
                    with:
                      distribution: temurin
                      java-version: '17'
                  - run: ./gradlew assembleRelease
            """.trimIndent()
        )

        val result = parser.parse(tempFile)

        assertTrue(result is DevoraResult.Success)
        val workflow = (result as DevoraResult.Success).data
        assertEquals("Android Release", workflow.name)
        assertEquals(1, workflow.jobs.size)

        val job = workflow.jobs.first()
        assertEquals("build", job.id)
        assertEquals(3, job.steps.size)
        assertTrue(job.steps[0] is WorkflowStep.UsesStep)
        assertEquals("actions/checkout@v4", (job.steps[0] as WorkflowStep.UsesStep).uses)
        assertTrue(job.steps[2] is WorkflowStep.RunStep)
        assertEquals("./gradlew assembleRelease", (job.steps[2] as WorkflowStep.RunStep).run)

        tempFile.delete()
    }
}