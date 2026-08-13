package dev.devora.feature.artifactmanager.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.model.Artifact
import dev.devora.feature.artifactmanager.domain.repository.ArtifactRepository

class ListArtifactsUseCase(private val repository: ArtifactRepository) {
    operator fun invoke(projectRootPath: String): DevoraResult<List<Artifact>> {
        val projectResult = repository.scanProjectArtifacts(projectRootPath)
        if (projectResult is DevoraResult.Failure) return projectResult
        val stagedResult = repository.scanStagedArtifacts()
        if (stagedResult is DevoraResult.Failure) return stagedResult

        val combined = (projectResult as DevoraResult.Success).data +
            (stagedResult as DevoraResult.Success).data
        return DevoraResult.Success(combined.sortedByDescending { it.createdAtEpochMillis })
    }
}

class DeleteArtifactUseCase(private val repository: ArtifactRepository) {
    operator fun invoke(artifact: Artifact): DevoraResult<Unit> = repository.deleteArtifact(artifact)
}