plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

fun gitVersionName(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .redirectErrorStream(true)
            .start()
        val tag = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        when {
            tag.startsWith("v") -> tag.substring(1)
            tag.isNotBlank() -> tag
            else -> "0.0.0"
        }
    } catch (exception: Exception) {
        "0.0.0"
    }
}

fun gitVersionCode(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectErrorStream(true)
            .start()
        val count = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        count.toIntOrNull() ?: 1
    } catch (exception: Exception) {
        1
    }
}

android {
    namespace = "dev.devora.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.devora.app"
        minSdk = 26
        targetSdk = 35
        versionCode = gitVersionCode()
        versionName = gitVersionName()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-logging"))
    implementation(project(":feature:project-manager"))
    implementation(project(":feature:file-manager"))
    implementation(project(":feature:terminal"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:build-system"))
    implementation(project(":feature:workflow-engine"))
    implementation(project(":feature:artifact-manager"))
    implementation(project(":feature:apk-inspector"))
    implementation(project(":feature:signing"))
    implementation(project(":feature:git"))
    implementation(project(":feature:github"))
    implementation(project(":feature:secrets"))
    implementation(project(":feature:plugin-system"))
    implementation(project(":feature:account-security"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
}