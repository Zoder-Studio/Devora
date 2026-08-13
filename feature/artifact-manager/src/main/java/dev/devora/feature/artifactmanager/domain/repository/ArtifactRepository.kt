package dev.devora.feature.artifactmanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.model.Artifact

interface ArtifactRepository {
    /** Scans real build output directories under the project root — no metadata is invented, everything reflects an actual file on disk. */
    fun scanProjectArtifacts(projectRootPath: String): DevoraResult<List<Artifact>>

    /** Scans Devora's own artifact staging area (populated by actions/upload-artifact, Stage 9). */
    fun scanStagedArtifacts(): DevoraResult<List<Artifact>>

    fun deleteArtifact(artifact: Artifact): DevoraResult<Unit>
}