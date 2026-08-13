package dev.devora.feature.git.domain.model

enum class GitFileStatus { MODIFIED, ADDED, DELETED, RENAMED, UNTRACKED, UNKNOWN }

data class GitFileChange(
    val path: String,
    val status: GitFileStatus,
    val staged: Boolean
)

data class GitStatusResult(
    val branch: String?,
    val ahead: Int,
    val behind: Int,
    val changes: List<GitFileChange>,
    val isClean: Boolean
)

data class GitLogEntry(
    val hash: String,
    val shortHash: String,
    val authorName: String,
    val authorDateEpochSeconds: Long,
    val subject: String
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean
)

data class GitCommandOutput(
    val exitCode: Int,
    val output: String
)