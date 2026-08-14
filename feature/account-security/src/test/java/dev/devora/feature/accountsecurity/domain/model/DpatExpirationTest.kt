package dev.devora.feature.accountsecurity.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DpatExpirationTest {

    @Test
    fun `dpat with past expiration is expired`() {
        val dpat = Dpat(
            id = "test", phoneLabel = null, isPrimaryPhone = true,
            createdAtEpochMillis = 1000L, expiresAtEpochMillis = 2000L
        )
        assertTrue(dpat.isExpired(nowEpochMillis = 3000L))
    }

    @Test
    fun `dpat with never expiration is never expired`() {
        val dpat = Dpat(
            id = "test", phoneLabel = null, isPrimaryPhone = true,
            createdAtEpochMillis = 1000L, expiresAtEpochMillis = null
        )
        assertFalse(dpat.isExpired(nowEpochMillis = Long.MAX_VALUE))
    }
}