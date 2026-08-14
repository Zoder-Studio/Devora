package dev.devora.feature.apkinspector.data

object AabInspectorContract {
    /**
     * Verified 2026-08-14 against https://github.com/google/bundletool/releases
     * RE-VERIFY before any production build.
     */
    const val BUNDLETOOL_VERSION = "1.15.6"
    const val BUNDLETOOL_DOWNLOAD_URL =
        "https://github.com/google/bundletool/releases/download/$BUNDLETOOL_VERSION/bundletool-all-$BUNDLETOOL_VERSION.jar"
    const val BUNDLETOOL_JAR_NAME = "bundletool.jar"
}