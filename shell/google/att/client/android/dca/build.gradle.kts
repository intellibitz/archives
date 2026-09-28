plugins {
    // Apply the application plugin to add support for building a CLI application.
    application
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "2.0.20"
}

// keep this until all targets fully migrated
ant.importBuild("build.xml")

// if normal source directory convention is not followed, define custom sourcesets
sourceSets.main {
    java.srcDir("src")
    kotlin.srcDir("src")
    java.outputDir = file("./out")
}
sourceSets.test {
    java.srcDir("tests/src")
    kotlin.srcDir("tests/src")
    java.outputDir = file("./out")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
