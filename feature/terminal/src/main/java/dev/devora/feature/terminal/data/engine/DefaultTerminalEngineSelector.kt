package dev.devora.feature.terminal.data.engine

import android.content.Context
import android.content.pm.PackageManager
import dev.devora.feature.terminal.data.TermuxContract
import dev.devora.feature.terminal.domain.engine.TerminalEngineSelector
import dev.devora.feature.terminal.domain.model.TerminalEngineMode

class DefaultTerminalEngineSelector(
    private val context: Context
) : TerminalEngineSelector {

    override fun selectMode(): TerminalEngineMode {
        return if (isTermuxAppInstalled()) {
            TerminalEngineMode.TERMUX_APP
        } else {
            TerminalEngineMode.EMBEDDED_BOOTSTRAP
        }
    }

    private fun isTermuxAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(TermuxContract.TERMUX_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}