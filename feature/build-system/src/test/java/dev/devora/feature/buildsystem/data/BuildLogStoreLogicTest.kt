package dev.devora.feature.buildsystem.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Exercises the tail/count logic against a real file, independent of
 * Android Context (BuildLogStore itself needs Context, so this test
 * validates the same algorithm against a plain File to keep it a fast
 * JVM unit test rather than an instrumented one).
 */
class BuildLogStoreLogicTest {

    @Test
    fun `tail returns last N lines when file has more than N lines`() {
        val tempFile = File.createTempFile("build-log-test", ".log")
        tempFile.writeText((1..250).joinToString("\n") { "line $it" })

        val allLines = tempFile.readLines()
        val tail = if (allLines.size <= 100) allLines else allLines.takeLast(100)

        assertEquals(100, tail.size)
        assertEquals("line 151", tail.first())
        assertEquals("line 250", tail.last())

        tempFile.delete()
    }
}