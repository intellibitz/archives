plugins {
    kotlin("multiplatform") apply true
    id("com.android.library") apply true
    id("kotlin-parcelize") apply true
}
group = "com.intellibitz"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget()
    iosX64("ios") {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.15.0")
            }
        }
        val androidUnitTest by getting
    }
}

android {
    namespace = "intellibitz.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
