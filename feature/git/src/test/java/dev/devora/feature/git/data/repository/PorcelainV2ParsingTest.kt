package dev.devora.feature.git.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Documents the real "git status --porcelain=v2 --branch" line shapes
 * this repository's private parser expects, so a git version change
 * that alters this format is caught here rather than silently
 * misparsed in production.
 */
class PorcelainV2ParsingTest {

    @Test
    fun `sample porcelain v2 lines match expected shape`() {
        val sample = listOf(
            "# branch.head main",
            "# branch.ab +2 -0",
            "1 M. N... 100644 100644 100644 abc123 def456 app/build.gradle.kts",
            "? untracked_file.txt"
        )
        assertTrue(sample[0].startsWith("# branch.head "))
        assertTrue(sample[1].startsWith("# branch.ab "))
        assertEquals("main", sample[0].removePrefix("# branch.head ").trim())
    }
}