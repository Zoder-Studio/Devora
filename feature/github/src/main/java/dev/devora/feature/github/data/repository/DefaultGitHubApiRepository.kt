package dev.devora.feature.github.data.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.github.data.GitHubTokenStore
import dev.devora.feature.github.domain.model.GitHubOrg
import dev.devora.feature.github.domain.model.GitHubRepo
import dev.devora.feature.github.domain.repository.GitHubApiRepository
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DefaultGitHubApiRepository(
    private val tokenStore: GitHubTokenStore
) : GitHubApiRepository {

    override suspend fun listOrgs(): DevoraResult<List<GitHubOrg>> {
        val token = tokenStore.readToken() ?: return DevoraResult.Failure(message = "Not logged in to GitHub")
        return try {
            val response = get("https://api.github.com/user/orgs", token)
            val array = JSONArray(response)
            val orgs = (0 until array.length()).map { i -> GitHubOrg(array.getJSONObject(i).getString("login")) }
            DevoraResult.Success(orgs)
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to list organizations", cause = e)
        }
    }

    override suspend fun createRepo(owner: String?, name: String, private: Boolean): DevoraResult<GitHubRepo> {
        val token = tokenStore.readToken() ?: return DevoraResult.Failure(message = "Not logged in to GitHub")
        val url = if (owner == null) {
            "https://api.github.com/user/repos"
        } else {
            "https://api.github.com/orgs/$owner/repos"
        }

        return try {
            val body = JSONObject().apply {
                put("name", name)
                put("private", private)
            }.toString()

            val response = postJson(url, token, body)
            val json = JSONObject(response)

            if (json.has("message") && !json.has("full_name")) {
                return DevoraResult.Failure(message = "GitHub API error: ${json.getString("message")}")
            }

            DevoraResult.Success(
                GitHubRepo(
                    fullName = json.getString("full_name"),
                    cloneUrl = json.getString("clone_url"),
                    defaultBranch = json.optString("default_branch", "main"),
                    private = json.getBoolean("private")
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to create GitHub repository", cause = e)
        }
    }

    override suspend fun getRepo(owner: String, name: String): DevoraResult<GitHubRepo> {
        val token = tokenStore.readToken() ?: return DevoraResult.Failure(message = "Not logged in to GitHub")
        return try {
            val response = get("https://api.github.com/repos/$owner/$name", token)
            val json = JSONObject(response)
            DevoraResult.Success(
                GitHubRepo(
                    fullName = json.getString("full_name"),
                    cloneUrl = json.getString("clone_url"),
                    defaultBranch = json.optString("default_branch", "main"),
                    private = json.getBoolean("private")
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to fetch repository info", cause = e)
        }
    }

    private fun get(url: String, token: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("Authorization", "Bearer $token")
        return connection.inputStream.bufferedReader().readText()
    }

    private fun postJson(url: String, token: String, body: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(body.toByteArray()) }
        return connection.inputStream.bufferedReader().readText()
    }
}