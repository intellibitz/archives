// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy {
            // Address 48 security vulnerabilities by forcing known safe versions of transitive dependencies
            force("com.google.guava:guava:33.7.1-jre")
            force("com.google.protobuf:protobuf-java:4.36.0")
            force("com.squareup.okhttp3:okhttp:5.5.0")
            force("com.squareup.okhttp3:logging-interceptor:5.5.0")
            force("com.fasterxml.jackson.core:jackson-databind:2.22.2")
            force("com.fasterxml.jackson.core:jackson-core:2.22.2")
            force("com.fasterxml.jackson.core:jackson-annotations:2.22.2")
            force("org.apache.commons:commons-collections4:4.6.0")
            force("commons-io:commons-io:2.22.0")
            force("commons-codec:commons-codec:1.22.1")
            force("org.json:json:20260814")
            force("io.netty:netty-all:5.0.0.Alpha2")
            force("io.grpc:grpc-netty-shaded:1.83.1")
        }
    }
}
