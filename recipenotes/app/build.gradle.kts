// App module build.gradle.kts - Configures the Android application module.
// This is where we define SDK versions, dependencies, and build features.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP (Kotlin Symbol Processing) - used by Room and Hilt for code generation.
    // KSP is faster than KAPT (the older annotation processor) because it works
    // directly with Kotlin compiler symbols instead of going through Java stubs.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.recipenotes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.recipenotes"
        // minSdk 26 = Android 8.0 - gives us java.time APIs without desugaring
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android KTX - Kotlin extensions for common Android APIs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose UI - The modern Android UI toolkit (declarative, like SwiftUI/Flutter)
    // BOM ensures all Compose libraries use compatible versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation Compose - Handles screen-to-screen navigation with back stack
    implementation(libs.androidx.navigation.compose)

    // Room Database - SQLite wrapper with compile-time SQL verification.
    // Room has three components:
    //   - runtime: Core Room library
    //   - ktx: Kotlin coroutines/Flow support for reactive queries
    //   - compiler (via KSP): Generates DAO implementation code at compile time
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt - Dependency Injection framework.
    // DI lets us define "how to create objects" in one place (modules),
    // then Hilt automatically provides them where needed via @Inject.
    // This makes code testable (swap real DB for fake in tests) and modular.
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil - Lightweight image loading for Compose (handles caching, resizing, etc.)
    implementation(libs.coil.compose)
}
