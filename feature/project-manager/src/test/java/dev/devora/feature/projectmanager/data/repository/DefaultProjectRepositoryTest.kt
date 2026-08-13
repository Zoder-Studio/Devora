package dev.devora.feature.projectmanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.projectmanager.data.local.ProjectRegistryStore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class DefaultProjectRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var registryFile: File
    private lateinit var repository: DefaultProjectRepository

    @Before
    fun setUp() {
        tempDir = createTempDir(prefix = "devora-test")
        registryFile = File(tempDir, "registry.json")
        repository = DefaultProjectRepository(ProjectRegistryStore(registryFile))
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `importing a directory without settings gradle fails`() = runTest {
        val projectDir = File(tempDir, "NoGradleProject").apply { mkdirs() }

        val result = repository.importExistingProject(projectDir.absolutePath)

        assertTrue(result is DevoraResult.Failure)
    }

    @Test
    fun `importing a valid gradle project succeeds and lists it`() = runTest {
        val projectDir = File(tempDir, "RealProject").apply { mkdirs() }
        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"RealProject\"")

        val importResult = repository.importExistingProject(projectDir.absolutePath)
        assertTrue(importResult is DevoraResult.Success)

        val listResult = repository.listProjects()
        assertTrue(listResult is DevoraResult.Success)
        val projects = (listResult as DevoraResult.Success).data
        assertEquals(1, projects.size)
        assertEquals("RealProject", projects.first().name)
        assertEquals(false, projects.first().hasDevoraMetadata)
    }

    @Test
    fun `initializing devora metadata creates workflows secrets and environments dirs`() = runTest {
        val projectDir = File(tempDir, "MetaProject").apply { mkdirs() }
        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"MetaProject\"")
        repository.importExistingProject(projectDir.absolutePath)

        val result = repository.initializeDevoraMetadata(projectDir.absolutePath)

        assertTrue(result is DevoraResult.Success)
        assertTrue(File(projectDir, ".devora/workflows").isDirectory)
        assertTrue(File(projectDir, ".devora/secrets").isDirectory)
        assertTrue(File(projectDir, ".devora/environments").isDirectory)
        assertTrue(File(projectDir, ".devora/.gitignore").exists())
    }

    @Test
    fun `removing an unknown project id fails`() = runTest {
        val result = repository.removeFromRegistry("does-not-exist")

        assertTrue(result is DevoraResult.Failure)
    }
}