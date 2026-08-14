package dev.devora.feature.terminal.data.embedded

object BootstrapContract {
    /**
     * Verified 2026-08-14 against https://github.com/termux/termux-packages/releases
     * Initial value only — PinnedBootstrapVersionStore is the actual
     * runtime source of truth once a device has installed once.
     * RE-VERIFY before any production build; this tag becomes stale
     * as termux-packages publishes new bootstrap releases regularly.
     */
    const val DEFAULT_BOOTSTRAP_RELEASE_TAG = "bootstrap-2026.08.09-r1+apt.android-7"

    const val GITHUB_OWNER = "termux"
    const val GITHUB_REPO = "termux-packages"

    fun downloadUrl(arch: String, releaseTag: String): String =
        "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/$releaseTag/bootstrap-$arch.zip"

    fun releasesApiUrl(): String =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases?per_page=30"
}