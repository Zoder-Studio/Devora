package dev.devora.feature.buildsystem.domain.model

enum class BuildStatus {
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED
}

/**
 * One execution of a Gradle task. Devora shows the real command it ran
 * and the real output — no summarized/paraphrased error messages
 * (spec section 19: "Jangan hanya menampilkan error yang sudah diringkas").
 */
data class BuildRun(
    val id: String,
    val projectRootPath: String,
    val gradleTask: String,
    val status: BuildStatus,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val exitCode: Int?,
    val stdoutLineCount: Int,
    val stderrLineCount: Int,
    val stderrAvailable: Boolean,
    val logFilePath: String
)