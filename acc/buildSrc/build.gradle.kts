plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("automationPlugin") {
            id = "automation-plugin"
            implementationClass = "AutomationPlugin"
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    // Eclipse releases repository for JGit
    maven {
        url = uri("https://repo.eclipse.org/content/repositories/releases/")
    }
    // Sonatype OSS in case artifacts are staged there
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/releases/")
    }
    // JitPack for any GitHub-hosted artifacts
    maven {
        url = uri("https://jitpack.io")
    }
}

// No external runtime dependencies required; tasks use the 'gh' and 'git' CLIs when available.
// Keep buildSrc lightweight to avoid external resolution issues.
