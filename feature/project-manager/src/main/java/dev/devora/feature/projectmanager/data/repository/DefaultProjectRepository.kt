package dev.devora.feature.projectmanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.projectmanager.data.local.ProjectRegistryEntry
import dev.devora.feature.projectmanager.data.local.ProjectRegistryStore
import dev.devora.feature.projectmanager.domain.model.DevoraProjectLayout
import dev.devora.feature.projectmanager.domain.model.Project
import dev.devora.feature.projectmanager.domain.repository.ProjectRepository
import java.io.File
import java.util.UUID

class DefaultProjectRepository(
    private val registryStore: ProjectRegistryStore
) : ProjectRepository {

    override suspend fun listProjects(): DevoraResult<List<Project>> {
        val registryResult = registryStore.read()
        return when (registryResult) {
            is DevoraResult.Failure -> registryResult
            is DevoraResult.Success -> {
                val projects = registryResult.data.entries.mapNotNull { entry ->
                    toProjectOrNull(entry)
                }
                DevoraResult.Success(projects)
            }
        }
    }

    override suspend fun importExistingProject(rootPath: String): DevoraResult<Project> {
        val rootDir = File(rootPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return DevoraResult.Failure(
                message = "Path does not exist or is not a directory: $rootPath"
            )
        }

        val hasSettingsKts = File(DevoraProjectLayout.settingsGradleKts(rootPath)).exists()
        val hasSettingsGroovy = File(DevoraProjectLayout.settingsGradleGroovy(rootPath)).exists()
        if (!hasSettingsKts && !hasSettingsGroovy) {
            return DevoraResult.Failure(
                message = "No settings.gradle or settings.gradle.kts found in $rootPath. " +
                    "Devora only imports existing Gradle projects; it does not scaffold new ones."
            )
        }

        val registryResult = registryStore.read()
        if (registryResult is DevoraResult.Failure) return registryResult
        val registry = (registryResult as DevoraResult.Success).data

        val existing = registry.entries.find { it.rootPath == rootPath }
        if (existing != null) {
            return toProjectOrNull(existing)?.let { DevoraResult.Success(it) }
                ?: DevoraResult.Failure(message = "Project already registered but could not be loaded: $rootPath")
        }

        val newEntry = ProjectRegistryEntry(
            id = UUID.randomUUID().toString(),
            rootPath = rootPath,
            lastOpenedAtEpochMillis = null
        )
        val writeResult = registryStore.write(registry.copy(entries = registry.entries + newEntry))
        if (writeResult is DevoraResult.Failure) {
            return DevoraResult.Failure(message = writeResult.message, cause = writeResult.cause)
        }

        return toProjectOrNull(newEntry)?.let { DevoraResult.Success(it) }
            ?: DevoraResult.Failure(message = "Failed to load project metadata after import: $rootPath")
    }

    override suspend fun initializeDevoraMetadata(rootPath: String): DevoraResult<Unit> {
        val rootDir = File(rootPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return DevoraResult.Failure(
                message = "Path does not exist or is not a directory: $rootPath"
            )
        }

        return try {
            val workflowsDir = File(DevoraProjectLayout.workflowsDir(rootPath))
            val secretsDir = File(DevoraProjectLayout.secretsDir(rootPath))
            val environmentsDir = File(DevoraProjectLayout.environmentsDir(rootPath))

            if (!workflowsDir.mkdirs() && !workflowsDir.exists()) {
                throw java.io.IOException("Could not create ${workflowsDir.absolutePath}")
            }
            if (!secretsDir.mkdirs() && !secretsDir.exists()) {
                throw java.io.IOException("Could not create ${secretsDir.absolutePath}")
            }
            if (!environmentsDir.mkdirs() && !environmentsDir.exists()) {
                throw java.io.IOException("Could not create ${environmentsDir.absolutePath}")
            }

            val gitignoreFile = File(secretsDir.parentFile, ".gitignore")
            if (!gitignoreFile.exists()) {
                gitignoreFile.writeText("secrets/\nenvironments/\n")
            }

            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(
                message = "Failed to initialize .devora metadata in $rootPath",
                cause = e
            )
        }
    }

    override suspend fun removeFromRegistry(projectId: String): DevoraResult<Unit> {
        val registryResult = registryStore.read()
        if (registryResult is DevoraResult.Failure) return registryResult
        val registry = (registryResult as DevoraResult.Success).data

        val filtered = registry.entries.filterNot { it.id == projectId }
        if (filtered.size == registry.entries.size) {
            return DevoraResult.Failure(message = "Project not found in registry: $projectId")
        }

        val writeResult = registryStore.write(registry.copy(entries = filtered))
        return when (writeResult) {
            is DevoraResult.Failure -> DevoraResult.Failure(message = writeResult.message, cause = writeResult.cause)
            is DevoraResult.Success -> DevoraResult.Success(Unit)
        }
    }

    override suspend fun markOpened(projectId: String): DevoraResult<Unit> {
        val registryResult = registryStore.read()
        if (registryResult is DevoraResult.Failure) return registryResult
        val registry = (registryResult as DevoraResult.Success).data

        var found = false
        val updated = registry.entries.map { entry ->
            if (entry.id == projectId) {
                found = true
                entry.copy(lastOpenedAtEpochMillis = System.currentTimeMillis())
            } else {
                entry
            }
        }

        if (!found) {
            return DevoraResult.Failure(message = "Project not found in registry: $projectId")
        }

        val writeResult = registryStore.write(registry.copy(entries = updated))
        return when (writeResult) {
            is DevoraResult.Failure -> DevoraResult.Failure(message = writeResult.message, cause = writeResult.cause)
            is DevoraResult.Success -> DevoraResult.Success(Unit)
        }
    }

    private fun toProjectOrNull(entry: ProjectRegistryEntry): Project? {
        val rootDir = File(entry.rootPath)
        if (!rootDir.exists() || !rootDir.isDirectory) return null

        val hasDevoraMetadata = File(DevoraProjectLayout.metadataDir(entry.rootPath)).isDirectory
        val hasGradleWrapper = File(DevoraProjectLayout.gradleWrapperProperties(entry.rootPath)).exists()

        return Project(
            id = entry.id,
            name = rootDir.name,
            rootPath = entry.rootPath,
            hasDevoraMetadata = hasDevoraMetadata,
            hasGradleWrapper = hasGradleWrapper,
            lastOpenedAtEpochMillis = entry.lastOpenedAtEpochMillis
        )
    }
}