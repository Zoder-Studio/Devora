package dev.devora.feature.git.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.git.domain.model.GitBranch
import dev.devora.feature.git.domain.model.GitCommandOutput
import dev.devora.feature.git.domain.model.GitLogEntry
import dev.devora.feature.git.domain.model.GitStatusResult

interface GitRepository {
    fun isGitInstalled(): Boolean

    suspend fun setupGit(onOutputLine: (String) -> Unit): DevoraResult<Unit>

    fun isGitRepository(projectRootPath: String): Boolean

    suspend fun init(projectRootPath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit>

    suspend fun status(projectRootPath: String): DevoraResult<GitStatusResult>

    suspend fun add(projectRootPath: String, paths: List<String>): DevoraResult<Unit>

    suspend fun unstage(projectRootPath: String, paths: List<String>): DevoraResult<Unit>

    /** message is passed exactly as the developer typed it — Devora never generates or edits commit messages itself. */
    suspend fun commit(projectRootPath: String, message: String): DevoraResult<Unit>

    suspend fun log(projectRootPath: String, maxCount: Int = 50): DevoraResult<List<GitLogEntry>>

    suspend fun listBranches(projectRootPath: String): DevoraResult<List<GitBranch>>

    suspend fun checkout(projectRootPath: String, branchName: String, createNew: Boolean): DevoraResult<Unit>

    suspend fun push(projectRootPath: String, remote: String, branch: String, onOutputLine: (String) -> Unit): DevoraResult<GitCommandOutput>

    suspend fun pull(projectRootPath: String, remote: String, branch: String, onOutputLine: (String) -> Unit): DevoraResult<GitCommandOutput>

    suspend fun diff(projectRootPath: String, path: String?, staged: Boolean): DevoraResult<String>

    suspend fun setRemote(projectRootPath: String, name: String, url: String): DevoraResult<Unit>
}