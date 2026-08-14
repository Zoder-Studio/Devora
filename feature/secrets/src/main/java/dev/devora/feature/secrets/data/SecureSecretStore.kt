package dev.devora.feature.secrets.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.devora.core.common.result.DevoraResult

/**
 * Secret values are encrypted at rest via Android Keystore-backed
 * EncryptedSharedPreferences and are never written to any project
 * file or build log (spec section 16-27: "jangan menampilkan secret
 * plaintext setelah disimpan", "jangan memasukkan DPAT/secrets ke
 * build log"). DevoraLogger is never called with a secret value
 * anywhere in this module.
 */
class SecureSecretStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "devora_secrets_values",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveValue(secretId: String, value: String): DevoraResult<Unit> {
        return try {
            prefs.edit().putString(secretId, value).apply()
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to store secret value securely", cause = e)
        }
    }

    /** Only ever called at the moment of pushing to GitHub — never surfaced in any UI list. */
    fun readValue(secretId: String): String? = prefs.getString(secretId, null)

    fun deleteValue(secretId: String) {
        prefs.edit().remove(secretId).apply()
    }
}