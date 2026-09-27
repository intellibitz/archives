plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
}
group = "com.intellibitz"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
}

android {
    namespace = "intellibitz.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "intellibitz.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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