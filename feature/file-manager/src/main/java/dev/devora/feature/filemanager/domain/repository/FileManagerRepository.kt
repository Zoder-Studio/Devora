package dev.devora.feature.filemanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.filemanager.domain.model.DirectoryListing
import dev.devora.feature.filemanager.domain.model.FileNode

interface FileManagerRepository {
    suspend fun listDirectory(path: String): DevoraResult<DirectoryListing>
    suspend fun readTextFile(path: String): DevoraResult<String>
    suspend fun createFile(parentPath: String, name: String): DevoraResult<FileNode>
    suspend fun createDirectory(parentPath: String, name: String): DevoraResult<FileNode>
    suspend fun rename(path: String, newName: String): DevoraResult<FileNode>
    suspend fun move(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode>
    suspend fun copy(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode>
    suspend fun delete(path: String): DevoraResult<Unit>
    suspend fun getPermissions(path: String): DevoraResult<FileNode>
}