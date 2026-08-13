package dev.devora.feature.git.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.git.domain.model.*
import dev.devora.feature.git.domain.repository.GitRepository

class GitStatusUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String): DevoraResult<GitStatusResult> = repository.status(projectRootPath)
}

class GitAddUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, paths: List<String>): DevoraResult<Unit> = repository.add(projectRootPath, paths)
}

class GitUnstageUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, paths: List<String>): DevoraResult<Unit> = repository.unstage(projectRootPath, paths)
}

class GitCommitUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, message: String): DevoraResult<Unit> = repository.commit(projectRootPath, message)
}

class GitLogUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, maxCount: Int = 50): DevoraResult<List<GitLogEntry>> = repository.log(projectRootPath, maxCount)
}

class GitListBranchesUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String): DevoraResult<List<GitBranch>> = repository.listBranches(projectRootPath)
}

class GitCheckoutUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, branchName: String, createNew: Boolean): DevoraResult<Unit> =
        repository.checkout(projectRootPath, branchName, createNew)
}

class GitPushUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, remote: String, branch: String, onOutputLine: (String) -> Unit): DevoraResult<GitCommandOutput> =
        repository.push(projectRootPath, remote, branch, onOutputLine)
}

class GitPullUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, remote: String, branch: String, onOutputLine: (String) -> Unit): DevoraResult<GitCommandOutput> =
        repository.pull(projectRootPath, remote, branch, onOutputLine)
}

class GitDiffUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(projectRootPath: String, path: String?, staged: Boolean): DevoraResult<String> =
        repository.diff(projectRootPath, path, staged)
}