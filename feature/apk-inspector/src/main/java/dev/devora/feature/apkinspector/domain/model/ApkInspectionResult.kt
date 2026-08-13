package dev.devora.feature.apkinspector.domain.model

data class ApkGeneralInfo(
    val fileName: String,
    val fileSizeBytes: Long,
    val format: String,
    val sha256: String
)

data class ApkApplicationInfo(
    val packageName: String,
    val appLabel: String,
    val versionName: String?,
    val versionCode: Long,
    val iconAvailable: Boolean
)

data class ApkAndroidInfo(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int?,
    val supportedAbis: List<String>
)

data class ApkSigningInfo(
    val isSigned: Boolean,
    val schemesUsed: List<String>,
    val certificates: List<ApkCertificateInfo>
)

data class ApkCertificateInfo(
    val subject: String,
    val issuer: String,
    val sha256Fingerprint: String
)

data class ApkComponentsInfo(
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>
)

data class ApkPermissionsInfo(
    val requested: List<String>,
    val declared: List<String>
)

data class ApkFilesInfo(
    val hasClassesDex: Boolean,
    val classesDexCount: Int,
    val hasResourcesArsc: Boolean,
    val libDirectories: List<String>,
    val hasAssets: Boolean,
    val hasRes: Boolean
)

data class ApkInspectionResult(
    val general: ApkGeneralInfo,
    val application: ApkApplicationInfo,
    val android: ApkAndroidInfo,
    val signing: ApkSigningInfo,
    val components: ApkComponentsInfo,
    val permissions: ApkPermissionsInfo,
    val files: ApkFilesInfo
)