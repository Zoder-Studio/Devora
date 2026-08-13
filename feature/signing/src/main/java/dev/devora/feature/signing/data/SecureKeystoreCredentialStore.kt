package dev.devora.feature.signing.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.devora.core.common.result.DevoraResult

/**
 * Stores keystore/key passwords encrypted via Android Keystore-backed
 * EncryptedSharedPreferences. Never logged, never exposed after save
 * (spec section 22-23: credentials stored securely, no silent
 * signing decisions). Only the password bytes are encrypted here —
 * the .keystore file itself lives on the project filesystem as a
 * normal file, exactly like a real keystore would.
 */
class SecureKeystoreCredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "devora_keystore_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePasswords(keystoreId: String, keystorePassword: String, keyPassword: String): DevoraResult<Unit> {
        return try {
            prefs.edit()
                .putString("$keystoreId.keystore_password", keystorePassword)
                .putString("$keystoreId.key_password", keyPassword)
                .apply()
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to securely store keystore credentials", cause = e)
        }
    }

    fun readPasswords(keystoreId: String): DevoraResult<Pair<String, String>> {
        val keystorePassword = prefs.getString("$keystoreId.keystore_password", null)
        val keyPassword = prefs.getString("$keystoreId.key_password", null)
        return if (keystorePassword != null && keyPassword != null) {
            DevoraResult.Success(keystorePassword to keyPassword)
        } else {
            DevoraResult.Failure(message = "No stored credentials found for keystore: $keystoreId")
        }
    }

    fun deletePasswords(keystoreId: String) {
        prefs.edit()
            .remove("$keystoreId.keystore_password")
            .remove("$keystoreId.key_password")
            .apply()
    }
}