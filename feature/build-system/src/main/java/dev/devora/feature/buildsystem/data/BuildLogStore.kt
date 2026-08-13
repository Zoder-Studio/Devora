package dev.devora.feature.buildsystem.data

import android.content.Context
import java.io.File
import java.io.FileWriter

/**
 * Persists the raw, unmodified build log to a file per run. This is
 * what "Export" (spec section 20) copies out — the file content is
 * never summarized or altered from what the command actually produced.
 */
class BuildLogStore(private val context: Context) {

    private val logsDir: File
        get() = File(context.filesDir, "devora/build_logs").apply { mkdirs() }

    fun createWriterFor(buildRunId: String): Pair<File, FileWriter> {
        val file = File(logsDir, "$buildRunId.log")
        return file to FileWriter(file, /* append = */ true)
    }

    fun readTail(logFile: File, maxLines: Int): List<String> {
        if (!logFile.exists()) return emptyList()
        val allLines = logFile.readLines()
        return if (allLines.size <= maxLines) allLines else allLines.takeLast(maxLines)
    }

    fun countLines(logFile: File): Int {
        if (!logFile.exists()) return 0
        return logFile.bufferedReader().useLines { it.count() }
    }

    fun exportTo(logFile: File, destination: File) {
        logFile.copyTo(destination, overwrite = true)
    }
}