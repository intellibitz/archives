plugins {
    kotlin("js") apply true
}
group = "com.intellibitz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":shared"))
    testImplementation(kotlin("test-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.11.0")
}

kotlin {
    js {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport { }
            }
            runTask {
                cssSupport { }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport { }
                }
            }
        }
    }
}