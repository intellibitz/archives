// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dependency.analysis)
}

allprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                when {
                    requested.group == "com.google.guava" && requested.name == "guava" -> useVersion("33.7.1-jre")
                    requested.group == "com.google.protobuf" && requested.name == "protobuf-java" -> useVersion("4.36.0")
                    requested.group == "com.squareup.okhttp3" -> useVersion("5.5.0")
                    requested.group == "com.fasterxml.jackson.core" -> useVersion("2.22.2")
                    requested.group == "org.apache.commons" && requested.name == "commons-collections4" -> useVersion("4.6.0")
                    requested.group == "commons-io" && requested.name == "commons-io" -> useVersion("2.22.0")
                    requested.group == "commons-codec" && requested.name == "commons-codec" -> useVersion("1.22.1")
                    requested.group == "org.json" && requested.name == "json" -> useVersion("20260814")
                    requested.group.startsWith("io.netty") -> useVersion("4.1.112.Final")
                    requested.group == "io.grpc" -> useVersion("1.83.1")
                    requested.group == "org.yaml" && requested.name == "snakeyaml" -> useVersion("2.6")
                }
            }
        }
    }
}
