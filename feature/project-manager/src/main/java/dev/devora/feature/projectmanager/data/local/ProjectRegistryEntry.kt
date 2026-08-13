package dev.devora.feature.projectmanager.data.local

import kotlinx.serialization.Serializable

@Serializable
data class ProjectRegistryEntry(
    val id: String,
    val rootPath: String,
    val lastOpenedAtEpochMillis: Long? = null
)

@Serializable
data class ProjectRegistry(
    val entries: List<ProjectRegistryEntry> = emptyList()
)