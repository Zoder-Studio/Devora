package dev.devora.feature.sdkmanager.data

object SdkManagerContract {
    /**
     * Verified 2026-08-14 against
     * https://developer.android.com/studio#command-line-tools-only
     * RE-VERIFY before any production build; Google increments this
     * build number with every cmdline-tools release.
     */
    const val DEFAULT_CMDLINE_TOOLS_DOWNLOAD_URL =
        "https://dl.google.com/android/repo/commandlinetools-linux-15859902_latest.zip"
    const val DEFAULT_CMDLINE_TOOLS_REVISION = "15859902"

    const val CMDLINE_TOOLS_DIR_NAME = "cmdline-tools"
    const val CMDLINE_TOOLS_LATEST_SUBDIR = "latest"
    const val SDKMANAGER_RELATIVE_PATH = "cmdline-tools/latest/bin/sdkmanager"
    const val JDK_APT_PACKAGE = "openjdk-17"

    const val REPOSITORY_MANIFEST_URL = "https://dl.google.com/android/repository/repository2-3.xml"

    fun sdkRoot(prefixPath: String): String = "$prefixPath/opt/android-sdk"
}