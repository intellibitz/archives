import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.shadow)
    // Removed application plugin to avoid broken shadow distribution tasks in Gradle 8+
}

group = "cc.thevar.acc"
version = (project.findProperty("appVersion") as? String) ?: "1.0.0"

val mainClassName = "cc.thevar.acc.ApplicationKt"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the ACC Gateway server."
    mainClass.set(mainClassName)
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

val isRelease = project.hasProperty("release") || 
               gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

tasks.register<Copy>("copyWasmFrontend") {
    description = "Copies the Wasm frontend distribution to the gateway resources."
    val frontendTask = if (isRelease) ":frontend:web:wasmJsBrowserDistribution" 
                       else ":frontend:web:wasmJsBrowserDevelopmentExecutableDistribution"
    val frontendDir = if (isRelease) "productionExecutable" else "developmentExecutable"
    
    dependsOn(frontendTask)
    from(project(":frontend:web").layout.buildDirectory.dir("dist/wasmJs/$frontendDir"))
    into(project.layout.buildDirectory.dir("resources/main/static"))
}

tasks.processResources {
    dependsOn("copyWasmFrontend")
}

dependencies {
    api(project(":common"))
    implementation(libs.oshi.core)
    implementation(libs.kotlinx.io)
    implementation(libs.logback)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.mockk)
}
