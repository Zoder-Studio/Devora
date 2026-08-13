package dev.devora.feature.apkinspector.domain.usecase

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.apkinspector.domain.model.ApkInspectionResult
import dev.devora.feature.apkinspector.domain.repository.AabInspectorRepository
import dev.devora.feature.apkinspector.domain.repository.ApkInspectorRepository

class InspectApkUseCase(private val repository: ApkInspectorRepository) {
    suspend operator fun invoke(apkFilePath: String): DevoraResult<ApkInspectionResult> =
        repository.inspectApk(apkFilePath)
}

class InspectAabUseCase(private val repository: AabInspectorRepository) {
    suspend operator fun invoke(aabFilePath: String, onOutputLine: (String) -> Unit): DevoraResult<String> {
        val setupResult = repository.setupBundletool(onOutputLine)
        if (setupResult is DevoraResult.Failure) return setupResult
        return repository.dumpManifest(aabFilePath, onOutputLine)
    }
}