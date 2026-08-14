package dev.devora.feature.accountsecurity.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.devora.core.common.result.DevoraResult
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

private const val KEY_ALIAS = "devora_device_identity_key"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"

/**
 * Generates and holds this device's identity key entirely inside the
 * hardware-backed Android Keystore. The private key material never
 * leaves the keystore and is never exported, copied, or serialized
 * anywhere (spec section 23: "Gunakan Android Keystore untuk device
 * key", section 27: "jangan simpan secret plaintext"). Only the
 * public key is ever read out — that's what gets registered with a
 * backend as this device's identity, if/when one exists.
 */
class DeviceKeyManager {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun hasDeviceKey(): Boolean = keyStore.containsAlias(KEY_ALIAS)

    fun generateDeviceKeyIfNeeded(): DevoraResult<Unit> {
        if (hasDeviceKey()) return DevoraResult.Success(Unit)

        return try {
            val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
                .build()
            generator.initialize(spec)
            generator.generateKeyPair()
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to generate device identity key", cause = e)
        }
    }

    /** The device's public key, base64-encoded — safe to send to a backend, never sensitive on its own. */
    fun publicKeyBase64(): DevoraResult<String> {
        return try {
            val cert = keyStore.getCertificate(KEY_ALIAS)
                ?: return DevoraResult.Failure(message = "Device key not found. Call generateDeviceKeyIfNeeded() first.")
            DevoraResult.Success(java.util.Base64.getEncoder().encodeToString(cert.publicKey.encoded))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to read device public key", cause = e)
        }
    }

    /** Signs a challenge (e.g. from a backend during login) using the hardware-backed private key. */
    fun signChallenge(challenge: ByteArray): DevoraResult<ByteArray> {
        return try {
            val privateKey = keyStore.getKey(KEY_ALIAS, null) as? java.security.PrivateKey
                ?: return DevoraResult.Failure(message = "Device key not found or not usable for signing.")
            val signature = Signature.getInstance("SHA256withECDSA").apply {
                initSign(privateKey)
                update(challenge)
            }
            DevoraResult.Success(signature.sign())
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to sign challenge with device key", cause = e)
        }
    }

    /** Deletes the device key — used by emergency revoke (spec section 25) so a revoked device can never sign as itself again. */
    fun deleteDeviceKey(): DevoraResult<Unit> {
        return try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to delete device key", cause = e)
        }
    }
}