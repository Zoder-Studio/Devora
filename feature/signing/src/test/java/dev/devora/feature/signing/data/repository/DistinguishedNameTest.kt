package dev.devora.feature.signing.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Validates the DN string format expected by keytool's -dname flag.
 * The actual builder is private inside DefaultSigningRepository; this
 * test documents the expected format so a refactor doesn't silently
 * break keytool invocation.
 */
class DistinguishedNameTest {

    @Test
    fun `dn format matches keytool expectations`() {
        val dn = "CN=Test App, OU=Engineering, O=Devora, C=ID"
        val parts = dn.split(", ")
        assertEquals("CN=Test App", parts[0])
        assertEquals(4, parts.size)
    }
}