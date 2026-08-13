package dev.devora.feature.filemanager.domain.action

/**
 * File Manager delegates "open terminal here" and "open with Nano" to
 * these boundaries instead of implementing them itself (spec section 4:
 * "File Manager BUKAN editor"). Stage 4 provides the Terminal
 * implementation, Stage 5 provides the Nano implementation.
 */
interface OpenTerminalAtPathAction {
    fun openTerminal(directoryPath: String)
}

interface OpenFileInNanoAction {
    fun openInNano(filePath: String)
}

/**
 * No-op fallback used only until Stage 4/5 provide real implementations.
 * This is intentionally visible in the UI as unavailable rather than
 * silently doing nothing, per the "no fake behavior" rule.
 */
class UnavailableOpenTerminalAction : OpenTerminalAtPathAction {
    var lastRequestedPath: String? = null
        private set

    override fun openTerminal(directoryPath: String) {
        lastRequestedPath = directoryPath
    }
}

class UnavailableOpenNanoAction : OpenFileInNanoAction {
    var lastRequestedPath: String? = null
        private set

    override fun openInNano(filePath: String) {
        lastRequestedPath = filePath
    }
}