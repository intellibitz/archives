plugins {
    // Apply the application plugin to add support for building a CLI application.
    application
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.4.0-rc"
}

// keep this until all targets fully migrated
ant.importBuild("build.xml")

// if normal source directory convention is not followed, define custom sourcesets
sourceSets.main {
    java.srcDir("src")
    java.outputDir = file("./out")
}
sourceSets.test {
    java.srcDir("test")
    java.outputDir = file("./out")
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
    mainClass.set("intellibitz.sted.Main")
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(kotlin("script-runtime"))
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    // Align versions of all Kotlin components
    implementation(kotlin("bom"))
    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))
}
