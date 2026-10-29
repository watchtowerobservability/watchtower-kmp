// Watchtower Kotlin Multiplatform: one common API that delegates to the native Android SDK
// (watchtower-android) and, on iOS, to watchtower-ios through a small Swift bridge. No product
// logic lives here.
plugins {
    kotlin("multiplatform") version "1.9.24"
    id("com.android.library") version "8.5.2"
}

group = "dev.watchtower"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
    // The Android SDK is published to GitHub Packages (needs a token with read:packages).
    maven("https://maven.pkg.github.com/watchtowerobservability/watchtower-android") {
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: ""
            password = System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
    mavenLocal()
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val androidMain by getting {
            dependencies { api("dev.watchtower:watchtower-android:0.1.1") }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
    }
}

android {
    namespace = "dev.watchtower.kmp"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
