package dev.devora.feature.secrets.data

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import dev.devora.core.common.result.DevoraResult
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

/**
 * Implements GitHub's documented Secrets API contract exactly:
 * 1. GET the repo's public key (base64-encoded, libsodium format)
 * 2. Seal the secret value with libsodium's crypto_box_seal using that key
 * 3. PUT the base64-encoded sealed box as encrypted_value
 * See: https://docs.github.com/en/rest/actions/secrets
 *
 * lazysodium-android is the real libsodium binding — Devora does not
 * implement NaCl/sealed-box cryptography itself (rolling your own
 * crypto primitive is exactly the kind of thing spec section 32 warns
 * against: use the real, maintained library).
 */
class GitHubSecretsApi {

    private val sodium = LazySodiumAndroid(SodiumAndroid())

    fun putSecret(
        token: String,
        owner: String,
        repo: String,
        secretName: String,
        secretValue: String
    ): DevoraResult<Unit> {
        return try {
            val publicKeyResult = fetchPublicKey(token, owner, repo)
            if (publicKeyResult is DevoraResult.Failure) {
                return DevoraResult.Failure(message = publicKeyResult.message, cause = publicKeyResult.cause)
            }
            val (keyId, publicKeyBase64) = (publicKeyResult as DevoraResult.Success).data

            val publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64)
            val publicKey = Key.fromBytes(publicKeyBytes)

            val sealedBox = ByteArray(secretValue.toByteArray().size + com.goterl.lazysodium.interfaces.Box.SEALBYTES)
            val secretBytes = secretValue.toByteArray()
            val success = sodium.cryptoBoxSeal(sealedBox, secretBytes, secretBytes.size.toLong(), publicKey.asBytes)
            if (!success) {
                return DevoraResult.Failure(message = "libsodium sealed box encryption failed")
            }

            val encryptedValueBase64 = Base64.getEncoder().encodeToString(sealedBox)

            val body = JSONObject().apply {
                put("encrypted_value", encryptedValueBase64)
                put("key_id", keyId)
            }.toString()

            putSecretRequest(token, owner, repo, secretName, body)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to push secret to GitHub: ${e.message}", cause = e)
        }
    }

    private fun fetchPublicKey(token: String, owner: String, repo: String): DevoraResult<Pair<String, String>> {
        return try {
            val connection = URL("https://api.github.com/repos/$owner/$repo/actions/secrets/public-key")
                .openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $token")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return DevoraResult.Failure(message = "Failed to fetch repo public key: HTTP ${connection.responseCode}")
            }

            val json = JSONObject(connection.inputStream.bufferedReader().readText())
            DevoraResult.Success(json.getString("key_id") to json.getString("key"))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to fetch repository public key", cause = e)
        }
    }

    private fun putSecretRequest(
        token: String,
        owner: String,
        repo: String,
        secretName: String,
        body: String
    ): DevoraResult<Unit> {
        return try {
            val connection = URL("https://api.github.com/repos/$owner/$repo/actions/secrets/$secretName")
                .openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toByteArray()) }

            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_NO_CONTENT) {
                DevoraResult.Success(Unit)
            } else {
                DevoraResult.Failure(message = "GitHub secrets API returned HTTP $code")
            }
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to PUT secret to GitHub", cause = e)
        }
    }
}