rootProject.name = "multiplatform"
include(":shared", ":server", ":app", ":browser", ":console")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

//include(":server")
//include(":app")
//include(":browser")
//include(":console")

