package dev.devora.feature.accountsecurity.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.accountsecurity.data.DeviceKeyManager
import dev.devora.feature.accountsecurity.data.DpatStore
import dev.devora.feature.accountsecurity.domain.backend.DpatBackendApi
import dev.devora.feature.accountsecurity.domain.backend.NoBackendDpatApi
import dev.devora.feature.accountsecurity.domain.model.Dpat
import dev.devora.feature.accountsecurity.domain.model.DpatExpirationOption
import dev.devora.feature.accountsecurity.domain.repository.AccountSecurityRepository
import java.util.UUID
import java.util.concurrent.TimeUnit

class DefaultAccountSecurityRepository(
    private val deviceKeyManager: DeviceKeyManager,
    private val dpatStore: DpatStore,
    private val backendApi: DpatBackendApi
) : AccountSecurityRepository {

    override suspend fun getCurrentDpat(): Dpat? = dpatStore.read()

    override suspend fun createDpat(
        expiration: DpatExpirationOption,
        phoneLabel: String?,
        makePrimary: Boolean
    ): DevoraResult<Dpat> {
        val keyResult = deviceKeyManager.generateDeviceKeyIfNeeded()
        if (keyResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = keyResult.message, cause = keyResult.cause)
        }

        val now = System.currentTimeMillis()
        val expiresAt = expiration.days?.let { days -> now + TimeUnit.DAYS.toMillis(days.toLong()) }

        val dpat = Dpat(
            id = UUID.randomUUID().toString(),
            phoneLabel = phoneLabel,
            isPrimaryPhone = makePrimary,
            createdAtEpochMillis = now,
            expiresAtEpochMillis = expiresAt
        )

        val saveResult = dpatStore.save(dpat)
        if (saveResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = saveResult.message, cause = saveResult.cause)
        }

        // Best-effort: register with backend if one is configured. Failure here
        // does not fail DPAT creation itself — the DPAT is still valid locally,
        // it just won't have cross-device enforcement (honestly reported via
        // isCrossDeviceEnforcementAvailable()).
        val publicKeyResult = deviceKeyManager.publicKeyBase64()
        if (publicKeyResult is DevoraResult.Success) {
            backendApi.registerDevice(publicKeyResult.data, dpat.id)
        }

        return DevoraResult.Success(dpat)
    }

    override suspend fun revokeLocalDpat(): DevoraResult<Unit> {
        val keyDeleteResult = deviceKeyManager.deleteDeviceKey()
        if (keyDeleteResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = keyDeleteResult.message, cause = keyDeleteResult.cause)
        }
        return dpatStore.clear()
    }

    override fun isCrossDeviceEnforcementAvailable(): Boolean = backendApi !is NoBackendDpatApi
}