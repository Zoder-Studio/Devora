package dev.devora.feature.filemanager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.filemanager.data.repository.DefaultFileManagerRepository
import dev.devora.feature.filemanager.domain.action.OpenFileInNanoAction
import dev.devora.feature.filemanager.domain.action.OpenTerminalAtPathAction
import dev.devora.feature.filemanager.domain.action.UnavailableOpenNanoAction
import dev.devora.feature.filemanager.domain.action.UnavailableOpenTerminalAction
import dev.devora.feature.filemanager.domain.repository.FileManagerRepository
import dev.devora.feature.filemanager.domain.usecase.CopyFileUseCase
import dev.devora.feature.filemanager.domain.usecase.CreateDirectoryUseCase
import dev.devora.feature.filemanager.domain.usecase.CreateFileUseCase
import dev.devora.feature.filemanager.domain.usecase.DeleteFileUseCase
import dev.devora.feature.filemanager.domain.usecase.ListDirectoryUseCase
import dev.devora.feature.filemanager.domain.usecase.MoveFileUseCase
import dev.devora.feature.filemanager.domain.usecase.RenameFileUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileManagerModule {

    @Provides
    @Singleton
    fun provideFileManagerRepository(): FileManagerRepository = DefaultFileManagerRepository()

    @Provides
    fun provideListDirectoryUseCase(repository: FileManagerRepository) =
        ListDirectoryUseCase(repository)

    @Provides
    fun provideCreateFileUseCase(repository: FileManagerRepository) =
        CreateFileUseCase(repository)

    @Provides
    fun provideCreateDirectoryUseCase(repository: FileManagerRepository) =
        CreateDirectoryUseCase(repository)

    @Provides
    fun provideRenameFileUseCase(repository: FileManagerRepository) =
        RenameFileUseCase(repository)

    @Provides
    fun provideMoveFileUseCase(repository: FileManagerRepository) =
        MoveFileUseCase(repository)

    @Provides
    fun provideCopyFileUseCase(repository: FileManagerRepository) =
        CopyFileUseCase(repository)

    @Provides
    fun provideDeleteFileUseCase(repository: FileManagerRepository) =
        DeleteFileUseCase(repository)
}