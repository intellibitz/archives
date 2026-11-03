rootProject.name = "acc"

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        ivy {
            url = uri("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact]-v[revision]-[classifier].[ext]")
            }
            metadataSources { artifact() }
            content { includeGroup("org.nodejs") }
        }
        ivy {
            url = uri("https://github.com/yarnpkg/yarn/releases/download")
            patternLayout {
                artifact("v[revision]/[artifact]-v[revision].[ext]")
            }
            metadataSources { artifact() }
            content { includeGroup("com.yarnpkg") }
        }
        ivy {
            url = uri("https://github.com/WebAssembly/binaryen/releases/download")
            patternLayout {
                artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]")
            }
            metadataSources { artifact() }
            content { includeGroup("com.github.webassembly") }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// include(":frontend:android")
include(":frontend:desktop")
include(":frontend:composeApp")
include(":frontend:web")
include(":common")
include(":gateway")
