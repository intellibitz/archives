import java.util.Properties

buildscript {
    dependencies {
        constraints {
            classpath("org.bouncycastle:bcprov-jdk18on:1.84") {
                because("CVE-2024-34447")
            }
            classpath("org.bouncycastle:bcpkix-jdk18on:1.84") {
                because("CVE-2026-5588")
            }
            classpath("io.netty:netty-codec-http:4.2.17.Final") {
                because("CVE-2026-59903")
            }
            classpath("org.apache.httpcomponents.client5:httpclient5:5.6.3") {
                because("CVE-2026-64607")
            }
            classpath("com.fasterxml.jackson.core:jackson-databind:2.18.9") {
                because("CVE-2026-54512, CVE-2026-59889, CVE-2026-54515")
            }
            classpath("io.opentelemetry:opentelemetry-api:1.62.0") {
                because("CVE-2026-45292")
            }
            classpath("org.bitbucket.b_c:jose4j:0.9.6") {
                because("CVE-2024-29371")
            }
        }
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.shadow) apply false
}

// Apply modular GitHub automation powers
apply(plugin = "automation-plugin")
apply(from = "github-automation.gradle.kts")

// Configuration cache compatible versioning helper
fun incrementVersion(version: String, isTest: Boolean): String {
    val isCurrentlyTest = version.contains("-test.")
    val base = version.split("-")[0]
    val parts = base.split(".").map { it.toInt() }.toMutableList()

    return if (isTest) {
        if (isCurrentlyTest) {
            val n = version.split("-test.")[1].toInt()
            "$base-test.${n + 1}"
        } else {
            parts[2] = parts[2] + 1
            "${parts.joinToString(".")}-test.1"
        }
    } else {
        if (isCurrentlyTest) {
            base
        } else {
            parts[2] = parts[2] + 1
            parts.joinToString(".")
        }
    }
}

tasks.register("releaseTest") {
    group = "publishing"
    description = "Increments version for test and pushes tag to GitHub."
    val propsFile = layout.projectDirectory.file("gradle.properties").asFile
    doLast {
        val props = Properties()
        props.load(propsFile.inputStream())
        val current = props["appVersion"] as String
        val next = incrementVersion(current, true)
        props["appVersion"] = next
        props.store(propsFile.outputStream(), "Updated by release task")

        // Manual ProcessBuilder to avoid configuration cache issues with project.exec
        fun git(vararg args: String) {
            val pb = ProcessBuilder("git", *args).inheritIO().start()
            if (pb.waitFor() != 0) throw GradleException("Git command failed: ${args.joinToString(" ")}")
        }

        git("add", "gradle.properties")
        git("commit", "-m", "chore: bump version to $next [test]")
        git("tag", "-a", "v$next", "-m", "Test Release $next")
        git("push", "origin", "HEAD", "--tags")

        println("Successfully triggered test release: v$next")
    }
}

tasks.register("releaseProduction") {
    group = "publishing"
    description = "Increments version for production and pushes tag to GitHub."
    val propsFile = layout.projectDirectory.file("gradle.properties").asFile
    doLast {
        val props = Properties()
        props.load(propsFile.inputStream())
        val current = props["appVersion"] as String
        val next = incrementVersion(current, false)
        props["appVersion"] = next
        props.store(propsFile.outputStream(), "Updated by release task")

        // Manual ProcessBuilder to avoid configuration cache issues with project.exec
        fun git(vararg args: String) {
            val pb = ProcessBuilder("git", *args).inheritIO().start()
            if (pb.waitFor() != 0) throw GradleException("Git command failed: ${args.joinToString(" ")}")
        }

        git("add", "gradle.properties")
        git("commit", "-m", "chore: bump version to $next [production]")
        git("tag", "-a", "v$next", "-m", "Production Release $next")
        git("push", "origin", "HEAD", "--tags")

        println("Successfully triggered production release: v$next")
    }
}

tasks.register<Exec>("pushToGitHub") {
    description = "Pushes the current branch to GitHub (origin)."
    group = "publishing"
    commandLine("git", "push", "origin", "HEAD")
}

subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.netty" && requested.name.startsWith("netty-")) {
                useVersion("4.2.17.Final")
                because("CVE-2026-59903")
            }
            if (requested.group == "org.apache.httpcomponents.client5" && requested.name == "httpclient5") {
                useVersion("5.6.3")
                because("CVE-2026-64607")
            }
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-databind") {
                useVersion("2.18.9")
                because("CVE-2026-54512, CVE-2026-59889, CVE-2026-54515")
            }
            if (requested.group == "io.opentelemetry" && requested.name.startsWith("opentelemetry-")) {
                useVersion("1.62.0")
                because("CVE-2026-45292")
            }
            if (requested.group == "org.bouncycastle" && requested.name.startsWith("bc")) {
                useVersion("1.84")
                because("CVE-2026-5588")
            }
            if (requested.group == "org.bitbucket.b_c" && requested.name == "jose4j") {
                useVersion("0.9.6")
                because("CVE-2024-29371")
            }
        }
    }
}
