plugins {
    // Apply the application plugin to add support for building a CLI application.
    application
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.0"
}

// keep this until all targets fully migrated
ant.importBuild("build.xml")

// if normal source directory convention is not followed, define custom sourcesets
sourceSets.main {
    java.srcDir("src")
    kotlin.srcDir("src")
    java.destinationDirectory.set(file("./out"))
}
sourceSets.test {
    java.srcDir("test")
    kotlin.srcDir("test")
    java.destinationDirectory.set(file("./out"))
}

/*
Makes compilation depend on the prepare task
Detaches package from the ant_build task and makes it depend on compileJava
Detaches assemble from the standard Gradle jar task and makes it depend on package instead
*/
/*
tasks {
    compileJava {
        dependsOn("init-sted")
    }
    named("deploy-sted") {
        setDependsOn(listOf(compileJava))
    }
    assemble {
        setDependsOn(listOf("run-sted"))
    }
}
*/

application {
    // Define the main class for the application.
    mainClass.set("intellibitz.sted.ComposeMainKt")
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("script-runtime"))
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    // Align versions of all Kotlin components
    implementation(kotlin("bom"))
    // Compose desktop dependencies
    implementation(compose.desktop.currentOs)
    
    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))
}
