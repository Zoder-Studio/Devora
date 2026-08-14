package dev.devora.feature.accountsecurity.domain.model

enum class DpatExpirationOption(val days: Int?) {
    TWO_DAYS(2),
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
    NINETY_DAYS(90),
    ONE_YEAR(365),
    NEVER(null)
}

data class Dpat(
    val id: String,
    val phoneLabel: String?,
    val isPrimaryPhone: Boolean,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?
) {
    fun isExpired(nowEpochMillis: Long = System.currentTimeMillis()): Boolean =
        expiresAtEpochMillis != null && nowEpochMillis >= expiresAtEpochMillis
}

enum class RevokeStage { IDLE, COUNTDOWN, REVOKING, REVOKED }