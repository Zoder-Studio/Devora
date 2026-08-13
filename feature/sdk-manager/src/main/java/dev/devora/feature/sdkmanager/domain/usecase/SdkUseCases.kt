package dev.devora.feature.sdkmanager.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.sdkmanager.domain.model.SdkListResult
import dev.devora.feature.sdkmanager.domain.repository.SdkRepository

class SetupSdkToolingUseCase(private val repository: SdkRepository) {
    suspend operator fun invoke(onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        repository.setupSdkTooling(onOutputLine)
}

class ListSdkPackagesUseCase(private val repository: SdkRepository) {
    suspend operator fun invoke(onOutputLine: (String) -> Unit): DevoraResult<SdkListResult> =
        repository.listPackages(onOutputLine)
}

class InstallSdkPackageUseCase(private val repository: SdkRepository) {
    suspend operator fun invoke(packagePath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        repository.installPackage(packagePath, onOutputLine)
}

class UninstallSdkPackageUseCase(private val repository: SdkRepository) {
    suspend operator fun invoke(packagePath: String, onOutputLine: (String) -> Unit): DevoraResult<Unit> =
        repository.uninstallPackage(packagePath, onOutputLine)
}