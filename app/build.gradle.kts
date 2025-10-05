import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Defines required plugins for Android application, Kotlin, Compose UI, and KSP (Kotlin Symbol Processing for Room DB).
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") // Required for Room Database code generation (M4 Persistence).
}

android {
    namespace = "com.csugprojects.recipeapp"
    // Sets the target Android version for compilation (Deployment Preparation).
    compileSdk = 36

    defaultConfig {
        applicationId = "com.csugprojects.recipeapp"
        // Sets the minimum required Android version (Deployment Preparation - M8 Compatibility).
        minSdk = 24
        // Sets the target Android version (Deployment Preparation).
        targetSdk = 36
        // Version tracking for Play Store submissions (Deployment Preparation/Release Strategy).
        versionCode = 1
        versionName = "1.0"

        // Specifies the test runner for Instrumented UI Tests (Testing Strategy - M8).
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        // Configuration for the production-ready build.
        release {
            // Enables R8 for code shrinking, obfuscation, and optimization (M6 Performance/Deployment Preparation).
            isMinifyEnabled = true
            // Specifies R8/Proguard rules to preserve necessary code (Deployment Preparation).
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Ensures Java compatibility across the project.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Ensures Kotlin compatibility.
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    // Activates Jetpack Compose features for the UI layer.
    buildFeatures {
        compose = true
    }
}

// Defines all project dependencies.
dependencies {

    // Core Android & Lifecycle dependencies (M4/M6 MVVM Architecture).
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose) // For screen navigation (M2 Navigation Flow).

    // Coroutines for asynchronous operations and performance optimization (M6 Performance).
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Library for EncryptedSharedPreferences (M6 Security Implementation).
    implementation(libs.androidx.security.crypto)

    // Compose Bill of Materials (BOM) manages all Compose versions.
    implementation(platform(libs.androidx.compose.bom))
    // Core Compose UI, Graphics, and Material 3 components for the View Layer (M2 UI/UX).
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Additional Compose dependencies for StateFlow observation and icons (M6 State Management).
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material)

    // Networking libraries for API access (M4 Technical Specifications).
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // JSON to Kotlin object conversion.
    implementation(libs.gson)

    // Room Database for local persistence of Favorites (M4 Persistence).
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // KSP is the annotation processor for Room.

    // Coil for efficient image loading (M6 Performance/Memory Management).
    implementation(libs.coil.compose)

    // --- TESTING DEPENDENCIES (Testing Strategy - M8) ---
    // Standard JUnit for unit tests (Repository, ViewModel logic).
    testImplementation(libs.junit)

    // AndroidX test extensions and Espresso for Instrumented UI Tests.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Tools for debugging the UI in development builds.
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Mocking framework for Unit and Instrumented Tests.
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk)

    // Coroutine testing utilities for suspend/Flow functions (Testing Strategy).
    testImplementation(libs.kotlinx.coroutines.test)
    // Core testing utilities often used with architecture components.
    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.core.testing)

    // Room's testing artifacts for in-memory database tests.
    androidTestImplementation(libs.androidx.room.testing)

}