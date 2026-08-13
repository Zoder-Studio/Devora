package dev.devora.feature.artifactmanager.data.install

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.install.ArtifactInstaller
import dev.devora.feature.artifactmanager.domain.model.Artifact
import dev.devora.feature.artifactmanager.domain.model.ArtifactType
import java.io.File

class DefaultArtifactInstaller(
    private val context: Context
) : ArtifactInstaller {

    override fun install(artifact: Artifact): DevoraResult<Unit> {
        if (artifact.type != ArtifactType.APK) {
            return DevoraResult.Failure(
                message = "Only APK files can be installed directly. AAB files must be converted to " +
                    "APKs (e.g. via bundletool) before installing — Devora does not fake an install path " +
                    "that Android itself does not support."
            )
        }

        val file = File(artifact.filePath)
        if (!file.exists()) {
            return DevoraResult.Failure(message = "Artifact file no longer exists: ${artifact.filePath}")
        }

        return try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to launch package installer for ${artifact.fileName}", cause = e)
        }
    }
}