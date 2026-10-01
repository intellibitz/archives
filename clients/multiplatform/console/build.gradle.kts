plugins {
    kotlin("jvm") apply true
    application
}
group = "com.intellibitz"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":shared"))
    testImplementation(kotlin("test-junit"))
}

application {
    mainClass.set("MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
