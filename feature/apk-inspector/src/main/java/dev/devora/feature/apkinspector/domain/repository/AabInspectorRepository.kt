package dev.devora.feature.apkinspector.domain.repository

import dev.devora.core.common.result.DevoraResult

interface AabInspectorRepository {
    fun isBundletoolInstalled(): Boolean

    suspend fun setupBundletool(onOutputLine: (String) -> Unit): DevoraResult<Unit>

    /**
     * Returns bundletool's raw "dump manifest" output as text. This is
     * NOT parsed into a structured model in this stage — the manifest
     * is protobuf internally and bundletool's text dump format is not
     * a stable contract to parse against. Devora shows the real,
     * unaltered output rather than guessing a schema that might break
     * on a bundletool update.
     */
    suspend fun dumpManifest(aabFilePath: String, onOutputLine: (String) -> Unit): DevoraResult<String>

    suspend fun dumpConfig(aabFilePath: String, onOutputLine: (String) -> Unit): DevoraResult<String>
}