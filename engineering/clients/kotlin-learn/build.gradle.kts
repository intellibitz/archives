group = "intellibitz"
version = 0.1

plugins {
    // Apply the application plugin to add support for building a CLI application.
    application
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.4.0"
//    kotlin("multiplatform") version "1.4.0"
//    kotlin("js") version "1.4.0"
//    kotlin("kapt") version "1.4.0"
//    kotlin("plugin.serialization") version "1.4.0"
//    id("org.jetbrains.dokka") version "1.4.0-rc"
}

/*
kotlin{
    jvm{
        withJava()
    }
    js {
        browser()
    }
}
*/

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/kotlin/dokka")
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
}

application {
    // Define the main class for the application.
    mainClass.set("sample.AppKt")
    applicationName = "KotlinLearn"
}

// if normal source directory convention is not followed, define custom sourcesets
sourceSets.main {
    java.srcDirs(listOf("src/main/intellibitz"))
    resources.srcDirs(listOf("src/main/resources"))
}
sourceSets.test {
    java.srcDirs(listOf("src/test/intellibitz"))
    resources.srcDirs(listOf("src/test/resources"))
}
/*
tasks.dokkaHtml {
    outputDirectory = "$buildDir/dokka"
}
*/
/*
tasks.withType<Test> {
    useJUnitPlatform()
}
*/
dependencies {
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("stdlib-common"))
//    implementation(kotlin("stdlib-js"))
    implementation(kotlin("script-runtime"))
//    implementation(kotlin("reflect"))
    // Align versions of all Kotlin components
//    implementation(kotlin("bom"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0.0-rc") // JVM dependency
//    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.0-rc")
    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))
//    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.1.0") // for kotest framework
//    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.0") // for kotest core jvm assertions
//    testImplementation("io.kotest:kotest-property-jvm:4.1.0") // for kotest property test
}
/*
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}*/
