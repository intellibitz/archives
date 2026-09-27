//lib/build.gradle.kts
plugins {
    java
//    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register("task3"){
    println("REGISTER TASK3: This is executed during the configuration phase")
}

tasks.named("task3"){
    println("NAMED TASK3: This is executed during the configuration phase")
    doFirst {
        println("NAMED TASK3 - doFirst: This is executed during the execution phase")
    }
    doLast {
        println("NAMED TASK3 - doLast: This is executed during the execution phase")
    }
}
