plugins {
    war
    kotlin("jvm") apply true
    kotlin("plugin.serialization") apply true
    id("com.google.cloud.tools.appengine") apply (true)
    id("org.gretty") apply(true)
//    id("org.akhikhl.gretty") apply(true)
//    `java-library`
//    application
}


dependencies {
    implementation(project(":shared"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("io.ktor:ktor-server-servlet:1.4.0")
//    implementation("io.ktor:ktor-server-jetty:1.4.0")
    implementation("io.ktor:ktor-html-builder:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly("com.google.appengine:appengine:1.9.82")
}

project(":server").evaluationDependsOn(":shared")
project(":server").evaluationDependsOnChildren()

gretty {
    servletContainer = "jetty11"
}

appengine {
    deploy {   // deploy configuration
        stopPreviousVersion = true  // default - stop the current version
        promote = true              // default - & make this the current version
//        version = 'v1'
//        project = "your GCP project ID"
    }
}

group = "com.intellibitz"   // Generated output GroupId
version = "1.0-SNAPSHOT"          // Version in generated output

// if normal source directory convention is not followed, define custom sourcesets
sourceSets {
    main {
        java.srcDirs(listOf("src/main/kotlin", "src/main/java"))
        resources.srcDirs(listOf("src/main/resources"))
    }
    test {
        java.srcDirs(listOf("src/test/kotlin", "src/test/java"))
        resources.srcDirs(listOf("src/test/resources"))
    }
}
//https://kotlinlang.org/docs/reference/using-gradle.html
//This plugin only works for Kotlin files so it is recommended that you keep Kotlin and Java files separately
// (in case the project contains Java files). If you don't store them separately , specify the source folder in the
// sourceSets block:
kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("src/main/kotlin")
//        kotlin.srcDirs(listOf("src/main/kotlin"))
    }
}
java {
    sourceSets["main"].apply {
        java.srcDirs(listOf("src/main/kotlin", "src/main/java"))
    }
}
sourceSets["main"].apply {
    java.srcDirs(listOf("src/main/kotlin", "src/main/java"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
// [END gradle]
/*
application {
    mainClassName = "ServerKt"
}
*/
