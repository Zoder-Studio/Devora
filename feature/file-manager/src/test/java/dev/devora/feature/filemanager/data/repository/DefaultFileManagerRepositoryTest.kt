package dev.devora.feature.filemanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.filemanager.domain.model.FileNodeType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class DefaultFileManagerRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var repository: DefaultFileManagerRepository

    @Before
    fun setUp() {
        tempDir = createTempDir(prefix = "devora-fm-test")
        repository = DefaultFileManagerRepository()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `listing directory includes hidden files as data not filtered`() = runTest {
        File(tempDir, "visible.txt").createNewFile()
        File(tempDir, ".hidden").createNewFile()

        val result = repository.listDirectory(tempDir.absolutePath)

        assertTrue(result is DevoraResult.Success)
        val entries = (result as DevoraResult.Success).data.entries
        assertEquals(2, entries.size)
        assertTrue(entries.any { it.name == ".hidden" && it.isHidden })
    }

    @Test
    fun `creating a file that already exists fails`() = runTest {
        File(tempDir, "dup.txt").createNewFile()

        val result = repository.createFile(tempDir.absolutePath, "dup.txt")

        assertTrue(result is DevoraResult.Failure)
    }

    @Test
    fun `renaming updates the node path`() = runTest {
        val original = File(tempDir, "before.txt").apply { createNewFile() }

        val result = repository.rename(original.absolutePath, "after.txt")

        assertTrue(result is DevoraResult.Success)
        val node = (result as DevoraResult.Success).data
        assertEquals("after.txt", node.name)
        assertEquals(FileNodeType.FILE, node.type)
    }

    @Test
    fun `deleting a directory removes it recursively`() = runTest {
        val dir = File(tempDir, "toDelete").apply { mkdirs() }
        File(dir, "inner.txt").createNewFile()

        val result = repository.delete(dir.absolutePath)

        assertTrue(result is DevoraResult.Success)
        assertTrue(!dir.exists())
    }
}