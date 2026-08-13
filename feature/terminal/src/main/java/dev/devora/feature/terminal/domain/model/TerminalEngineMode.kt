package dev.devora.feature.terminal.domain.model

/**
 * Devora supports two terminal engines:
 *
 * - TERMUX_APP: dispatches to the real, separately installed Termux app
 *   via RUN_COMMAND. No GPL code is linked into Devora's binary.
 *
 * - EMBEDDED_BOOTSTRAP: Devora extracts its own Termux bootstrap into
 *   its own PREFIX and renders sessions using the bundled
 *   com.termux:terminal-view library. This links GPLv3 code into
 *   Devora — see THIRD_PARTY_NOTICES.md.
 *
 * Devora always prefers TERMUX_APP when available and only falls back
 * to EMBEDDED_BOOTSTRAP when the Termux app is not installed, since
 * the app mode keeps license obligations simpler and reuses the
 * user's existing Termux setup (packages, config) instead of
 * duplicating a second Linux userland on the device.
 */
enum class TerminalEngineMode {
    TERMUX_APP,
    EMBEDDED_BOOTSTRAP
}