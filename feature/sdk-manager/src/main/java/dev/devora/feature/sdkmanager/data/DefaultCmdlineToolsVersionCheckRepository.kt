package dev.devora.feature.sdkmanager.data

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.domain.model.CmdlineToolsVersionCheckResult
import dev.devora.feature.sdkmanager.domain.repository.CmdlineToolsVersionCheckRepository
import org.w3c.dom.Element
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Reads Google's official Android SDK repository manifest to find the
 * latest published "cmdline-tools;latest" revision, and compares it
 * against the version Devora currently has pinned. This never installs
 * anything — it only reports what is available, matching the same
 * read-only contract as DefaultBootstrapVersionCheckRepository.
 */
class DefaultCmdlineToolsVersionCheckRepository(
    private val versionStore: PinnedCmdlineToolsVersionStore
) : CmdlineToolsVersionCheckRepository {

    override suspend fun checkForUpdate(): DevoraResult<CmdlineToolsVersionCheckResult> {
        return try {
            val pinned = versionStore.read(
                PinnedCmdlineToolsVersion(
                    downloadUrl = SdkManagerContract.DEFAULT_CMDLINE_TOOLS_DOWNLOAD_URL,
                    revision = SdkManagerContract.DEFAULT_CMDLINE_TOOLS_REVISION
                )
            )

            val connection = URL(SdkManagerContract.REPOSITORY_MANIFEST_URL).openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return DevoraResult.Failure(
                    message = "Repository manifest request returned HTTP ${connection.responseCode}"
                )
            }

            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(connection.inputStream)
            document.documentElement.normalize()

            val remotePackages = document.getElementsByTagName("remotePackage")
            var matchedElement: Element? = null
            for (i in 0 until remotePackages.length) {
                val element = remotePackages.item(i) as Element
                if (element.getAttribute("path") == "cmdline-tools;latest") {
                    matchedElement = element
                    break
                }
            }

            val packageElement = matchedElement
                ?: return DevoraResult.Failure(
                    message = "No remotePackage with path=\"cmdline-tools;latest\" found in repository manifest"
                )

            val revisionElement = (packageElement.getElementsByTagName("revision").item(0) as? Element)
                ?: return DevoraResult.Failure(message = "No <revision> element found for cmdline-tools;latest")

            val major = revisionElement.getElementsByTagName("major").item(0)?.textContent?.trim().orEmpty()
            val minor = revisionElement.getElementsByTagName("minor").item(0)?.textContent?.trim().orEmpty()
            val micro = revisionElement.getElementsByTagName("micro").item(0)?.textContent?.trim().orEmpty()

            if (major.isBlank()) {
                return DevoraResult.Failure(message = "Could not read major revision number for cmdline-tools;latest")
            }
            val latestRevision = listOf(major, minor, micro).filter { it.isNotBlank() }.joinToString(".")

            val archiveUrl = findLinuxArchiveUrl(packageElement)
                ?: return DevoraResult.Failure(message = "No linux archive URL found for cmdline-tools;latest")

            DevoraResult.Success(
                CmdlineToolsVersionCheckResult(
                    pinnedRevision = pinned.revision,
                    latestRevision = latestRevision,
                    latestDownloadUrl = archiveUrl,
                    isUpdateAvailable = latestRevision != pinned.revision
                )
            )
        } catch (e: Exception) {
            DevoraResult.Failure(message = "Failed to check cmdline-tools version", cause = e)
        }
    }

    /**
     * The manifest lists per-OS archives inside <archives><archive>...
     * with a <host-os>linux</host-os> marker and a <complete><url>
     * pointing to a relative filename. The base download host is fixed
     * ("https://dl.google.com/android/repository/"), consistent with
     * how sdkmanager itself resolves these paths.
     */
    private fun findLinuxArchiveUrl(packageElement: Element): String? {
        val archiveElements = packageElement.getElementsByTagName("archive")
        for (i in 0 until archiveElements.length) {
            val archive = archiveElements.item(i) as Element
            val hostOs = archive.getElementsByTagName("host-os").item(0)?.textContent?.trim()
            if (hostOs == "linux") {
                val relativeUrl = archive.getElementsByTagName("url").item(0)?.textContent?.trim()
                if (!relativeUrl.isNullOrBlank()) {
                    return "https://dl.google.com/android/repository/$relativeUrl"
                }
            }
        }
        return null
    }
}