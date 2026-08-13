package dev.devora.feature.filemanager.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.filemanager.domain.model.DirectoryListing
import dev.devora.feature.filemanager.domain.model.FileNode
import dev.devora.feature.filemanager.domain.repository.FileManagerRepository

class ListDirectoryUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(path: String): DevoraResult<DirectoryListing> =
        repository.listDirectory(path)
}

class CreateFileUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(parentPath: String, name: String): DevoraResult<FileNode> =
        repository.createFile(parentPath, name)
}

class CreateDirectoryUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(parentPath: String, name: String): DevoraResult<FileNode> =
        repository.createDirectory(parentPath, name)
}

class RenameFileUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(path: String, newName: String): DevoraResult<FileNode> =
        repository.rename(path, newName)
}

class MoveFileUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode> =
        repository.move(sourcePath, destinationDirectoryPath)
}

class CopyFileUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(sourcePath: String, destinationDirectoryPath: String): DevoraResult<FileNode> =
        repository.copy(sourcePath, destinationDirectoryPath)
}

class DeleteFileUseCase(private val repository: FileManagerRepository) {
    suspend operator fun invoke(path: String): DevoraResult<Unit> = repository.delete(path)
}