package dev.devora.feature.sdkmanager.data

object SdkManagerContract {
    const val DEFAULT_CMDLINE_TOOLS_DOWNLOAD_URL =
        "https://dl.google.com/android/repo/commandlinetools-linux-11076708_latest.zip"
    const val DEFAULT_CMDLINE_TOOLS_REVISION = "11076708"

    const val CMDLINE_TOOLS_DIR_NAME = "cmdline-tools"
    const val CMDLINE_TOOLS_LATEST_SUBDIR = "latest"
    const val SDKMANAGER_RELATIVE_PATH = "cmdline-tools/latest/bin/sdkmanager"
    const val JDK_APT_PACKAGE = "openjdk-17"

    const val REPOSITORY_MANIFEST_URL = "https://dl.google.com/android/repository/repository2-3.xml"

    fun sdkRoot(prefixPath: String): String = "$prefixPath/opt/android-sdk"
}