package dev.devora.feature.git.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.git.domain.model.*
import dev.devora.feature.git.domain.repository.GitRepository
import dev.devora.feature.terminal.domain.execution.CommandExecutionEngineProvider
import dev.devora.feature.terminal.domain.execution.EnginePaths
import java.io.File

class DefaultGitRepository(
    private val engineProvider: CommandExecutionEngineProvider,
    private val enginePaths: EnginePaths
) : GitRepository {

    override fun isGitInstalled(): Boolean =
        File(enginePaths.currentPrefixPath(), "bin/git").exists()

    override suspend fun setupGit(onOutputLine: (String) -> Unit): DevoraResult<Unit> {
        if (isGitInstalled()) return DevoraResult.Success(Unit)
        onOutputLine("Installing git...")
        return engineProvider.current().run(
            workingDirectory = enginePaths.currentPrefixPath(),
            script = "apt update && apt install -y git",
            timeoutMillis = 300_000L,
            onOutputLine = onOutputLine
        )
    }

    override fun isGitRepository(projectRootPath: String): Boolean =
        File(projectRootPath, ".git").exists()

    override suspend fun init(projectRootPath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        engineProvider.current().run(projectRootPath, "git init", onOutputLine = onOutputLine)

    override suspend fun status(projectRootPath: String): DevoraResult<GitStatusResult> {
        val lines = mutableListOf<String>()
        // "--porcelain=v2 --branch" gives a stable, script-friendly format —
        // this is git's own documented machine-readable output, not something Devora invents.
        val result = engineProvider.current().run(
            projectRootPath,
            "git status --porcelain=v2 --branch",
            onOutputLine = { line -> lines.add(line) }
        )
        if (result is DevoraResult.Failure) {
            return DevoraResult.Failure(message = result.message, cause = result.cause)
        }
        return DevoraResult.Success(parsePorcelainV2(lines))
    }

    override suspend fun setRemote(projectRootPath: String, name: String, url: String): DevoraResult<Unit> {
        // "remote remove" may fail harmlessly if the remote doesn't exist yet — ignore that specific failure.
        engineProvider.current().run(projectRootPath, "git remote remove '$name' 2>/dev/null; true", onOutputLine = {})
        return engineProvider.current().run(projectRootPath, "git remote add '$name' '$url'", onOutputLine = {})
    }

    private fun parsePorcelainV2(lines: List<String>): GitStatusResult {
        var branch: String? = null
        var ahead = 0
        var behind = 0
        val changes = mutableListOf<GitFileChange>()

        for (line in lines) {
            when {
                line.startsWith("# branch.head ") -> branch = line.removePrefix("# branch.head ").trim()
                line.startsWith("# branch.ab ") -> {
                    val parts = line.removePrefix("# branch.ab ").trim().split(" ")
                    ahead = parts.getOrNull(0)?.removePrefix("+")?.toIntOrNull() ?: 0
                    behind = parts.getOrNull(1)?.removePrefix("-")?.toIntOrNull() ?: 0
                }
                line.startsWith("1 ") || line.startsWith("2 ") -> {
                    // ordinary changed / renamed entry: "1 XY sub mH mI mW hH hI path"
                    val fields = line.split(" ", limit = 9)
                    val xy = fields.getOrNull(1).orEmpty()
                    val path = fields.lastOrNull().orEmpty()
                    val stagedChar = xy.getOrNull(0) ?: '.'
                    val unstagedChar = xy.getOrNull(1) ?: '.'
                    val staged = stagedChar != '.'
                    val status = mapStatusChar(if (staged) stagedChar else unstagedChar)
                    changes.add(GitFileChange(path, status, staged))
                }
                line.startsWith("? ") -> {
                    val path = line.removePrefix("? ").trim()
                    changes.add(GitFileChange(path, GitFileStatus.UNTRACKED, staged = false))
                }
            }
        }

        return GitStatusResult(
            branch = branch,
            ahead = ahead,
            behind = behind,
            changes = changes,
            isClean = changes.isEmpty()
        )
    }

    private fun mapStatusChar(c: Char): GitFileStatus = when (c) {
        'M' -> GitFileStatus.MODIFIED
        'A' -> GitFileStatus.ADDED
        'D' -> GitFileStatus.DELETED
        'R' -> GitFileStatus.RENAMED
        else -> GitFileStatus.UNKNOWN
    }

    override suspend fun add(projectRootPath: String, paths: List<String>): DevoraResult<Unit> {
        val pathArgs = if (paths.isEmpty()) "." else paths.joinToString(" ") { "'$it'" }
        return engineProvider.current().run(projectRootPath, "git add $pathArgs", onOutputLine = {})
    }

    override suspend fun unstage(projectRootPath: String, paths: List<String>): DevoraResult<Unit> {
        val pathArgs = if (paths.isEmpty()) "." else paths.joinToString(" ") { "'$it'" }
        return engineProvider.current().run(projectRootPath, "git restore --staged $pathArgs", onOutputLine = {})
    }

    override suspend fun commit(projectRootPath: String, message: String): DevoraResult<Unit> {
        // heredoc avoids shell-escaping issues with quotes/newlines in the developer's message
        val script = "git commit -F - <<'DEVORA_COMMIT_MSG_EOF'\n$message\nDEVORA_COMMIT_MSG_EOF"
        return engineProvider.current().run(projectRootPath, script, onOutputLine = {})
    }

    override suspend fun log(projectRootPath: String, maxCount: Int): DevoraResult<List<GitLogEntry>> {
        val lines = mutableListOf<String>()
        val format = "%H%x1f%h%x1f%an%x1f%at%x1f%s"
        val result = engineProvider.current().run(
            projectRootPath,
            "git log -n $maxCount --pretty=format:'$format'",
            onOutputLine = { line -> lines.add(line) }
        )
        if (result is DevoraResult.Failure) return DevoraResult.Failure(message = result.message, cause = result.cause)

        val entries = lines.mapNotNull { line ->
            val fields = line.split("\u001f")
            if (fields.size < 5) return@mapNotNull null
            GitLogEntry(
                hash = fields[0],
                shortHash = fields[1],
                authorName = fields[2],
                authorDateEpochSeconds = fields[3].toLongOrNull() ?: 0L,
                subject = fields[4]
            )
        }
        return DevoraResult.Success(entries)
    }

    override suspend fun listBranches(projectRootPath: String): DevoraResult<List<GitBranch>> {
        val lines = mutableListOf<String>()
        val result = engineProvider.current().run(
            projectRootPath,
            "git branch --all --format='%(HEAD)|%(refname:short)'",
            onOutputLine = { line -> lines.add(line) }
        )
        if (result is DevoraResult.Failure) return DevoraResult.Failure(message = result.message, cause = result.cause)

        val branches = lines.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 2) return@mapNotNull null
            val isCurrent = parts[0].trim() == "*"
            val name = parts[1].trim()
            GitBranch(name = name, isCurrent = isCurrent, isRemote = name.startsWith("remotes/"))
        }
        return DevoraResult.Success(branches)
    }

    override suspend fun checkout(projectRootPath: String, branchName: String, createNew: Boolean): DevoraResult<Unit> {
        val flag = if (createNew) "-b" else ""
        return engineProvider.current().run(projectRootPath, "git checkout $flag '$branchName'", onOutputLine = {})
    }

    override suspend fun push(
        projectRootPath: String,
        remote: String,
        branch: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<GitCommandOutput> {
        val output = StringBuilder()
        val result = engineProvider.current().run(
            projectRootPath,
            "git push '$remote' '$branch'",
            timeoutMillis = 300_000L,
            onOutputLine = { line -> output.appendLine(line); onOutputLine(line) }
        )
        return when (result) {
            is DevoraResult.Success -> DevoraResult.Success(GitCommandOutput(0, output.toString()))
            is DevoraResult.Failure -> DevoraResult.Failure(message = result.message, cause = result.cause)
        }
    }

    override suspend fun pull(
        projectRootPath: String,
        remote: String,
        branch: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<GitCommandOutput> {
        val output = StringBuilder()
        val result = engineProvider.current().run(
            projectRootPath,
            "git pull '$remote' '$branch'",
            timeoutMillis = 300_000L,
            onOutputLine = { line -> output.appendLine(line); onOutputLine(line) }
        )
        return when (result) {
            is DevoraResult.Success -> DevoraResult.Success(GitCommandOutput(0, output.toString()))
            is DevoraResult.Failure -> DevoraResult.Failure(message = result.message, cause = result.cause)
        }
    }

    override suspend fun diff(projectRootPath: String, path: String?, staged: Boolean): DevoraResult<String> {
        val output = StringBuilder()
        val stagedFlag = if (staged) "--staged" else ""
        val pathArg = path?.let { "-- '$it'" } ?: ""
        val result = engineProvider.current().run(
            projectRootPath,
            "git diff $stagedFlag $pathArg",
            onOutputLine = { line -> output.appendLine(line) }
        )
        return when (result) {
            is DevoraResult.Success -> DevoraResult.Success(output.toString())
            is DevoraResult.Failure -> DevoraResult.Failure(message = result.message, cause = result.cause)
        }
    }
}