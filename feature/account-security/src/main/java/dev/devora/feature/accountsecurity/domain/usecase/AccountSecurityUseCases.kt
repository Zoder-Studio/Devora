package dev.devora.feature.accountsecurity.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.accountsecurity.domain.model.Dpat
import dev.devora.feature.accountsecurity.domain.model.DpatExpirationOption
import dev.devora.feature.accountsecurity.domain.repository.AccountSecurityRepository

class GetCurrentDpatUseCase(private val repository: AccountSecurityRepository) {
    suspend operator fun invoke(): Dpat? = repository.getCurrentDpat()
}

class CreateDpatUseCase(private val repository: AccountSecurityRepository) {
    suspend operator fun invoke(
        expiration: DpatExpirationOption,
        phoneLabel: String?,
        makePrimary: Boolean
    ): DevoraResult<Dpat> = repository.createDpat(expiration, phoneLabel, makePrimary)
}

class RevokeLocalDpatUseCase(private val repository: AccountSecurityRepository) {
    suspend operator fun invoke(): DevoraResult<Unit> = repository.revokeLocalDpat()
}

class IsCrossDeviceEnforcementAvailableUseCase(private val repository: AccountSecurityRepository) {
    operator fun invoke(): Boolean = repository.isCrossDeviceEnforcementAvailable()
}