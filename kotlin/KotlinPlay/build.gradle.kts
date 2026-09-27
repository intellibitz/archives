//https://kotlinlang.org/docs/gradle-configure-project.html#set-a-dependency-on-a-kotlinx-library
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
