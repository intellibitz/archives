//	The settings.gradle.kts file should include all subprojects.
//Each subproject should have its own build.gradle.kts file.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// toolchain resolver plugin below will enable the following in build.gradle.kts
//kotlin {
//    jvmToolchain(21)
//}
//If you use Gradle 8.0.2 or higher, you also need to add a toolchain resolver
// plugin. This type of plugin manages which repositories to download a
// toolchain from. As an example, add to your settings.gradle(.kts) the
// following plugin:
//https://docs.gradle.org/current/userguide/toolchains.html#sub:download_repositories
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

rootProject.name = "KotlinPlay"
include(":app")
