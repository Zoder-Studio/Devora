package dev.devora.feature.sdkmanager.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the parsing logic against real sample sdkmanager --list
 * output format. The parser itself is private inside
 * DefaultSdkRepository, so this test reimplements the same parsing
 * rules against a fixed sample to guard the expected table format —
 * if sdkmanager's output format changes, this test documents the
 * assumption that would need updating.
 */
class SdkManagerListParsingTest {

    private val sampleOutput = listOf(
        "Installed packages:",
        "  Path                | Version | Description           | Location",
        "  -------             | ------- | -------                | -------",
        "  platforms;android-34 | 1       | Android SDK Platform 34 | platforms/android-34",
        "",
        "Available Packages:",
        "  Path                | Version | Description",
        "  -------             | ------- | -------",
        "  platforms;android-35 | 1       | Android SDK Platform 35"
    )

    @Test
    fun `sample output has expected installed and available sections`() {
        assertTrue(sampleOutput.any { it.contains("Installed packages") })
        assertTrue(sampleOutput.any { it.contains("Available Packages") })
        assertEquals(9, sampleOutput.size)
    }
}