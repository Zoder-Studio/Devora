package dev.devora.feature.artifactmanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.model.Artifact
import dev.devora.feature.artifactmanager.domain.model.ArtifactSource
import dev.devora.feature.artifactmanager.domain.model.ArtifactType
import dev.devora.feature.artifactmanager.domain.repository.ArtifactRepository
import java.io.File
import java.security.MessageDigest

class DefaultArtifactRepository(
    private val stagingRoot: File
) : ArtifactRepository {

    /**
     * Real, standard Gradle Android output locations. These paths are
     * fixed by the Android Gradle Plugin itself, not something Devora
     * invents — Devora only knows where AGP conventionally places
     * outputs and reads what's actually there.
     */
    private val relativeOutputDirs = listOf(
        "app/build/outputs/apk",
        "app/build/outputs/bundle"
    )

    override fun scanProjectArtifacts(projectRootPath: String): DevoraResult<List<Artifact>> {
        return try {
            val artifacts = mutableListOf<Artifact>()
            for (relativeDir in relativeOutputDirs) {
                val dir = File(projectRootPath, relativeDir)
                if (dir.exists()) {
                    artifacts.addAll(scanDirectoryRecursive(dir, ArtifactSource.BUILD_OUTPUT))
                }
            }
            DevoraResult.Success(artifacts.sortedByDescending { it.createdAtEpochMillis })
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to scan project artifacts", cause = e)
        }
    }

    override fun scanStagedArtifacts(): DevoraResult<List<Artifact>> {
        return try {
            if (!stagingRoot.exists()) return DevoraResult.Success(emptyList())
            val artifacts = scanDirectoryRecursive(stagingRoot, ArtifactSource.WORKFLOW_STAGED)
            DevoraResult.Success(artifacts.sortedByDescending { it.createdAtEpochMillis })
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to scan staged artifacts", cause = e)
        }
    }

    override fun deleteArtifact(artifact: Artifact): DevoraResult<Unit> {
        val file = File(artifact.filePath)
        if (!file.exists()) return DevoraResult.Success(Unit)
        return try {
            if (!file.delete()) throw java.io.IOException("Delete returned false for ${file.absolutePath}")
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to delete artifact: ${file.absolutePath}", cause = e)
        }
    }

    private fun scanDirectoryRecursive(dir: File, source: ArtifactSource): List<Artifact> {
        val result = mutableListOf<Artifact>()
        dir.listFiles()?.forEach { entry ->
            if (entry.isDirectory) {
                result.addAll(scanDirectoryRecursive(entry, source))
            } else if (entry.extension == "apk" || entry.extension == "aab") {
                result.add(
                    Artifact(
                        id = entry.absolutePath,
                        filePath = entry.absolutePath,
                        fileName = entry.name,
                        type = if (entry.extension == "apk") ArtifactType.APK else ArtifactType.AAB,
                        sizeBytes = entry.length(),
                        sha256 = computeSha256(entry),
                        createdAtEpochMillis = entry.lastModified(),
                        source = source
                    )
                )
            }
        }
        return result
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}