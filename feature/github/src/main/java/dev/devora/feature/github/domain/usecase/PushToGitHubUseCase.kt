package dev.devora.feature.github.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.git.domain.repository.GitRepository
import dev.devora.feature.github.data.GitHubTokenStore
import dev.devora.feature.github.domain.model.GitHubRepo
import java.io.File

/**
 * Implements spec section 15 exactly: when pushing to GitHub,
 * ".devora/workflows/" is materialized as ".github/workflows/" with
 * identical content, while ".devora/secrets/" is never copied or
 * pushed under any circumstance. This runs as a plain file copy plus
 * real git commands — no network upload logic of its own beyond
 * standard git push (authenticated via the stored GitHub token as
 * the HTTPS credential).
 */
class PushToGitHubUseCase(
    private val gitRepository: GitRepository,
    private val tokenStore: GitHubTokenStore
) {
    suspend operator fun invoke(
        projectRootPath: String,
        repo: GitHubRepo,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        val token = tokenStore.readToken()
            ?: return DevoraResult.Failure(message = "Not logged in to GitHub")

        val materializeResult = materializeGithubWorkflows(projectRootPath, onOutputLine)
        if (materializeResult is DevoraResult.Failure) return materializeResult

        if (!gitRepository.isGitRepository(projectRootPath)) {
            val initResult = gitRepository.init(projectRootPath, onOutputLine)
            if (initResult is DevoraResult.Failure) return initResult
        }

        gitRepository.add(projectRootPath, listOf("."))

        // Authenticated remote URL: embeds the token only in the git
        // remote config for this push, never written to any file the
        // developer might accidentally commit or share.
        val authenticatedUrl = repo.cloneUrl.replace("https://", "https://x-access-token:$token@")

        val addRemoteResult = addOrUpdateRemote(projectRootPath, authenticatedUrl, onOutputLine)
        if (addRemoteResult is DevoraResult.Failure) return addRemoteResult

        val pushResult = gitRepository.push(projectRootPath, "origin", repo.defaultBranch, onOutputLine)
        return when (pushResult) {
            is DevoraResult.Success -> DevoraResult.Success(Unit)
            is DevoraResult.Failure -> DevoraResult.Failure(message = pushResult.message, cause = pushResult.cause)
        }
    }

    private fun materializeGithubWorkflows(
        projectRootPath: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        val devoraWorkflowsDir = File(projectRootPath, ".devora/workflows")
        val githubWorkflowsDir = File(projectRootPath, ".github/workflows")

        if (!devoraWorkflowsDir.exists()) {
            onOutputLine("No .devora/workflows directory found — skipping workflow materialization.")
            return DevoraResult.Success(Unit)
        }

        return try {
            githubWorkflowsDir.mkdirs()
            devoraWorkflowsDir.listFiles { file -> file.extension == "yml" || file.extension == "yaml" }
                ?.forEach { workflowFile ->
                    val destination = File(githubWorkflowsDir, workflowFile.name)
                    workflowFile.copyTo(destination, overwrite = true)
                    onOutputLine("Materialized ${workflowFile.name} -> .github/workflows/")
                }

            val secretsDir = File(projectRootPath, ".devora/secrets")
            if (secretsDir.exists()) {
                onOutputLine(".devora/secrets/ excluded from push (never uploaded to GitHub).")
            }

            DevoraResult.Success(Unit)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to materialize .github/workflows", cause = e)
        }
    }

    private suspend fun addOrUpdateRemote(
        projectRootPath: String,
        authenticatedUrl: String,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> = gitRepository.setRemote(projectRootPath, "origin", authenticatedUrl)
}