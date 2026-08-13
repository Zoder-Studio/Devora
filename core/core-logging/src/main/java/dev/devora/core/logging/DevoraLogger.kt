package dev.devora.core.logging

import android.util.Log

/**
 * Devora's logging boundary.
 *
 * Hard rule (spec section 27 / 33): never log tokens, DPAT values,
 * keystore passwords, or GitHub secrets. Callers must pass already
 * redacted messages; this class does not attempt to guess and scrub
 * secrets itself, since silent scrubbing can hide real error data.
 */
object DevoraLogger {
    private const val GLOBAL_TAG = "Devora"

    fun d(tag: String, message: String) {
        Log.d("$GLOBAL_TAG:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$GLOBAL_TAG:$tag", message, throwable)
    }

    fun w(tag: String, message: String) {
        Log.w("$GLOBAL_TAG:$tag", message)
    }
}