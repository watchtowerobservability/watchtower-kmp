// Watchtower Kotlin Multiplatform: one common API that delegates to the native Android SDK
// (watchtower-android) and, on iOS, to watchtower-ios through a small Swift bridge. No product
// logic lives here.
plugins {
    kotlin("multiplatform") version "1.9.24"
    id("com.android.library") version "8.5.2"
    `maven-publish`
    signing
}

group = "io.github.watchtowerobservability"
// The release tag sets the version; local builds fall back to the last release.
version = (System.getenv("GITHUB_REF_NAME")?.takeIf { it.matches(Regex("v\\d.*")) } ?: "v0.1.1").removePrefix("v")

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
    androidTarget { publishLibraryVariants("release") }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val androidMain by getting {
            dependencies { api("io.github.watchtowerobservability:watchtower-android:0.1.2") }
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

// Maven Central: every publication needs POM metadata and a javadoc jar (empty is accepted for Kotlin).
val javadocJar by tasks.registering(Jar::class) { archiveClassifier.set("javadoc") }

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        pom {
            name.set("Watchtower Kotlin Multiplatform SDK")
            description.set("One Kotlin API over the native Watchtower Android and iOS SDKs.")
            url.set("https://github.com/watchtowerobservability/watchtower-kmp")
            licenses { license { name.set("Apache-2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0.txt") } }
            developers { developer { id.set("watchtower"); name.set("Watchtower"); url.set("https://github.com/watchtowerobservability") } }
            scm {
                url.set("https://github.com/watchtowerobservability/watchtower-kmp")
                connection.set("scm:git:https://github.com/watchtowerobservability/watchtower-kmp.git")
            }
        }
    }
    repositories {
        // Central Portal OSSRH staging API; credentials are CI secrets (repository absent without them).
        System.getenv("MAVEN_CENTRAL_USERNAME")?.takeIf { it.isNotBlank() }?.let { user ->
            maven("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/") {
                name = "MavenCentral"
                credentials {
                    username = user
                    password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: ""
                }
            }
        }
    }
}

// CI imports SIGNING_KEY into gpg; sign through the gpg binary (ed25519 safe).
signing {
    isRequired = !System.getenv("SIGNING_KEY").isNullOrBlank()
    if (isRequired) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
// KMP publications share artifacts; make every publish wait for every sign task (Gradle validation).
tasks.withType<AbstractPublishToMaven>().configureEach { dependsOn(tasks.withType<Sign>()) }
