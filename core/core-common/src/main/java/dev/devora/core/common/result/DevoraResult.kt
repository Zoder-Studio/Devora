package dev.devora.core.common.result

/**
 * Generic result wrapper used across all Devora modules.
 * Devora never swallows errors: [Failure] must carry the real
 * underlying cause so callers can surface it unmodified to the developer.
 */
sealed class DevoraResult<out T> {
    data class Success<T>(val data: T) : DevoraResult<T>()
    data class Failure(
        val message: String,
        val cause: Throwable? = null,
        val rawOutput: String? = null
    ) : DevoraResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): DevoraResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): DevoraResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (Failure) -> Unit): DevoraResult<T> {
        if (this is Failure) action(this)
        return this
    }
}