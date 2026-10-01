plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply true
}

group = "org.sushmu"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.ktor.client)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.lincheck)
}

tasks.test {
    useJUnitPlatform()
//    useTestNG()
}

kotlin {
    jvmToolchain(21)
}