plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.devora.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.devora.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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