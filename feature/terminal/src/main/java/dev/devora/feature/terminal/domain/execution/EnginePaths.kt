package dev.devora.feature.terminal.domain.execution

interface EnginePaths {
    /** Root filesystem prefix for the currently active engine — where "bin/", "opt/", "home/" live. */
    fun currentPrefixPath(): String
}