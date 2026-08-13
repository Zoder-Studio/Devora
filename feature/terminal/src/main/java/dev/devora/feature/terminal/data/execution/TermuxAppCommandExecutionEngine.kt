package dev.devora.feature.terminal.data.execution

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dev.devora.core.common.result.DevoraResult
import dev.devora.core.logging.DevoraLogger
import dev.devora.feature.terminal.data.TermuxContract
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

private const val TAG = "TermuxAppCommandEngine"

/**
 * Runs commands in the real Termux app via RUN_COMMAND and awaits the
 * real result through RUN_COMMAND_PENDING_INTENT, matching the same
 * documented mechanism used by Termux plugin apps (e.g. Termux:Tasker).
 * This gives TERMUX_APP mode the same "wait for completion, capture
 * output" contract as the embedded engine — no more fire-and-forget.
 */
class TermuxAppCommandExecutionEngine(
    private val context: Context
) : CommandExecutionEngine {

    private val requestCodeCounter = AtomicInteger(1000)

    override suspend fun run(
        workingDirectory: String,
        script: String,
        timeoutMillis: Long,
        onOutputLine: (String) -> Unit
    ): DevoraResult<Unit> {
        val result = withTimeoutOrNull(timeoutMillis) {
            awaitCommandResult(workingDirectory, script)
        } ?: return DevoraResult.Failure(message = "Timed out waiting for Termux to run: $script")

        result.stdout.lines().filter { it.isNotBlank() }.forEach(onOutputLine)

        return if (result.exitCode == 0) {
            DevoraResult.Success(Unit)
        } else {
            DevoraResult.Failure(
                message = "Command failed with exit code ${result.exitCode} in Termux.\n" +
                    "stderr:\n${result.stderr}",
                rawOutput = result.stdout + "\n" + result.stderr
            )
        }
    }

    private data class CommandResult(val stdout: String, val stderr: String, val exitCode: Int)

    private suspend fun awaitCommandResult(
        workingDirectory: String,
        script: String
    ): CommandResult = suspendCancellableCoroutine { continuation ->
        val requestCode = requestCodeCounter.incrementAndGet()
        val resultAction = "${TermuxContract.RESULT_ACTION}.$requestCode"

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                context.unregisterReceiver(this)
                val resultBundle = intent.getBundleExtra("result") ?: intent.extras
                val stdout = resultBundle?.getString(TermuxContract.RESULT_EXTRA_STDOUT).orEmpty()
                val stderr = resultBundle?.getString(TermuxContract.RESULT_EXTRA_STDERR).orEmpty()
                val exitCode = resultBundle?.getInt(TermuxContract.RESULT_EXTRA_EXIT_CODE, -1) ?: -1
                if (continuation.isActive) {
                    continuation.resume(CommandResult(stdout, stderr, exitCode))
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(resultAction),
            Context.RECEIVER_NOT_EXPORTED
        )

        val resultReceiverIntent = Intent(resultAction).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            resultReceiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        continuation.invokeOnCancellation {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // already unregistered, ignore
            }
        }

        try {
            val runIntent = Intent().apply {
                setClassName(TermuxContract.TERMUX_PACKAGE_NAME, TermuxContract.RUN_COMMAND_SERVICE)
                action = TermuxContract.RUN_COMMAND_ACTION
                putExtra(TermuxContract.EXTRA_COMMAND_PATH, TermuxContract.TERMUX_SHELL_BINARY)
                putExtra(TermuxContract.EXTRA_COMMAND_ARGUMENTS, arrayOf("-c", script))
                putExtra(TermuxContract.EXTRA_COMMAND_WORKDIR, workingDirectory)
                putExtra(TermuxContract.EXTRA_COMMAND_BACKGROUND, true)
                putExtra(TermuxContract.EXTRA_PENDING_INTENT, pendingIntent)
            }
            context.startForegroundService(runIntent)
        } catch (e: Exception) {
            DevoraLogger.e(TAG, "Failed to dispatch RUN_COMMAND", e)
            try {
                context.unregisterReceiver(receiver)
            } catch (ignored: IllegalArgumentException) {
            }
            if (continuation.isActive) {
                continuation.resume(CommandResult(stdout = "", stderr = e.message.orEmpty(), exitCode = -1))
            }
        }
    }

    override suspend fun runCapturing(
        workingDirectory: String,
        script: String,
        timeoutMillis: Long,
        onOutputLine: (String) -> Unit
    ): DevoraResult<CommandExecutionResult> {
        val result = withTimeoutOrNull(timeoutMillis) {
            awaitCommandResult(workingDirectory, script)
        } ?: return DevoraResult.Failure(message = "Timed out waiting for Termux to run: $script")

        result.stdout.lines().filter { it.isNotBlank() }.forEach(onOutputLine)
        result.stderr.lines().filter { it.isNotBlank() }.forEach(onOutputLine)

        return DevoraResult.Success(
            CommandExecutionResult(
                exitCode = result.exitCode,
                stdout = result.stdout,
                stderr = result.stderr,
                stderrAvailable = true
            )
        )
    }
}