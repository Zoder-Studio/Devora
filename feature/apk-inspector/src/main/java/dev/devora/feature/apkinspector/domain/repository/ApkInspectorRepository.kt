package dev.devora.feature.apkinspector.domain.repository

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.apkinspector.domain.model.ApkInspectionResult

interface ApkInspectorRepository {
    /** Reads metadata only — never modifies the artifact file (spec section 18: "Inspector harus membaca metadata tanpa memodifikasi artifact"). */
    suspend fun inspectApk(apkFilePath: String): DevoraResult<ApkInspectionResult>
}