package dev.devora.feature.projectmanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.projectmanager.domain.model.Project

interface ProjectRepository {
    suspend fun listProjects(): DevoraResult<List<Project>>

    /**
     * Imports an existing directory as a Devora project. Does not
     * create any Gradle files — the directory must already contain
     * a real Android/Gradle project, or this fails with a clear error.
     */
    suspend fun importExistingProject(rootPath: String): DevoraResult<Project>

    /**
     * Creates the ".devora" metadata directory inside an existing
     * project directory. Never creates build.gradle.kts, settings.gradle.kts,
     * or any Gradle file — those remain fully the developer's responsibility.
     */
    suspend fun initializeDevoraMetadata(rootPath: String): DevoraResult<Unit>

    suspend fun removeFromRegistry(projectId: String): DevoraResult<Unit>

    suspend fun markOpened(projectId: String): DevoraResult<Unit>
}