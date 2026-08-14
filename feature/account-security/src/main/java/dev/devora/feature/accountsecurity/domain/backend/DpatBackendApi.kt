package dev.devora.feature.accountsecurity.domain.backend

import dev.devora.core.common.result.DevoraResult

/**
 * Contract for the server-side coordination that spec sections 24-25
 * require (detecting another active device, revoking sessions across
 * devices). Devora's architecture through Stage 19 is on-device only
 * — no Devora backend has been specified anywhere in this project.
 *
 * "Single device account" and "emergency revoke locks other devices"
 * are inherently cross-device concepts: no on-device-only app can
 * know about a session on a *different* physical device without a
 * server telling it so. This interface exists so a real backend can
 * be plugged in later without redesigning the local DPAT flow — until
 * then, NoBackendDpatApi below is used, and Devora is honest in the
 * UI that cross-device enforcement is unavailable rather than faking it.
 */
interface DpatBackendApi {
    suspend fun registerDevice(publicKeyBase64: String, dpatId: String): DevoraResult<Unit>

    suspend fun checkForOtherActiveDevice(dpatId: String): DevoraResult<Boolean>

    suspend fun revokeAllOtherSessions(dpatId: String, signature: ByteArray): DevoraResult<Unit>
}

/**
 * Honest no-op used until a real backend exists. Every method fails
 * explicitly rather than pretending to succeed — spec section 1:
 * never fake behavior, never hide the real state.
 */
class NoBackendDpatApi : DpatBackendApi {
    override suspend fun registerDevice(publicKeyBase64: String, dpatId: String): DevoraResult<Unit> =
        DevoraResult.Failure(message = "No Devora backend is configured. Cross-device registration is unavailable.")

    override suspend fun checkForOtherActiveDevice(dpatId: String): DevoraResult<Boolean> =
        DevoraResult.Failure(message = "No Devora backend is configured. Cannot check for other active devices.")

    override suspend fun revokeAllOtherSessions(dpatId: String, signature: ByteArray): DevoraResult<Unit> =
        DevoraResult.Failure(message = "No Devora backend is configured. Cannot revoke sessions on other devices.")
}