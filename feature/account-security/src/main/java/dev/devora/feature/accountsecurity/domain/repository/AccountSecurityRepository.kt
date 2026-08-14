package dev.devora.feature.accountsecurity.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.accountsecurity.domain.model.Dpat
import dev.devora.feature.accountsecurity.domain.model.DpatExpirationOption

interface AccountSecurityRepository {
    suspend fun getCurrentDpat(): Dpat?

    suspend fun createDpat(
        expiration: DpatExpirationOption,
        phoneLabel: String?,
        makePrimary: Boolean
    ): DevoraResult<Dpat>

    /** Local-only revoke: deletes the device key and clears the DPAT on THIS device. Cross-device revocation requires a backend — see DpatBackendApi. */
    suspend fun revokeLocalDpat(): DevoraResult<Unit>

    fun isCrossDeviceEnforcementAvailable(): Boolean
}