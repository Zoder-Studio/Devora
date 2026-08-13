package dev.devora.feature.artifactmanager.domain.install

import dev.devora.core.common.result.DevoraResult
import dev.devora.feature.artifactmanager.domain.model.Artifact

interface ArtifactInstaller {
    /** Only supports APK — AAB cannot be installed directly by Android (it must be turned into APKs via bundletool first, spec: Devora reads/inspects only, never fakes an install path that doesn't exist). */
    fun install(artifact: Artifact): DevoraResult<Unit>
}