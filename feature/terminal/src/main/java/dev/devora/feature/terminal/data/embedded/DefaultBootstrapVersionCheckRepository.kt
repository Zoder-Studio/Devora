package dev.devora.feature.terminal.data.embedded

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.terminal.domain.model.BootstrapVersionCheckResult
import dev.devora.feature.terminal.domain.repository.BootstrapVersionCheckRepository
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class DefaultBootstrapVersionCheckRepository(
    private val versionStore: PinnedBootstrapVersionStore
) : BootstrapVersionCheckRepository {

    override suspend fun checkForUpdate(): DevoraResult<BootstrapVersionCheckResult> {
        return try {
            val pinned = versionStore.read(BootstrapContract.DEFAULT_BOOTSTRAP_RELEASE_TAG)

            val connection = URL(BootstrapContract.releasesApiUrl()).openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return DevoraResult.Failure(
                    message = "GitHub releases API returned HTTP ${connection.responseCode}"
                )
            }

            val body = connection.inputStream.bufferedReader().readText()
            val releases = JSONArray(body)

            var latestBootstrapTag: String? = null
            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                val tag = release.getString("tag_name")
                if (tag.startsWith("bootstrap-")) {
                    latestBootstrapTag = tag
                    break
                }
            }

            val latest = latestBootstrapTag
                ?: return DevoraResult.Failure(message = "No bootstrap-* release found in termux-packages releases")

            DevoraResult.Success(
                BootstrapVersionCheckResult(
                    pinnedReleaseTag = pinned.releaseTag,
                    latestReleaseTag = latest,
                    isUpdateAvailable = latest != pinned.releaseTag
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to check bootstrap version", cause = e)
        }
    }
}