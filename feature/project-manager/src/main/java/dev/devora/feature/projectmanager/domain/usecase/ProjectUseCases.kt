package dev.devora.feature.projectmanager.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.projectmanager.domain.model.Project
import dev.devora.feature.projectmanager.domain.repository.ProjectRepository

class ListProjectsUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(): DevoraResult<List<Project>> = repository.listProjects()
}

/**
 * Imports an existing project and, only if the caller explicitly
 * requests it, initializes ".devora" metadata. Metadata initialization
 * is opt-in per spec section 3 — Devora never silently modifies a
 * project directory the developer did not ask it to touch.
 */
class ImportProjectUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(
        rootPath: String,
        initializeDevoraMetadata: Boolean
    ): DevoraResult<Project> {
        val importResult = repository.importExistingProject(rootPath)
        if (importResult is DevoraResult.Failure) return importResult

        if (initializeDevoraMetadata) {
            val metadataResult = repository.initializeDevoraMetadata(rootPath)
            if (metadataResult is DevoraResult.Failure) {
                return DevoraResult.Failure(
                    message = "Project imported but .devora metadata setup failed: ${metadataResult.message}",
                    cause = metadataResult.cause
                )
            }
        }

        return importResult
    }
}

class RemoveProjectUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(projectId: String): DevoraResult<Unit> =
        repository.removeFromRegistry(projectId)
}

class OpenProjectUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(projectId: String): DevoraResult<Unit> =
        repository.markOpened(projectId)
}