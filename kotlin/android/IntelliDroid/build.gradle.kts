buildscript {
    // Top-level build file where you can add configuration options common to all sub-projects/modules.
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.3")
        classpath("com.google.gms:google-services:4.4.2")
    }
}

group = "intellibitz"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
