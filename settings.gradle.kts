pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Devora"

include(":app")
include(":core:core-common")
include(":core:core-ui")
include(":core:core-logging")
include(":feature:project-manager")
include(":feature:file-manager")
include(":feature:terminal")
include(":feature:sdk-manager")
include(":feature:gradle-manager")
include(":feature:build-system")
include(":feature:workflow-engine")
include(":feature:artifact-manager")
include(":feature:apk-inspector")
include(":feature:signing")
include(":feature:git")
include(":feature:github")
include(":feature:secrets")
include(":feature:notifications")
include(":feature:plugin-system")
include(":feature:account-security")
include(":feature:editor")