package dev.devora.feature.terminal.data.embedded

object BootstrapContract {
    /** Initial value only, used the first time PinnedBootstrapVersionStore has nothing saved yet. */
    const val DEFAULT_BOOTSTRAP_RELEASE_TAG = "bootstrap-2024.11.01-r1"

    const val GITHUB_OWNER = "termux"
    const val GITHUB_REPO = "termux-packages"

    fun downloadUrl(arch: String, releaseTag: String): String =
        "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/$releaseTag/bootstrap-$arch.zip"

    fun releasesApiUrl(): String =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases?per_page=30"
}