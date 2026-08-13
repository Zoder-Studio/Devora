package dev.devora.feature.terminal.data

/**
 * Constants defined by the real Termux app and Termux:API RUN_COMMAND
 * contract. These values are fixed by Termux itself and must not be
 * changed — see https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent
 */
object TermuxContract {
    const val TERMUX_PACKAGE_NAME = "com.termux"
    const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
    const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    const val EXTRA_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    const val EXTRA_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    const val EXTRA_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    const val EXTRA_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
    const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

    const val RESULT_EXTRA_STDOUT = "stdout"
    const val RESULT_EXTRA_STDERR = "stderr"
    const val RESULT_EXTRA_EXIT_CODE = "exitCode"
    const val RESULT_EXTRA_ERR = "err"
    const val RESULT_EXTRA_ERRMSG = "errmsg"
    const val RESULT_ACTION = "dev.devora.app.TERMUX_RUN_COMMAND_RESULT"

    /** Termux RUN_COMMAND session action: bring Termux to foreground with a new session. */
    const val SESSION_ACTION_SWITCH_TO_NEW = "0"
    const val TERMUX_PROPERTIES_PATH = "/data/data/com.termux/files/home/.termux/termux.properties"

    const val TERMUX_SHELL_BINARY = "/data/data/com.termux/files/usr/bin/bash"
}