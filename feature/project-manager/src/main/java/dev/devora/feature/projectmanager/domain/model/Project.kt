package dev.devora.feature.projectmanager.domain.model

/**
 * Represents an Android/Gradle project tracked by Devora.
 *
 * [rootPath] always points to the real filesystem location. Devora
 * never copies or virtualizes project files — it operates directly
 * on the developer's own directory (spec section 3).
 */
data class Project(
    val id: String,
    val name: String,
    val rootPath: String,
    val hasDevoraMetadata: Boolean,
    val hasGradleWrapper: Boolean,
    val lastOpenedAtEpochMillis: Long?
)

/**
 * Fixed layout of the ".devora" metadata directory inside a project.
 * These paths are contract-level constants — feature modules must
 * reference them instead of hardcoding string paths elsewhere.
 */
object DevoraProjectLayout {
    const val METADATA_DIR_NAME = ".devora"
    const val WORKFLOWS_DIR_NAME = "workflows"
    const val SECRETS_DIR_NAME = "secrets"
    const val ENVIRONMENTS_DIR_NAME = "environments"

    fun metadataDir(projectRoot: String): String = "$projectRoot/$METADATA_DIR_NAME"
    fun workflowsDir(projectRoot: String): String =
        "${metadataDir(projectRoot)}/$WORKFLOWS_DIR_NAME"
    fun secretsDir(projectRoot: String): String =
        "${metadataDir(projectRoot)}/$SECRETS_DIR_NAME"
    fun environmentsDir(projectRoot: String): String =
        "${metadataDir(projectRoot)}/$ENVIRONMENTS_DIR_NAME"
    fun gradlewFile(projectRoot: String): String = "$projectRoot/gradlew"
    fun gradleWrapperProperties(projectRoot: String): String =
        "$projectRoot/gradle/wrapper/gradle-wrapper.properties"
    fun settingsGradleKts(projectRoot: String): String = "$projectRoot/settings.gradle.kts"
    fun settingsGradleGroovy(projectRoot: String): String = "$projectRoot/settings.gradle"
}