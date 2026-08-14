package dev.devora.core.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import dev.devora.core.ui.R

/**
 * JetBrainsMono Nerd Font — used for terminal, file manager, and any
 * other developer-facing surface per spec section 28
 * ("JetBrainsMono NerdFont untuk terminal/developer surfaces").
 * Requires core/core-ui/src/main/res/font/jetbrains_mono_nerd_font.ttf
 * to be present — see project setup notes.
 */
val NerdFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_nerd_font)
)

/**
 * A curated set of Nerd Font glyph codepoints used across Devora's
 * developer-facing screens (File Manager, Terminal, Git, SDK/Gradle
 * Manager). Codepoints are from the "nf-md" (Material Design) and
 * "nf-oct" (Octicons) Nerd Font icon sets — verify against
 * https://www.nerdfonts.com/cheat-sheet if a glyph does not render,
 * since exact codepoints can shift between Nerd Font releases.
 */
object NerdFontIcons {
    const val FOLDER = "\uf07b"
    const val FOLDER_OPEN = "\uf07c"
    const val FILE = "\uf15b"
    const val FILE_CODE = "\uf1c9"
    const val TERMINAL = "\uf120"
    const val GIT_BRANCH = "\uf418"
    const val GIT_COMMIT = "\uf417"
    const val PACKAGE = "\uf487"
    const val GEAR = "\uf013"
    const val CHECK = "\uf00c"
    const val CROSS = "\uf00d"
    const val WARNING = "\uf071"
    const val LOCK = "\uf023"
    const val KEY = "\uf084"
    const val CLOUD_UPLOAD = "\uf0ee"
    const val DOWNLOAD = "\uf019"
    const val REFRESH = "\uf021"
}