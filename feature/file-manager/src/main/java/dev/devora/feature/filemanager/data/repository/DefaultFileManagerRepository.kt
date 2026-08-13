package dev.devora.feature.filemanager.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.filemanager.domain.model.DirectoryListing
import dev.devora.feature.filemanager.domain.model.FileNode
import dev.devora.feature.filemanager.domain.model.FileNodeType
import dev.devora.feature.filemanager.domain.model.FilePermissions
import dev.devora.feature.filemanager.domain.repository.FileManagerRepository
import java.io.File
import java.io.IOException
import java.nio.file.Files

class DefaultFileManagerRepository : FileManagerRepository {

    override suspend fun listDirectory(path: String): DevoraResult<DirectoryListing> {
        val dir = File(path)
        if (!dir.exists()) {
            return DevoraResult.Failure(message = "Directory does not exist: $path")
        }
        if (!dir.isDirectory) {
            return DevoraResult.Failure(message = "Not a directory: $path")
        }
        if (!dir.canRead()) {
            return DevoraResult.Failure(message = "Permission denied reading directory: $path")
        }

        return try {
            val children = dir.listFiles() ?: emptyArray()
            val entries = children
                .map { toFileNode(it) }
                .sortedWith(
                    compareByDescending<FileNode> { it.type == FileNodeType.DIRECTORY }
                        .thenBy { it.name.lowercase() }
                )
            DevoraResult.Success(
                DirectoryListing(
                    currentPath = dir.absolutePath,
                    parentPath = dir.parentFile?.absolutePath,
                    entries = entries
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to list directory: $path", cause = e)
        }
    }

    override suspend fun readTextFile(path: String): DevoraResult<String> {
        val file = File(path)
        if (!file.exists() || !file.isFile) {
            return DevoraResult.Failure(message = "File does not exist: $path")
        }
        if (!file.canRead()) {
            return DevoraResult.Failure(message = "Permission denied reading file: $path")
        }
        return try {
            DevoraResult.Success(file.readText())
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to read file: $path", cause = e)
        }
    }

    override suspend fun createFile(parentPath: String, name: String): DevoraResult<FileNode> {
        val parent = File(parentPath)
        if (!parent.exists() || !parent.isDirectory) {
            return DevoraResult.Failure(message = "Parent directory does not exist: $parentPath")
        }
        val target = File(parent, name)
        if (target.exists()) {
            return DevoraResult.Failure(message = "File already exists: ${target.absolutePath}")
        }
        return try {
            if (!target.createNewFile()) {
                throw IOException("createNewFile returned false for ${target.absolutePath}")
            }
            DevoraResult.Success(toFileNode(target))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to create file: ${target.absolutePath}", cause = e)
        }
    }

    override suspend fun createDirectory(parentPath: String, name: String): DevoraResult<FileNode> {
        val parent = File(parentPath)
        if (!parent.exists() || !parent.isDirectory) {
            return DevoraResult.Failure(message = "Parent directory does not exist: $parentPath")
        }
        val target = File(parent, name)
        if (target.exists()) {
            return DevoraResult.Failure(message = "Directory already exists: ${target.absolutePath}")
        }
        return try {
            if (!target.mkdir()) {
                throw IOException("mkdir returned false for ${target.absolutePath}")
            }
            DevoraResult.Success(toFileNode(target))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to create directory: ${target.absolutePath}", cause = e)
        }
    }

    override suspend fun rename(path: String, newName: String): DevoraResult<FileNode> {
        val source = File(path)
        if (!source.exists()) {
            return DevoraResult.Failure(message = "Path does not exist: $path")
        }
        val destination = File(source.parentFile, newName)
        if (destination.exists()) {
            return DevoraResult.Failure(message = "Target already exists: ${destination.absolutePath}")
        }
        return try {
            if (!source.renameTo(destination)) {
                throw IOException("renameTo returned false for ${source.absolutePath} -> ${destination.absolutePath}")
            }
            DevoraResult.Success(toFileNode(destination))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to rename ${source.absolutePath}", cause = e)
        }
    }

    override suspend fun move(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode> {
        val source = File(sourcePath)
        val destinationDir = File(destinationDirectoryPath)
        if (!source.exists()) {
            return DevoraResult.Failure(message = "Source does not exist: $sourcePath")
        }
        if (!destinationDir.exists() || !destinationDir.isDirectory) {
            return DevoraResult.Failure(message = "Destination directory does not exist: $destinationDirectoryPath")
        }
        val destination = File(destinationDir, source.name)
        if (destination.exists()) {
            return DevoraResult.Failure(message = "Target already exists: ${destination.absolutePath}")
        }
        return try {
            if (!source.renameTo(destination)) {
                throw IOException("Move failed (cross-filesystem move is not supported): ${source.absolutePath} -> ${destination.absolutePath}")
            }
            DevoraResult.Success(toFileNode(destination))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to move ${source.absolutePath}", cause = e)
        }
    }

    override suspend fun copy(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode> {
        val source = File(sourcePath)
        val destinationDir = File(destinationDirectoryPath)
        if (!source.exists()) {
            return DevoraResult.Failure(message = "Source does not exist: $sourcePath")
        }
        if (!destinationDir.exists() || !destinationDir.isDirectory) {
            return DevoraResult.Failure(message = "Destination directory does not exist: $destinationDirectoryPath")
        }
        val destination = File(destinationDir, source.name)
        if (destination.exists()) {
            return DevoraResult.Failure(message = "Target already exists: ${destination.absolutePath}")
        }
        return try {
            if (source.isDirectory) {
                source.copyRecursively(destination, overwrite = false)
            } else {
                source.copyTo(destination, overwrite = false)
            }
            DevoraResult.Success(toFileNode(destination))
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to copy ${source.absolutePath}", cause = e)
        }
    }

    override suspend fun delete(path: String): DevoraResult<Unit> {
        val target = File(path)
        if (!target.exists()) {
            return DevoraResult.Failure(message = "Path does not exist: $path")
        }
        return try {
            val deleted = if (target.isDirectory) target.deleteRecursively() else target.delete()
            if (!deleted) {
                throw IOException("Delete returned false for ${target.absolutePath}")
            }
            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to delete ${target.absolutePath}", cause = e)
        }
    }

    override suspend fun getPermissions(path: String): DevoraResult<FileNode> {
        val target = File(path)
        if (!target.exists()) {
            return DevoraResult.Failure(message = "Path does not exist: $path")
        }
        return DevoraResult.Success(toFileNode(target))
    }

    private fun toFileNode(file: File): FileNode {
        val type = when {
            Files.isSymbolicLink(file.toPath()) -> FileNodeType.SYMLINK
            file.isDirectory -> FileNodeType.DIRECTORY
            else -> FileNodeType.FILE
        }
        return FileNode(
            name = file.name,
            absolutePath = file.absolutePath,
            type = type,
            isHidden = file.isHidden || file.name.startsWith("."),
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModifiedEpochMillis = file.lastModified(),
            permissions = FilePermissions(
                readable = file.canRead(),
                writable = file.canWrite(),
                executable = file.canExecute()
            )
        )
    }
}