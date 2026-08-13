package dev.devora.feature.signing.data.repository

import com.android.apksig.ApkSigner
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.signing.data.KeystoreRegistryStore
import dev.devora.feature.signing.data.SecureKeystoreCredentialStore
import dev.devora.feature.signing.domain.model.KeystoreCreationRequest
import dev.devora.feature.signing.domain.model.KeystoreEntry
import dev.devora.feature.signing.domain.model.SigningRequest
import dev.devora.feature.signing.domain.repository.SigningRepository
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.UUID

class DefaultSigningRepository(
    private val engineProvider: CommandExecutionEngineProvider,
    private val enginePaths: EnginePaths,
    private val registryStore: KeystoreRegistryStore,
    private val credentialStore: SecureKeystoreCredentialStore
) : SigningRepository {

    override suspend fun listKeystores(): List<KeystoreEntry> = registryStore.readAll()

    override suspend fun createKeystore(
        request: KeystoreCreationRequest,
        destinationDir: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<KeystoreEntry> {
        val keystoreFile = File(destinationDir, "${request.keystoreName}.keystore")
        if (keystoreFile.exists()) {
            return DevoraResult.Failure(message = "Keystore already exists: ${keystoreFile.absolutePath}")
        }

        val validityDays = request.validityYears * 365
        val dn = buildDistinguishedName(request)

        // keytool is bundled with the JDK already installed via SDK Manager (Stage 6) / bundletool
        // setup (Stage 13) — Devora does not ship or reimplement a keystore format of its own.
        val script = buildString {
            append("keytool -genkeypair -v ")
            append("-keystore '${keystoreFile.absolutePath}' ")
            append("-alias '${request.alias}' ")
            append("-keyalg RSA -keysize 2048 ")
            append("-validity $validityDays ")
            append("-storepass '${request.keystorePassword}' ")
            append("-keypass '${request.keyPassword}' ")
            append("-dname '$dn'")
        }

        val result = engineProvider.current().run(
            workingDirectory = enginePaths.currentPrefixPath(),
            script = script,
            onOutputLine = { line ->
                // never echo the command line itself (it contains passwords) — only forward keytool's own output
                if (!line.contains(request.keystorePassword) && !line.contains(request.keyPassword)) {
                    onOutputLine(line)
                }
            }
        )

        if (result is DevoraResult.Failure) {
            return DevoraResult.Failure(message = "keytool failed to create keystore", cause = result.cause)
        }
        if (!keystoreFile.exists()) {
            return DevoraResult.Failure(message = "keytool reported success but no keystore file was created")
        }

        val entry = KeystoreEntry(
            id = UUID.randomUUID().toString(),
            keystoreFilePath = keystoreFile.absolutePath,
            alias = request.alias,
            createdAtEpochMillis = System.currentTimeMillis()
        )

        val credentialResult = credentialStore.savePasswords(entry.id, request.keystorePassword, request.keyPassword)
        if (credentialResult is DevoraResult.Failure) {
            keystoreFile.delete()
            return DevoraResult.Failure(message = "Keystore created but failed to store credentials securely", cause = credentialResult.cause)
        }

        val registryResult = registryStore.add(entry)
        if (registryResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = registryResult.message, cause = registryResult.cause)
        }

        return DevoraResult.Success(entry)
    }

    override suspend fun signApk(request: SigningRequest, onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        val entry = listKeystores().find { it.id == request.keystoreId }
            ?: return DevoraResult.Failure(message = "Keystore not found: ${request.keystoreId}")

        val credentialsResult = credentialStore.readPasswords(entry.id)
        if (credentialsResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = credentialsResult.message)
        }
        val (keystorePassword, keyPassword) = (credentialsResult as DevoraResult.Success).data

        val keystoreFile = File(entry.keystoreFilePath)
        if (!keystoreFile.exists()) {
            return DevoraResult.Failure(message = "Keystore file no longer exists: ${entry.keystoreFilePath}")
        }
        val apkFile = File(request.apkFilePath)
        if (!apkFile.exists()) {
            return DevoraResult.Failure(message = "APK to sign does not exist: ${request.apkFilePath}")
        }

        return try {
            onOutputLine("Loading keystore...")
            val keyStore = KeyStore.getInstance("JKS")
            keystoreFile.inputStream().use { keyStore.load(it, keystorePassword.toCharArray()) }

            val privateKey = keyStore.getKey(entry.alias, keyPassword.toCharArray()) as? java.security.PrivateKey
                ?: return DevoraResult.Failure(message = "No private key found for alias '${entry.alias}' in keystore")

            val certChain = keyStore.getCertificateChain(entry.alias)
                ?.filterIsInstance<X509Certificate>()
                ?: return DevoraResult.Failure(message = "No certificate chain found for alias '${entry.alias}'")

            val signerConfig = ApkSigner.SignerConfig.Builder(
                entry.alias,
                privateKey,
                certChain
            ).build()

            onOutputLine("Signing APK with apksig (V1=${request.v1Enabled}, V2=${request.v2Enabled}, V3=${request.v3Enabled})...")

            val outputFile = File(request.outputApkFilePath)
            outputFile.parentFile?.mkdirs()

            val signer = ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(apkFile)
                .setOutputApk(outputFile)
                .setV1SigningEnabled(request.v1Enabled)
                .setV2SigningEnabled(request.v2Enabled)
                .setV3SigningEnabled(request.v3Enabled)
                .build()

            signer.sign()

            onOutputLine("Signed APK written to: ${outputFile.absolutePath}")
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to sign APK: ${e.message}", cause = e)
        }
    }

    private fun buildDistinguishedName(request: KeystoreCreationRequest): String {
        val parts = mutableListOf("CN=${request.commonName}")
        request.organizationalUnit?.let { parts.add("OU=$it") }
        request.organization?.let { parts.add("O=$it") }
        request.locality?.let { parts.add("L=$it") }
        request.state?.let { parts.add("ST=$it") }
        request.countryCode?.let { parts.add("C=$it") }
        return parts.joinToString(", ")
    }
}