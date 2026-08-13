package dev.devora.feature.signing.domain.model

/**
 * Everything the developer explicitly provides when creating a
 * keystore (spec section 22: "User menentukan: Keystore name, Alias,
 * Password, Validity, Certificate information"). Devora never fills
 * in or guesses any of these values.
 */
data class KeystoreCreationRequest(
    val keystoreName: String,
    val alias: String,
    val keystorePassword: String,
    val keyPassword: String,
    val validityYears: Int,
    val commonName: String,
    val organizationalUnit: String?,
    val organization: String?,
    val locality: String?,
    val state: String?,
    val countryCode: String?
)

data class KeystoreEntry(
    val id: String,
    val keystoreFilePath: String,
    val alias: String,
    val createdAtEpochMillis: Long
)

data class SigningRequest(
    val apkFilePath: String,
    val outputApkFilePath: String,
    val keystoreId: String,
    val v1Enabled: Boolean = true,
    val v2Enabled: Boolean = true,
    val v3Enabled: Boolean = true
)