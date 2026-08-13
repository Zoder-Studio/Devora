package dev.devora.feature.sdkmanager.domain.model

/**
 * One installable package as reported by "sdkmanager --list", e.g.
 * "platforms;android-35" or "build-tools;35.0.0". Devora surfaces
 * exactly what sdkmanager reports — it never filters or pre-selects
 * which SDKs the developer should use (spec section 7).
 */
data class SdkComponent(
    val packagePath: String,
    val displayName: String,
    val version: String,
    val isInstalled: Boolean
)

data class SdkListResult(
    val installed: List<SdkComponent>,
    val available: List<SdkComponent>
)