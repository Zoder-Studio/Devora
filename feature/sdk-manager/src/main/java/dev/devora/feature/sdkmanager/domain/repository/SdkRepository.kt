package dev.devora.feature.sdkmanager.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.domain.model.SdkListResult

interface SdkRepository {
    fun isSdkToolingInstalled(): Boolean

    suspend fun setupSdkTooling(onOutputLine: (String) -> Unit): DevoraResult<Unit>

    suspend fun listPackages(onOutputLine: (String) -> Unit): DevoraResult<SdkListResult>

    /** packagePath must be an exact sdkmanager package path, e.g. "platforms;android-35". */
    suspend fun installPackage(packagePath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit>

    suspend fun uninstallPackage(packagePath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit>
}