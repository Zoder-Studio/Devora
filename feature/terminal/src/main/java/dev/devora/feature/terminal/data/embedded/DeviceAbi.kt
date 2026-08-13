package dev.devora.feature.terminal.data.embedded

import android.os.Build

/**
 * Maps Android's supported ABIs to the bootstrap archive naming used
 * by termux-packages bootstrap releases. Devora picks the first
 * supported ABI it has a matching bootstrap for; if none match, the
 * embedded engine cannot run on this device and Devora says so
 * plainly instead of guessing.
 */
object DeviceAbi {
    fun resolveBootstrapArch(): String? {
        val supported = Build.SUPPORTED_ABIS.toList()
        return when {
            supported.contains("arm64-v8a") -> "aarch64"
            supported.contains("armeabi-v7a") -> "arm"
            supported.contains("x86_64") -> "x86_64"
            supported.contains("x86") -> "i686"
            else -> null
        }
    }
}