package dev.devora.feature.artifactmanager.domain.model

enum class ArtifactType { APK, AAB, OTHER }

data class Artifact(
    val id: String,
    val filePath: String,
    val fileName: String,
    val type: ArtifactType,
    val sizeBytes: Long,
    val sha256: String,
    val createdAtEpochMillis: Long,
    val source: ArtifactSource
)

enum class ArtifactSource { BUILD_OUTPUT, WORKFLOW_STAGED }