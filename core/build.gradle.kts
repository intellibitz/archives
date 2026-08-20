plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "cc.thevar.blukit.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
