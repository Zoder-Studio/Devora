package dev.devora.feature.artifactmanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.model.ArtifactType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class DefaultArtifactRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var stagingDir: File
    private lateinit var repository: DefaultArtifactRepository

    @Before
    fun setUp() {
        tempDir = createTempDir(prefix = "devora-artifact-test")
        stagingDir = File(tempDir, "staging")
        repository = DefaultArtifactRepository(stagingDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `scans apk in standard AGP output path`() {
        val outputDir = File(tempDir, "project/app/build/outputs/apk/debug").apply { mkdirs() }
        val apkFile = File(outputDir, "app-debug.apk").apply { writeText("fake apk content") }

        val result = repository.scanProjectArtifacts(File(tempDir, "project").absolutePath)

        assertTrue(result is DevoraResult.Success)
        val artifacts = (result as DevoraResult.Success).data
        assertEquals(1, artifacts.size)
        assertEquals(ArtifactType.APK, artifacts.first().type)

        val expectedHash = MessageDigest.getInstance("SHA-256")
            .digest(apkFile.readBytes())
            .joinToString("") { "%02x".format(it) }
        assertEquals(expectedHash, artifacts.first().sha256)
    }

    @Test
    fun `ignores non-apk-aab files in output directory`() {
        val outputDir = File(tempDir, "project/app/build/outputs/apk/debug").apply { mkdirs() }
        File(outputDir, "output-metadata.json").writeText("{}")

        val result = repository.scanProjectArtifacts(File(tempDir, "project").absolutePath)

        assertTrue(result is DevoraResult.Success)
        assertEquals(0, (result as DevoraResult.Success).data.size)
    }
}