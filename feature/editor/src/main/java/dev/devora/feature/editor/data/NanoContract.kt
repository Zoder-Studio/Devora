package dev.devora.feature.editor.data

object NanoContract {
    const val NANO_BINARY_RELATIVE_PATH = "bin/nano"
    const val NANORC_PATH = "etc/nanorc"
    const val APT_SOURCES_LINE = "deb https://packages.termux.dev/apt/termux-main stable main"
    const val APT_SOURCES_LIST_RELATIVE_PATH = "etc/apt/sources.list"

    /**
     * Official install script from galenguyer/nano-syntax-highlighting.
     * Verified content (fetched 2026-08-12): requires "unzip" on PATH,
     * downloads the repo zip via wget, extracts into ~/.nano/, and
     * appends missing "include ..." lines into ~/.nanorc. Devora runs
     * this script unmodified rather than reimplementing its logic.
     */
    const val SYNTAX_INSTALL_SCRIPT_URL =
        "https://raw.githubusercontent.com/galenguyer/nano-syntax-highlighting/master/install.sh"

    /** Packages the install script itself depends on, beyond nano. */
    val SYNTAX_INSTALL_DEPENDENCIES = listOf("wget", "unzip")
}