package dev.devora.feature.apkinspector.data

/**
 * bundletool is Google's official tool for inspecting/building from
 * .aab files — AAB is a protobuf-based container, not something
 * Devora parses by hand. This mirrors the SDK Manager / nano approach:
 * use the real upstream tool, don't reimplement its format.
 *
 * VERIFY BEFORE PRODUCTION BUILD: version pinned below may be outdated —
 * check https://github.com/google/bundletool/releases for the current one.
 */
object AabInspectorContract {
    const val BUNDLETOOL_VERSION = "1.17.2"
    const val BUNDLETOOL_DOWNLOAD_URL =
        "https://github.com/google/bundletool/releases/download/$BUNDLETOOL_VERSION/bundletool-all-$BUNDLETOOL_VERSION.jar"
    const val BUNDLETOOL_JAR_NAME = "bundletool.jar"
}