#https://docs.gradle.org/current/userguide/userguide.html
#https://docs.gradle.org/current/userguide/quick_start.html

#Gradle supports Android, Java, Kotlin Multiplatform, Groovy, Scala, Javascript, and C/C++.
#The presence of the gradlew and gradlew.bat files in the root directory of a
# project is a clear indicator that Gradle is used.

$ gradle build #Gradle can be invoked in the command line

#The Wrapper is a script that invokes a declared version of Gradle and is the recommended way to execute a
# Gradle build. It is found in the project root directory as a gradlew or gradlew.bat file:
$ gradlew build     // Linux or OSX
$ gradlew.bat build  // Windows

#If you want to view or update the Gradle version of your project, use the
# command line. Do not edit the wrapper files manually:
$ ./gradlew --version
$ ./gradlew wrapper --gradle-version 8.10.2
$ ./gradlew --status

gradlew --help
gradlew -h

https://docs.gradle.org/current/userguide/getting_started_eng.html
#To execute a task called taskName on the root project, type:
$ gradle :taskName

#To pass an option to a task, prefix the option name with -- after the task name:
$ gradle taskName --exampleOption=exampleValue

#The primary purpose of the settings file is to add subprojects to your build.
<< 'settings.gradle.kts'
rootProject.name = "root-project"

include("sub-project-a")
include("sub-project-b")
include("sub-project-c")
settings.gradle.kts
#The settings file is a script. It is either a settings.gradle file written in Groovy or a
# settings.gradle.kts file in Kotlin.
#There is only one root project per build.

#In the build file, two types of dependencies can be added:
#The libraries and/or plugins on which Gradle and the build script depend.
#The libraries on which the project sources (i.e., source code) depend.
<< 'build.gradle.kts'
plugins {
    id("application")
}

application {
    mainClass = "com.example.Main"
}
build.gradle.kts
#Every Gradle build comprises at least one build script.

#Version catalogs provide a way to centralize your dependency declarations in a libs.versions.toml file.
#The version catalog typically contains four sections:
#[versions] to declare the version numbers that plugins and libraries will reference.
#[libraries] to define the libraries used in the build files.
#[bundles] to define a set of dependencies.
#[plugins] to define plugins.

#The file is located in the gradle directory so that it can be used by Gradle and IDEs automatically. The
# version catalog should be checked into source control: gradle/libs.versions.toml.
<< 'libs.versions.toml'
[versions]
androidGradlePlugin = "7.4.1"
mockito = "2.16.0"

[libraries]
googleMaterial = { group = "com.google.android.material", name = "material", version = "1.1.0-alpha05" }
mockitoCore = { module = "org.mockito:mockito-core", version.ref = "mockito" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "androidGradlePlugin" }
libs.versions.toml

#The following build.gradle.kts file adds a plugin and two dependencies to the project using the version catalog above:
<< 'build.gradle.kts'
plugins {
   alias(libs.plugins.androidApplication)
}

dependencies {
    // Dependency on a remote binary to compile and run the code
    implementation(libs.googleMaterial)

    // Dependency on a remote binary to compile and run the test code
    testImplementation(libs.mockitoCore)
}
build.gradle.kts

#You can view your dependency tree in the terminal using the ./gradlew :app:dependencies command:
$ ./gradlew :app:dependencies

#You run a Gradle build task using the gradle command or by invoking the Gradle Wrapper (./gradlew or gradlew.bat) in your project directory:
$ ./gradlew build

#All available tasks in your project come from Gradle plugins and build scripts.
#You can list all the available tasks in the project by running the following command in the terminal:
$ ./gradlew tasks
#You can also list the tasks only available in the app subproject by running
$ ./gradlew :app:tasks.
#You can obtain more information in the task listing using the --all option:
$ ./gradlew tasks --all

#The run task is executed with ./gradlew run:
$ ./gradlew run

#Many times, a task requires another task to run first.
$ ./gradlew build

#Plugins can be applied to a Gradle build script to add new tasks, configurations, or other build-related capabilities:
The Java Library Plugin - java-library
#Used to define and build Java libraries. It compiles Java source code with the compileJava task, generates Javadoc with the
# javadoc task, and packages the compiled classes into a JAR file with the jar task.

The Google Services Gradle Plugin - com.google.gms:google-services
#Enables Google APIs and Firebase services in your Android application with a configuration block called
# googleServices{} and a task called generateReleaseAssets.

The Gradle Bintray Plugin - com.jfrog.bintray
#Allows you to publish artifacts to Bintray by configuring the plugin using the bintray{} block.

#You apply plugins in the build script using a plugin id (a globally unique identifier / name) and a version:
<< 'build.gradle.kts'
plugins {
    id «plugin id» version «plugin version»
}
build.gradle.kts

#Core plugins are unique in that they provide short names, such as java for the core JavaPlugin, when applied in
# build scripts. They also do not require versions. To apply the java plugin to a project:
<< 'build.gradle.kts'
plugins {
    id("java")
}
build.gradle.kts

#Community plugins can be published at the Gradle Plugin Portal, where other Gradle users can easily discover and use them.
<< 'build.gradle.kts'
plugins {
    id("org.springframework.boot") version "3.1.5"
}
build.gradle.kts

#Incremental builds are always enabled, and the best way to see them in action is to turn on verbose mode.
# With verbose mode, each task state is labeled during a build:
$ ./gradlew compileJava --console=verbose

#When the build cache has been used to repopulate the local directory, the tasks are marked as FROM-CACHE:
$ ./gradlew compileJava --build-cache

#A build scan is a representation of metadata captured as you run your build.
#A Build Scan is a shareable and centralized record of a build and is available as a free service from Gradle.
#To enable build scans on a gradle command, add --scan to the command line option:
$ ./gradlew build --scan

https://docs.gradle.org/current/userguide/part1_gradle_init.html
$ gradle
$ gradle init --type java-application  --dsl kotlin

https://docs.gradle.org/current/userguide/part2_gradle_tasks.html#part2_begin
<< 'build.gradle.kts'
tasks.register<Copy>("copyTask") {
    from("source")
    into("target")
    include("*.war")
}

tasks.register("hello") {
    doLast {
        println("Hello!")
    }
}

tasks.register("greet") {
    doLast {
        println("How are you?")
    }
    dependsOn("hello")
}
build.gradle.kts

https://docs.gradle.org/current/userguide/part3_gradle_dep_man.html#part3_begin
<< 'build.gradle.kts'
repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // This dependency is used by the application.
    implementation(libs.guava)
}

Some key concepts in Gradle dependency management include:

Repositories - The source of dependencies → mavenCentral()
Maven Central is a collection of jar files, plugins, and libraries provided by the Maven community and backed by
 Sonatype. It is the de-facto public artifact store for Java and is used by many build systems.
Dependencies - Dependencies declared via configuration types → libs.junit.jupiter and libs.guava

Gradle needs specific information to find a dependency. Let’s look at
 libs.guava → com.google.guava:guava:32.1.2-jre and
  libs.junit.jupiter → org.junit.jupiter:junit-jupiter-api:5.9.1; they are broken down as follows:

Description	                            com.google.guava:guava:32.1.2-jre,	org.junit.jupiter:junit-jupiter-api:5.9.1
Group / identifier of an organization   com.google.guava , org.junit.jupiter
Name  / dependency identifier           guava , junit-jupiter-api
Version / version # to import           32.1.2-jre , 5.9.1
build.gradle.kts

https://docs.gradle.org/current/userguide/part4_gradle_plugins.html#part4_begin
#The Maven Publish Plugin provides the ability to publish build artifacts to an Apache Maven repository. It can also
# publish to Maven local which is a repository located on your machine.
<< 'build.gradle.kts'
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.gradle.tutorial"
            artifactId = "tutorial"
            version = "1.0"

            from(components["java"])
        }
    }
}
build.gradle.kts

#The publishToMavenLocal task builds the POM file and the artifacts to be published. It then installs them into the local Maven repository.
$ ./gradlew :app:publishToMavenLocal

#Plugins are used to extend build capability and customize Gradle. Using plugins is the primary mechanism for organizing build logic.
#Plugin authors can either keep their plugins private or distribute them to the public. As such, plugins are distributed three ways:

Core plugins - Gradle develops and maintains a set of Core Plugins.
Community plugins - Gradles community shares plugins via the Gradle Plugin Portal.
Custom plugins - Gradle enables user to create custom plugins using APIs.
Convention plugins are plugins used to share build logic between subprojects /modules .

#Users can wrap common logic in a convention plugin. For example, a code coverage plugin used as a convention plugin can
# survey code coverage for the entire project and not just a specific subproject.

Gradle highly recommends the use of Convention plugins.

https://docs.gradle.org/current/userguide/part5_gradle_inc_builds.html#part5_begin
$ ./gradlew :app:clean :app:build
$ ./gradlew :app:build

#There are four labels that developers can use to view task outcomes when verbose mode is turned on:
#OutcomeLabel	Description
UP-TO-DATE    Task that has been already executed and hasnt changed ~incremental build feature
SKIPPED       Task was explicitly prevented from running
FROM-CACHE    Task output has been copied to local directory from previous builds in the build cache ~caching feature
NO-SOURCE     Task was not executed because its required inputs were not available
#If there is no label, the task was newly executed by Gradle ~locally.

https://docs.gradle.org/current/userguide/part6_gradle_caching.html#part6_begin
https://docs.gradle.org/current/userguide/gradle_directories.html
#Add org.gradle.caching=true to the gradle.properties file:
<< 'gradle.properties'
org.gradle.console=verbose
org.gradle.caching=true
gradle.properties

$ ./gradlew :app:clean :app:build
$ ./gradlew :app:build

#Gradle lets us know the outcome of each task in the console output:
FROM-CACHE - tasks have been fetched from the local build cache.
UP-TO-DATE - tasks that used incremental build and were not re-run.

#To summarize:
First, we used the build task to populate our local cache with task inputs and outputs, we can imagine this was done a week ago.
Then, we used the clean task to mimic switching branches, overriding previous outputs.
Finally, we used the build task, unlike incremental builds, the previous outputs were stored in the local cache and could be reused.

Gradle is efficient, especially with the local build cache turned on. Gradle will look at the cache directory on your
 machine to check for output files that may already exist. If they do, instead of running that task, it will copy its
  ~output results into your project build directory.
The outcome label FROM-CACHE lets the user know that Gradle has fetched the task results from the local build cache.

https://docs.gradle.org/current/userguide/part7_gradle_refs.html#part7_begin
#http://gradle.org/docs/current/javadoc/
https://docs.gradle.org/current/dsl/index.html
https://docs.gradle.org/current/kotlin-dsl/index.html
https://docs.gradle.org/current/userguide/plugin_reference.html#plugin_reference
#https://plugins.gradle.org/
#https://gradle.org/releases/
http://gradle.org/docs/current/release-notes
#https://discuss.gradle.org/
#https://gradle-community.slack.com/
#https://gradle.org/courses/

https://docs.gradle.org/current/userguide/command_line_interface.html#command_line_interface

#Authoring Gradle Builds
https://docs.gradle.org/current/userguide/getting_started_dev.html
Gradle User Home directory
By default, the Gradle User Home ~~/.gradle or C:\Users\<USERNAME>\.gradle~ stores global configuration properties,
 initialization scripts, caches, and log files.
It can be set with the environment variable GRADLE_USER_HOME.
Not to be confused with the GRADLE_HOME, the optional installation directory for Gradle.
It is roughly structured as follows:

├── caches
│   ├── 4.8
│   ├── 4.9
│   ├── ⋮
│   ├── jars-3
│   └── modules-2
├── daemon
│   ├── ⋮
│   ├── 4.8
│   └── 4.9
├── init.d
│   └── my-setup.gradle
├── jdks
│   ├── ⋮
│   └── jdk-14.0.2+12
├── wrapper
│   └── dists
│       ├── ⋮
│       ├── gradle-4.8-bin
│       ├── gradle-4.9-all
│       └── gradle-4.9-bin
└── gradle.properties

Global cache directory ~for everything that is not project-specific~.
Version-specific caches ~e.g., to support incremental builds~.
Shared caches ~e.g., for artifacts of dependencies~.
Registry and logs of the Gradle Daemon.
Global initialization scripts.
JDKs downloaded by the toolchain support.
Distributions downloaded by the Gradle Wrapper.
Global Gradle configuration properties.
Consult the Gradle Directories reference to learn more.
https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home

Project Root directory
The project root directory contains all source files from your project.
It also contains files and directories Gradle generates, such as .gradle and build.
~While gradle is usually checked into source control, the build directory contains the output of your builds as well as
 transient files Gradle uses to support features like incremental builds.
The anatomy of a typical project root directory looks as follows:

├── .gradle
│   ├── 4.8
│   ├── 4.9
│   └── ⋮
├── build
├── gradle
│   └── wrapper
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── subproject-one
   └── build.gradle.kts
├── subproject-two
   └── build.gradle.kts
└── ⋮

Project-specific cache directory generated by Gradle.
Version-specific caches ~e.g., to support incremental builds~.
The build directory of this project into which Gradle generates all build artifacts.
Contains the JAR file and configuration of the Gradle Wrapper.
Project-specific Gradle configuration properties.
Scripts for executing builds using the Gradle Wrapper.
The projects settings file where the list of subprojects is defined.
Usually, a project is organized into one or multiple subprojects.
Each subproject has its own Gradle build script.
Consult the Gradle Directories reference to learn more.
https://docs.gradle.org/current/userguide/directory_layout.html#dir:project_root

https://docs.gradle.org/current/userguide/intro_multi_project_builds.html
#A multi-project build consists of one root project and one or more subprojects.
The following represents the structure of a multi-project build that contains two subprojects:
The directory structure should look as follows:

├── .gradle
│   └── ⋮
├── gradle
│   ├── libs.version.toml
│   └── wrapper
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── sub-project-1
│   └── build.gradle.kts
├── sub-project-2
│   └── build.gradle.kts
└── sub-project-3
    └── build.gradle.kts

The settings.gradle.kts file should include all subprojects.
Each subproject should have its own build.gradle.kts file.
#The Gradle community has two standards for multi-project build structures:
Multi-Project Builds using buildSrc - where buildSrc is a subproject-like directory at the Gradle project root containing all the build logic.
~For example, a build that has many modules called mobile-app, web-app, api, lib, and documentation could be structured as follows:

.
├── gradle
├── gradlew
├── settings.gradle.kts
├── buildSrc
│   ├── build.gradle.kts
│   └── src/main/kotlin/shared-build-conventions.gradle.kts
├── mobile-app
│   └── build.gradle.kts
├── web-app
│   └── build.gradle.kts
├── api
│   └── build.gradle.kts
├── lib
│   └── build.gradle.kts
└── documentation
    └── build.gradle.kts

The modules will have dependencies between them such as web-app and mobile-app depending on lib. This means that in
 order for Gradle to build web-app or mobile-app, it must build lib first.

In this example, the root settings file will look as follows:

<< 'settings.gradle.kts'
include("mobile-app", "web-app", "api", "lib", "documentation")
settings.gradle.kts
The order in which the subprojects ~modules~ are included does not matter.

Composite Builds - a build that includes other builds where build-logic is a build directory at the Gradle project root containing reusable build logic.
#Multi-Project Builds using buildSrc
Multi-project builds allow you to organize projects with many modules, wire dependencies between those modules, and
easily share common build logic amongst themi-project builds allow you to organize projects with many modules,
wire dependencies between those modules, and easily share common build logic amongst them

https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:using_buildsrc
The buildSrc directory is automatically recognized by Gradle. It is a good place to define and maintain shared configuration or
 imperative build logic, such as custom tasks or plugins.
buildSrc is automatically included in your build as a special subproject if a build.gradle.kts file is found under buildSrc.
~If the java plugin is applied to the buildSrc project, the compiled code from buildSrc/src/main/java is put in the
 classpath of the root build script, making it available to any subproject ~web-app, mobile-app, lib, etc., in the build.

https://docs.gradle.org/current/userguide/composite_builds.html#defining_composite_builds
Composite Builds, also referred to as included builds, are best for sharing logic between builds ~not subprojects~ or
 isolating access to shared build logic ~i.e., convention plugins~.

Let’s take the previous example. The logic in buildSrc has been turned into a project that contains plugins and can be
 published and worked on independently of the root project build.
The plugin is moved to its own build called build-logic with a build script and settings file:

.
├── gradle
├── gradlew
├── settings.gradle.kts
├── build-logic
│   ├── settings.gradle.kts
│   └── conventions
│       ├── build.gradle.kts
│       └── src/main/kotlin/shared-build-conventions.gradle.kts
├── mobile-app
│   └── build.gradle.kts
├── web-app
│   └── build.gradle.kts
├── api
│   └── build.gradle.kts
├── lib
│   └── build.gradle.kts
└── documentation
    └── build.gradle.kts

The fact that build-logic is located in a subdirectory of the root project is irrelevant.
The folder could be located outside the root project if desired.
<< 'settings.gradle.kts'
pluginManagement {
    includeBuild("build-logic")
}
include("mobile-app", "web-app", "api", "lib", "documentation")
settings.gradle.kts

A project path has the following pattern: it starts with an optional colon, which denotes the root project.
The root project, :, is the only project in a path not specified by its name.
The rest of a project path is a colon-separated sequence of project names, where the next project is a subproject of the previous project:
:sub-project-1

gradlew -q projects

https://docs.gradle.org/current/userguide/multi_project_builds.html#multi_project_builds
#Multi-project builds are collections of tasks you can run. The difference is that you may want to control which project’s tasks get executed.
#Executing tasks by name
The command gradle test will execute the test task in any subprojects relative to the current working directory that has that task.
~If you run the command from the root project directory, you will run test in api, shared, services:shared and services:webservice.
~If you run the command from the services project directory, you will only execute the task in services:shared and services:webservice.
The basic rule behind Gradle’s behavior is to execute all tasks down the hierarchy with this name. And complain if there
 is no such task found in any of the subprojects traversed.
Some task selectors, like help or dependencies, will only run the task on the project they are invoked on and not on all the
 subprojects to reduce the amount of information printed on the screen.
#Executing tasks by fully qualified name
You can use a task’s fully qualified name to execute a specific task in a particular subproject.
~For example: gradle :services:webservice:build will run the build task of the webservice subproject.
The fully qualified name of a task is its project path plus the task name.
This approach works for any task, so if you want to know what tasks are in a particular subproject, use the
 tasks task, e.g. gradle :services:webservice:tasks.

The build task is typically used to compile, test, and check a single project.
$ gradle :api:build
The buildNeeded task builds AND tests all the projects from the project dependencies of the testRuntime configuration:
$ gradle :api:buildNeeded
The buildDependents task tests ALL the projects that have a project dependency ~in the testRuntime configuration~ on the specified project:
$ gradle :api:buildDependents
Finally, you can build and test everything in all projects. Any task you run in the root project folder will
 cause that same-named task to be run on all the children.
You can run gradle build to build and test ALL projects.
$ gradle build

https://docs.gradle.org/current/userguide/build_lifecycle.html#build_lifecycle
https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:task_dependencies
https://docs.gradle.org/current/userguide/incremental_build.html#sec:task_inputs_outputs
#A Gradle build has three distinct phases. Gradle runs these phases in order:
Phase 1. Initialization
In the initialization phase, Gradle detects the set of projects ~root and subprojects~ and
 included builds participating in the build.
Detects the settings.gradle~.kts~ file. Creates a Settings instance.
Evaluates the settings file to determine which projects ~and included builds~ make up the build.
Creates a Project instance for every project.

Phase 2. Configuration
In the configuration phase, Gradle adds tasks and other properties to the projects found by the initialization phase.
Evaluates the build scripts, build.gradle~.kts~, of every project participating in the build.
Creates a task graph for requested tasks.

Phase 3. Execution
In the execution phase, Gradle runs tasks.
Gradle uses the task execution graphs generated by the configuration phase to determine which tasks to execute.
Schedules and executes the selected tasks.
Dependencies between tasks determine execution order.
Execution of tasks can occur in parallel.

The following example shows which parts of settings and build files correspond to various build phases:
<< 'settings.gradle.kts'
rootProject.name = "basic"
println("This is executed during the initialization phase.")
settings.gradle.kts
<< 'build.gradle.kts'
println("This is executed during the configuration phase.")

tasks.register("configured") {
    println("This is also executed during the configuration phase, because :configured is used in the build.")
}

tasks.register("test") {
    doLast {
        println("This is executed during the execution phase.")
    }
}

tasks.register("testBoth") {
    doFirst {
        println("This is executed first during the execution phase.")
    }
    doLast {
        println("This is executed last during the execution phase.")
    }
    println("This is executed during the configuration phase as well, because :testBoth is used in the build.")
}
build.gradle.kts

The following command executes the test and testBoth tasks specified above. Because Gradle only configures requested
 tasks and their dependencies, the configured task never configures:
> gradle test testBoth
This is executed during the initialization phase.
> Configure project :
This is executed during the configuration phase.
This is executed during the configuration phase as well, because :testBoth is used in the build.
> Task :test
This is executed during the execution phase.
> Task :testBoth
This is executed first during the execution phase.
This is executed last during the execution phase.

https://docs.gradle.org/current/userguide/writing_settings_files.html#writing_settings_files
The settings file is the entry point of every Gradle build.
Early in the Gradle Build lifecycle, the initialization phase finds the settings file in your project root directory.
When the settings file ~settings.gradle.kts is found, Gradle instantiates a Settings object.
One of the purposes of the Settings object is to allow you to declare all the projects to be included in the build.
Before Gradle assembles the projects for a build, it creates a Settings instance and executes the settings file against it.
As the settings script executes, it configures this Settings. Therefore, the settings file defines the Settings object.
There is a one-to-one correspondence between a Settings instance and a ~settings.gradle.kts file.

https://docs.gradle.org/current/javadoc/org/gradle/api/initialization/Settings.html
The Settings object is part of the Gradle API.
In the Kotlin DSL, the Settings object documentation is found here.
https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api.initialization/-settings/index.html
Many top-level properties and blocks in a settings script are part of the Settings API.
~For example, we can set the root project name in the settings script using the Settings.rootProject property:
<< 'settings.gradle.kts'
settings.rootProject.name = "root"
#Which is usually shortened to:
rootProject.name = "root"
settings.gradle.kts

#The Settings object exposes a standard set of properties in your settings script.
The following table lists a few commonly used properties:
Name	Description
buildCache
The build cache configuration.
plugins
The container of plugins that have been applied to the settings.
rootDir
The root directory of the build. The root directory is the project directory of the root project.
rootProject
The root project of the build.
settings
Returns this settings object.

The following table lists a few commonly used methods:
Name	Description
~include~~
Adds the given projects to the build.
~includeBuild~~
Includes a build at the specified path to the composite build.

A Settings script is a series of method calls to the Gradle API that often use {  }, a special shortcut in both the
 Groovy and Kotlin languages. A { } block is called a lambda in Kotlin or a closure in Groovy.
Simply put, the plugins{ } block is a method invocation in which a Kotlin lambda object or Groovy closure object is
 passed as the argument. It is the short form for:
<< 'settings.gradle.kts'
plugins(function() {
    id("plugin")
})
settings.gradle.kts
Blocks are mapped to Gradle API methods.
The code inside the function is executed against a this object called a receiver in Kotlin lambda and a delegate in
 Groovy closure. Gradle determines the correct this object and invokes the correct corresponding method. The this of the
  method invocation id~"plugin"~ object is of type PluginDependenciesSpec.

The settings file is composed of Gradle API calls built on top of the DSLs. Gradle executes the script line by line, top to bottom.
<< 'settings.gradle.kts'
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "root-project"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("sub-project-a")
include("sub-project-b")
include("sub-project-c")
settings.gradle.kts

1. Define the location of plugins
The settings file can optionally manage plugin versions and repositories for your build with pluginManagement It
 provides a centralized way to define which plugins should be used in your project and from which repositories they should be resolved.

<< 'settings.gradle.kts'
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
settings.gradle.kts
2. Apply settings plugins
The settings file can optionally apply plugins that are required for configuring the settings of the project. These are
 commonly the Develocity plugin and the Toolchain Resolver plugin in the example below.
Plugins applied in the settings file only affect the Settings object.

<< 'settings.gradle.kts'
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
settings.gradle.kts
3. Define the root project name
The settings file defines your project name using the rootProject.name property:

<< 'settings.gradle.kts'
rootProject.name = "root-project"
settings.gradle.kts
There is only one root project per build.
4. Define dependency resolution strategies
The settings file can optionally define rules and configurations for dependency resolution across your projects. It
 provides a centralized way to manage and customize dependency resolution.

<< 'settings.gradle.kts'
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
}
settings.gradle.kts
You can also include version catalogs in this section.
5. Add subprojects to the build
The settings file defines the structure of the project by adding all the subprojects using the include statement:

<< 'settings.gradle.kts'
include("app")
include("business-logic")
include("data-model")
settings.gradle.kts
You can also include entire builds using includeBuild.

There are many more properties and methods on the Settings object that you can use to configure your build.
It’s important to remember that while many Gradle scripts are typically written in short Groovy or Kotlin syntax, every
 item in the settings script is essentially invoking a method on the Settings object in the Gradle API:

<< 'settings.gradle.kts'
include("app")
#Is actually:
settings.include("app")
settings.gradle.kts
Additionally, the full power of the Groovy and Kotlin languages is available to you.
~For example, instead of using include many times to add subprojects, you can iterate over the list of directories in
 the project root folder and include them automatically:

<< 'settings.gradle.kts'
rootDir.listFiles().filter { it.isDirectory && (new File(it, "build.gradle.kts").exists()) }.forEach {
    include(it.name)
}
settings.gradle.kts
~This type of logic should be developed in a plugin.

https://docs.gradle.org/current/userguide/writing_build_scripts.html#writing_build_scripts
The initialization phase in the Gradle Build lifecycle finds the root project and subprojects included in your
 project root directory using the settings file.
https://docs.gradle.org/current/userguide/directory_layout.html#dir:project_root
Then, for each project included in the settings file, Gradle creates a Project instance. Gradle then looks for a
 corresponding build script file, which is used in the configuration phase.

Every Gradle build comprises one or more projects; a root project and subprojects.
A project typically corresponds to a software component that needs to be built, like a library or an application. It
 might represent a library JAR, a web application, or a distribution ZIP assembled from the JARs produced by other projects.
On the other hand, it might represent a thing to be done, such as deploying your application to staging or production environments.
Gradle scripts are written in either Groovy DSL or Kotlin DSL ~domain-specific language~.
A build script configures a project and is associated with an object of type Project.
As the build script executes, it configures Project.
The build script is either a *.gradle file in Groovy or a *.gradle.kts file in Kotlin.
Build scripts configure Project objects and their children.

The Project object is part of the Gradle API:
https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html
In the Kotlin DSL, the Project object documentation is found here.
https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api/-project/index.html
Many top-level properties and blocks in a build script are part of the Project API.
~For example, the following build script uses the Project.name property to print the name of the project:
https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:name

<< 'build.gradle.kts'
println(name)
println(project.name)
#The first uses the top-level reference to the name property of the Project object. The second statement uses the
# project property available to any build script, which returns the associated Project object.
build.gradle.kts

$ gradle -q check

#The Project object exposes a standard set of properties in your build script.
The following table lists a few commonly used properties:
#Name	Type	Description
name String
The name of the project directory.
path String
The fully qualified name of the project.
description String
A description for the project.
dependencies DependencyHandler
Returns the dependency handler of the project.
repositories RepositoryHandler
Returns the repository handler of the project.
layout ProjectLayout
Provides access to several important locations for a project.
group Object
The group of this project.
version Object
The version of this project.

The following table lists a few commonly used methods:
#Name	Description
uri~~
Resolves a file path to a URI, relative to the project directory of this project.
task~~
Creates a Task with the given name and adds it to this project.

<< 'build.gradle.kts'
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("application")
}
repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.guava:guava:32.1.1-jre")
}
application {
    mainClass = "com.example.Main"
}
tasks.named<Test>("test") {
    useJUnitPlatform()
}
build.gradle.kts

1. Apply plugins to the build
Plugins are used to extend Gradle. They are also used to modularize and reuse project configurations.
Plugins can be applied using the PluginDependenciesSpec plugins script block.
The plugins block is preferred:
<< 'build.gradle.kts'
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("application")
}
build.gradle.kts
In the example, the application plugin, which is included with Gradle, has been applied, describing our project as a Java application.
The Kotlin gradle plugin, version 2.0.20, has also been applied. This plugin is not included with Gradle and, therefore, has to be
 described using a plugin id and a plugin version so that Gradle can find and apply it.

2. Define the locations where dependencies can be found
A project generally has a number of dependencies it needs to do its work. Dependencies include plugins, libraries, or
 components that Gradle must download for the build to succeed.
The build script lets Gradle know where to look for the binaries of the dependencies. More than one location can be provided:
<< 'build.gradle.kts'
repositories {
    mavenCentral()
    google()
}
build.gradle.kts
In the example, the guava library and the JetBrains Kotlin plugin ~org.jetbrains.kotlin.jvm~ will be downloaded from the Maven Central Repository.

3. Add dependencies
A project generally has a number of dependencies it needs to do its work. These dependencies are often libraries of
precompiled classes that are imported in the projects source code.
Dependencies are managed via configurations and are retrieved from repositories.
https://docs.gradle.org/current/userguide/glossary.html#sub:terminology_configuration
Use the DependencyHandler returned by Project.getDependencies~~ method to manage the dependencies. Use the
 RepositoryHandler returned by Project.getRepositories~~ method to manage the repositories.
<< 'build.gradle.kts'
dependencies {
    implementation("com.google.guava:guava:32.1.1-jre")
}
build.gradle.kts
In the example, the application code uses Google guava libraries. Guava provides utility methods for collections,
caching, primitives support, concurrency, common annotations, string processing, I/O, and validations.

4. Set properties
A plugin can add properties and methods to a project using extensions.
The Project object has an associated ExtensionContainer object that contains all the settings and properties for the
plugins that have been applied to the project.
In the example, the application plugin added an application property, which is used to detail the main class of our Java application:
<< 'build.gradle.kts'
application {
    mainClass = "com.example.Main"
}
build.gradle.kts

5. Register and configure tasks
Tasks perform some basic piece of work, such as compiling classes, or running unit tests, or zipping up a WAR file.
~While tasks are typically defined in plugins, you may need to register or configure tasks in build scripts.
Registering a task adds the task to your project.
You can register tasks in a project using the TaskContainer.register~java.lang.String~ method:
<< 'build.gradle.kts'
tasks.register<Zip>("zip-reports") {
    from 'Reports/'
    include '*'
    archiveName 'Reports.zip'
    destinationDir(file('/dir'))
}
build.gradle.kts
You may have seen usage of the TaskContainer.create~java.lang.String~ method which should be avoided:
<< 'build.gradle.kts'
tasks.create<Zip>("zip-reports") {
    from 'Reports/'
    include '*'
    archiveName 'Reports.zip'
    destinationDir(file('/dir'))
}
#register(), which enables task configuration avoidance, is preferred over create().
build.gradle.kts
You can locate a task to configure it using the TaskCollection.named~java.lang.String~ method:
<< 'build.gradle.kts'
tasks.named<Test>("test") {
    useJUnitPlatform()
}
build.gradle.kts
The example below configures the Javadoc task to automatically generate HTML documentation from Java code:
<< 'build.gradle.kts'
tasks.named("javadoc").configure {
    exclude 'app/Internal*.java'
    exclude 'app/internal/*'
    exclude 'app/internal/*'
}
build.gradle.kts

A build script is made up of zero or more statements and script blocks:
<< 'build.gradle.kts'
println(project.layout.projectDirectory);
#Statements can include method calls, property assignments, and local variable definitions:
version = '1.0.0.GA'
#A script block is a method call which takes a closure/lambda as a parameter:
configurations {
}
#The closure/lambda configures some delegate object as it executes:
repositories {
    google()
}
build.gradle.kts

A build script is also a Groovy or a Kotlin script:
<< 'build.gradle.kts'
tasks.register("upper") {
    doLast {
        val someString = "mY_nAmE"
        println("Original: $someString")
        println("Upper case: ${someString.toUpperCase()}")
    }
}
build.gradle.kts
$ gradle -q upper

It can contain elements allowed in a Groovy or Kotlin script, such as method definitions and class definitions:
<< 'build.gradle.kts'
tasks.register("count") {
    doLast {
        repeat(4) { print("$it ") }
    }
}
build.gradle.kts
$ gradle -q count

Using the capabilities of the Groovy or Kotlin language, you can register multiple tasks in a loop:
<< 'build.gradle.kts'
repeat(4) { counter ->
    tasks.register("task$counter") {
        doLast {
            println("I'm task number $counter")
        }
    }
}
build.gradle.kts
$ gradle -q task1

Build scripts can declare two variables: local variables and extra properties.

Local Variables
Declare local variables with the val keyword. Local variables are only visible in the scope where they have been
 declared. They are a feature of the underlying Kotlin language.
<< 'build.gradle.kts'
val dest = "dest"

tasks.register<Copy>("copy") {
    from("source")
    into(dest)
}
build.gradle.kts

Extra Properties
Gradle’s enhanced objects, including projects, tasks, and source sets, can hold user-defined properties.
Add, read, and set extra properties via the owning object’s extra property. Alternatively, you can access extra
 properties via Kotlin delegated properties using by extra.
<< 'build.gradle.kts'
plugins {
    id("java-library")
}

val springVersion by extra("3.1.0.RELEASE")
val emailNotification by extra { "build@master.org" }

sourceSets.all { extra["purpose"] = null }

sourceSets {
    main {
        extra["purpose"] = "production"
    }
    test {
        extra["purpose"] = "test"
    }
    create("plugin") {
        extra["purpose"] = "production"
    }
}

tasks.register("printProperties") {
    val springVersion = springVersion
    val emailNotification = emailNotification
    val productionSourceSets = provider {
        sourceSets.matching { it.extra["purpose"] == "production" }.map { it.name }
    }
    doLast {
        println(springVersion)
        println(emailNotification)
        productionSourceSets.get().forEach { println(it) }
    }
}
build.gradle.kts
$ gradle -q printProperties

This example adds two extra properties to the project object via by extra. Additionally, this example adds a
 property named purpose to each source set by setting extra["purpose"] to null. Once added, you can read and set these properties via extra.
Gradle requires special syntax for adding a property so that it can fail fast. For example, this allows Gradle to
 recognize when a script attempts to set a property that does not exist. You can access extra properties anywhere where
  you can access their owning object. This gives extra properties a wider scope than local variables. Subprojects can
   access extra properties on their parent projects.
~For more information about extra properties, see ExtraPropertiesExtension in the API documentation.
https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtraPropertiesExtension.html

Configure Arbitrary Objects
<< 'build.gradle.kts'
#The example greet() task shows an example of arbitrary object configuration:
class UserInfo(
    var name: String? = null,
    var email: String? = null
)

tasks.register("greet") {
    val user = UserInfo().apply {
        name = "Isaac Newton"
        email = "isaac@newton.me"
    }
    doLast {
        println(user.name)
        println(user.email)
    }
}
build.gradle.kts
$ gradle -q greet

Closure Delegates
Each closure has a delegate object. Groovy uses this delegate to look up variable and method references to
nonlocal variables and closure parameters. Gradle uses this for configuration closures, where the delegate object refers to
 the object being configured.
<< 'build.gradle.kts'
dependencies {
    assert delegate == project.dependencies
    testImplementation('junit:junit:4.13')
    delegate.testImplementation('junit:junit:4.13')
}
build.gradle.kts

Default imports
To make build scripts more concise, Gradle automatically adds a set of import statements to scripts.
As a result, instead of writing
<< 'build.gradle.kts'
throw new org.gradle.api.tasks.StopExecutionException()
#, you can write
throw new StopExecutionException()
#instead.
build.gradle.kts

https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#tutorial_using_tasks
The work that Gradle can do on a project is defined by one or more tasks.
A task represents some independent unit of work that a build performs. This might be compiling some classes, creating a
 JAR, generating Javadoc, or publishing some archives to a repository.
When a user runs ./gradlew build in the command line, Gradle will execute the build task along with any other tasks it depends on.
Gradle provides several default tasks for a project, which are listed by running
./gradlew tasks

Tasks either come from build scripts or plugins.
Once we apply a plugin to our project, such as the application plugin, additional tasks become available:
<< 'build.gradle.kts'
plugins {
    id("application")
}
build.gradle.kts

Task classification - There are two classes of tasks that can be executed:

Actionable tasks have some actions attached to do work in your build: compileJava.
Lifecycle tasks are tasks with no actions attached: assemble, build.

Typically, a lifecycle tasks depends on many actionable tasks, and is used to execute many tasks at once.

Task registration and action
Let’s take a look at a simple "Hello World" task in a build script:
<< 'build.gradle.kts'
tasks.register("hello") {
    doLast {
        println("Hello world!")
    }
}
build.gradle.kts
In the example, the build script registers a single task called hello using the TaskContainer API, and adds an action to it.
https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html
#If the tasks in the project are listed, the hello task is available to Gradle:
$ ./gradlew app:tasks --all
You can execute the task in the build script with ./gradlew hello:
$ ./gradlew hello

Task group and description
The hello task from the previous section can be detailed with a description and assigned to a group with the following update:
<< 'build.gradle.kts'
tasks.register("hello") {
    group = "Custom"
    description = "A lovely greeting task."
    doLast {
        println("Hello world!")
    }
}
build.gradle.kts
To view information about a task, use the help --task <task-name> command:
$./gradlew help --task hello

Task dependencies
You can declare tasks that depend on other tasks:
<< 'build.gradle.kts'
tasks.register("hello") {
    doLast {
        println("Hello world!")
    }
    dependsOn(tasks.assemble)
}
tasks.register("intro") {
    dependsOn("hello")
    doLast {
        println("I'm Gradle")
    }
}
build.gradle.kts
$ gradle -q intro

The dependency of taskX to taskY may be declared before taskY is defined:
<< 'build.gradle.kts'
tasks.register("taskX") {
    dependsOn("taskY")
    doLast {
        println("taskX")
    }
}
tasks.register("taskY") {
    doLast {
        println("taskY")
    }
}
build.gradle.kts
$ gradle -q taskX

Task configuration
Once registered, tasks can be accessed via the TaskProvider API for further configuration.
https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskProvider.html
<< 'build.gradle.kts'
#For instance, you can use this to add dependencies to a task at runtime dynamically:
repeat(4) { counter ->
    tasks.register("task$counter") {
        doLast {
            println("I'm task number $counter")
        }
    }
}
tasks.named("task0") { dependsOn("task2", "task3") }
build.gradle.kts
$ gradle -q task0

You can add behavior to an existing task:
<< 'build.gradle.kts'
tasks.register("hello") {
    doLast {
        println("Hello Earth")
    }
}
tasks.named("hello") {
    doFirst {
        println("Hello Venus")
    }
}
tasks.named("hello") {
    doLast {
        println("Hello Mars")
    }
}
tasks.named("hello") {
    doLast {
        println("Hello Jupiter")
    }
}
#The calls doFirst and doLast can be executed multiple times. They add an action to the beginning or the end of the
# task’s actions list. When the task executes, the actions in the action list are executed in order.
build.gradle.kts
$ gradle -q hello

Here is an example of the named method being used to configure a task added by a plugin:
<< 'build.gradle.kts'
tasks.named("dokkaHtml") {
    outputDirectory.set(buildDir.resolve("dokka"))
}
build.gradle.kts

Task types - Gradle tasks are a subclass of Task.
In the build script, the HelloTask class is created by extending DefaultTask:
<< 'build.gradle.kts'
// Extend the DefaultTask class to create a HelloTask class
abstract class HelloTask : DefaultTask() {
    @TaskAction
    fun hello() {
        println("hello from HelloTask")
    }
}

// Register the hello Task with type HelloTask
tasks.register<HelloTask>("hello") {
    group = "Custom tasks"
    description = "A lovely greeting task."
}
#The hello task is registered with the type HelloTask. Executing our new hello task:
build.gradle.kts
$ ./gradlew hello
Now the hello task is of type HelloTask instead of type Task. The Gradle help task reveals the change:
$ ./gradlew help --task hello

Built-in task types
Gradle provides many built-in task types with common and popular functionality, such as copying or deleting files.
This example task copies *.war files from the source directory to the target directory using the Copy built-in task:
<< 'build.gradle.kts'
tasks.register("copyTask",Copy) {
    from("source")
    into("target")
    include("*.war")
}
build.gradle.kts
There are many task types developers can take advantage of, including GroovyDoc, Zip, Jar, JacocoReport, Sign, or
 Delete, which are available in the DSL.
link:../dsl/org.gradle.api.plugins.antlr.AntlrTask.html

https://docs.gradle.org/current/userguide/writing_tasks.html#writing_tasks
Gradle tasks are created by extending DefaultTask.
However, the generic DefaultTask provides no action for Gradle. If users want to extend the capabilities of Gradle and
 their build script, they must either use a built-in task or create a custom task:

Built-in task - Gradle provides built-in utility tasks such as Copy, Jar, Zip, Delete, etc.,
Custom task - Gradle allows users to subclass DefaultTask to create their own task types.

Create a task
The simplest and quickest way to create a custom task is in a build script:
To create a task, inherit from the DefaultTask class and implement a @TaskAction handler:
<< 'build.gradle.kts'
abstract class CreateFileTask : DefaultTask() {
    @TaskAction
    fun action() {
        val file = File("myfile.txt")
        file.createNewFile()
        file.writeText("HELLO FROM MY TASK")
    }
}
build.gradle.kts
The CreateFileTask implements a simple set of actions. First, a file called "myfile.txt" is created in the
 main project. Then, some text is written to the file.

Register a task
<< 'build.gradle.kts'
#A task is registered in the build script using the TaskContainer.register() method, which allows it
# to be then used in the build logic.
abstract class CreateFileTask : DefaultTask() {
    @TaskAction
    fun action() {
        val file = File("myfile.txt")
        file.createNewFile()
        file.writeText("HELLO FROM MY TASK")
    }
}
tasks.register<CreateFileTask>("createFileTask")
build.gradle.kts

Task group and description
Setting the group and description properties on your tasks can help users understand how to use your task:
<< 'build.gradle.kts'
abstract class CreateFileTask : DefaultTask() {
    @TaskAction
    fun action() {
        val file = File("myfile.txt")
        file.createNewFile()
        file.writeText("HELLO FROM MY TASK")
    }
}
tasks.register<CreateFileTask>("createFileTask", ) {
    group = "custom"
    description = "Create myfile.txt in the current directory"
}
#Once a task is added to a group, it is visible when listing tasks.
build.gradle.kts

Task input and outputs
~For the task to do useful work, it typically needs some inputs. A task typically produces outputs.
<< 'build.gradle.kts'
abstract class CreateFileTask : DefaultTask() {
    @Input
    val fileText = "HELLO FROM MY TASK"

    @Input
    val fileName = "myfile.txt"

    @OutputFile
    val myFile: File = File(fileName)

    @TaskAction
    fun action() {
        myFile.createNewFile()
        myFile.writeText(fileText)
    }
}

tasks.register<CreateFileTask>("createFileTask") {
    group = "custom"
    description = "Create myfile.txt in the current directory"
}
build.gradle.kts

Configure a task
The CreateFileTask class is updated so that the text in the file is configurable:
<< 'build.gradle.kts'
abstract class CreateFileTask : DefaultTask() {
    @get:Input
    abstract val fileText: Property<String>

    @Input
    val fileName = "myfile.txt"

    @OutputFile
    val myFile: File = File(fileName)

    @TaskAction
    fun action() {
        myFile.createNewFile()
        myFile.writeText(fileText.get())
    }
}

tasks.register<CreateFileTask>("createFileTask") {
    group = "custom"
    description = "Create myfile.txt in the current directory"
    fileText.convention("HELLO FROM THE CREATE FILE TASK METHOD") // Set convention
}

#A task is optionally configured in a build script using the TaskCollection.named() method.
tasks.named<CreateFileTask>("createFileTask") {
    fileText.set("HELLO FROM THE NAMED METHOD") // Override with custom message
}
#In the named() method, we find the createFileTask task and set the text that will be written to the file.
build.gradle.kts
When the task is executed:
$ ./gradlew createFileTask
A text file called myfile.txt is created in the project root folder:
myfile.txt
HELLO FROM THE NAMED METHOD
https://docs.gradle.org/current/userguide/more_about_tasks.html#more_about_tasks

https://docs.gradle.org/current/userguide/plugins.html#using_plugins
Much of Gradle’s functionality is delivered via plugins, including core plugins distributed with Gradle,
 third-party plugins, and script plugins defined within builds.
Plugins introduce new tasks ~e.g., JavaCompile~, domain objects ~e.g., SourceSet~, conventions
 ~e.g., locating Java source at src/main/java~, and extend core or other plugin objects.
Plugins in Gradle are essential for automating common build tasks, integrating with external tools or services, and
tailoring the build process to meet specific project needs. They also serve as the primary mechanism for organizing build logic.

Benefits of plugins
Writing many tasks and duplicating configuration blocks in build scripts can get messy. Plugins offer several
 advantages over adding logic directly to the build script:
Promotes Reusability: Reduces the need to duplicate similar logic across projects.
Enhances Modularity: Allows for a more modular and organized build script.
Encapsulates Logic: Keeps imperative logic separate, enabling more declarative build scripts.

Plugin distribution
You can leverage plugins from Gradle and the Gradle community or create your own.
Plugins are available in three ways:
Core plugins - Gradle develops and maintains a set of Core Plugins.
https://docs.gradle.org/current/userguide/plugin_reference.html#plugin_reference
Community plugins - Gradle plugins shared in a remote repository such as Maven or the Gradle Plugin Portal.
#https://plugins.gradle.org/
Local plugins - Gradle enables users to create custom plugins using APIs.
https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html

Types of plugins
Plugins can be implemented as binary plugins, precompiled script plugins, or script plugins:
#Binary Plugins
Binary plugins are compiled plugins typically written in Java or Kotlin DSL that are packaged as JAR files. They are
 applied to a project using the plugins {} block. They offer better performance and maintainability compared to
  script plugins or precompiled script plugins.
#Precompiled Script Plugins
Precompiled script plugins are Groovy DSL or Kotlin DSL scripts compiled and distributed as Java class files packaged in a
 library. They are applied to a project using the plugins {} block. They provide a way to reuse complex logic across
  projects and allow for better organization of build logic.
#Script Plugins
Script plugins are Groovy DSL or Kotlin DSL scripts that are applied directly to a Gradle build script using the
apply from: syntax. They are applied inline within a build script to add functionality or
 customize the build process. They are simple to use.
A plugin often starts as a script plugin ~because they are easy to write~. Then, as the code becomes more valuable,
 it’s migrated to a binary plugin that can be easily tested and shared between multiple projects or organizations.

Using plugins
To use the build logic encapsulated in a plugin, Gradle needs to perform two steps. First, it needs to resolve the
 plugin, and then it needs to apply the plugin to the target, usually a Project.
Resolving a plugin means finding the correct version of the JAR that contains a given plugin and adding it to the
script classpath. Once a plugin is resolved, its API can be used in a build script. Script plugins are self-resolving in
 that they are resolved from the specific file path or URL provided when applying them. Core binary plugins provided as
  part of the Gradle distribution are automatically resolved.
Applying a plugin means executing the plugin’s Plugin.apply~T~ on a project.
https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html#apply-T-
The plugins DSL is recommended to resolve and apply plugins in one step.
https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block

Resolving plugins
Gradle provides the core plugins ~e.g., JavaPlugin, GroovyPlugin, MavenPublishPlugin, etc.~ as part of its
 distribution, which means they are automatically resolved.
Core plugins are applied in a build script using the plugin name:
<< 'build.gradle.kts'
plugins {
    id «plugin name»
}
build.gradle.kts
<< 'build.gradle.kts'
plugins {
    id("java")
}
build.gradle.kts
Non-core plugins must be resolved before they can be applied. Non-core plugins are identified by a unique ID and a version in the build file:
<< 'build.gradle.kts'
plugins {
    id «plugin id» version «plugin version»
}
build.gradle.kts
And the location of the plugin must be specified in the settings file:
<< 'settings.gradle.kts'
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url 'https://maven.example.com/plugins'
        }
    }
}
settings.gradle.kts
There are additional considerations for resolving and applying plugins:

#	To	Use	For example:
1 Apply a core, community or local plugin to a specific project.
The plugins block in the build file
https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block
<< 'build.gradle.kts'
plugins {
  id("org.barfuin.gradle.taskinfo") version "2.1.0"
}
build.gradle.kts

2 Apply common core, community or local plugin to multiple subprojects.
A build script in the buildSrc directory
https://docs.gradle.org/current/userguide/plugins.html#sec:buildsrc_plugins_dsl
<< 'build.gradle.kts'
plugins {
    id("org.barfuin.gradle.taskinfo") version "2.1.0"
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(Libs.Kotlin.coroutines)
}
build.gradle.kts

3 Apply a core, community or local plugin needed for the build script itself.
The buildscript block in the build file
https://docs.gradle.org/current/userguide/plugins.html#sec:applying_plugins_buildscript
<< 'build.gradle.kts'
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("org.barfuin.gradle.taskinfo:gradle-taskinfo:2.1.0")
  }
}
plugins {
  id("org.barfuin.gradle.taskinfo") version "2.1.0"
}
build.gradle.kts

4 Apply a local script plugins.
https://docs.gradle.org/current/userguide/plugins.html#sec:script_plugins
<< 'build.gradle.kts'
#The legacy apply() method in the build file
apply(plugin = "org.barfuin.gradle.taskinfo")
apply<MyPlugin>()
build.gradle.kts

1. Applying plugins using the plugins{} block
https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block
The plugin DSL provides a concise and convenient way to declare plugin dependencies.
The plugins block configures an instance of PluginDependenciesSpec:
https://docs.gradle.org/current/javadoc/org/gradle/plugin/use/PluginDependenciesSpec.html
<< 'build.gradle.kts'
plugins {
    application                                     // by name
    java                                            // by name
    id("java")                                      // by id - recommended
    id("org.jetbrains.kotlin.jvm") version "2.0.20"  // by id - recommended
}
build.gradle.kts
Core Gradle plugins are unique in that they provide short names, such as java for the core JavaPlugin.
https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/JavaPlugin.html
To apply a core plugin, the short name can be used:
<< 'build.gradle.kts'
plugins {
    java                                            // by name
}
build.gradle.kts
All other binary plugins must use the fully qualified form of the plugin id ~e.g., com.github.foo.bar~.
To apply a community plugin from Gradle plugin portal, the fully qualified plugin id, a globally unique identifier, must be used:
#http://plugins.gradle.org/
<< 'build.gradle.kts'
plugins {
    id("org.springframework.boot") version "3.3.1"
}
build.gradle.kts
See PluginDependenciesSpec for more information on using the Plugin DSL.
https://docs.gradle.org/current/javadoc/org/gradle/plugin/use/PluginDependenciesSpec.html

The plugins {} block does not support arbitrary code.
It is constrained to be idempotent ~produce the same result every time~ and side effect-free ~safe for
 Gradle to execute at any time~.
<< 'build.gradle.kts'
plugins {
#for core Gradle plugins or plugins already available to the build script
    id(«plugin id»)
#for binary Gradle plugins that need to be resolved
    id(«plugin id») version «plugin version»
}
#Where «plugin id» and «plugin version» are a string.
build.gradle.kts
The plugins{} block must also be a top-level statement in the build script. It cannot be nested inside another
construct ~e.g., an if-statement or for-loop~.
Only in build scripts and settings file
The plugins{} block can only be used in a project’s build script ~build.gradle.kts and the ~settings.gradle.kts file.
 It must appear before any other block. It cannot be used in script plugins or init scripts.

Applying plugins to all subprojects
Suppose you have a multi-project build, you probably want to apply plugins to some or all of the subprojects in
your build but not to the root project.
~While the default behavior of the plugins{} block is to immediately resolve and apply the plugins, you can use the
 apply false syntax to tell Gradle not to apply the plugin to the current project. Then, use the plugins{} block without the
  version in subprojects build scripts:
<< 'settings.gradle.kts'
include("hello-a")
include("hello-b")
include("goodbye-c")
settings.gradle.kts
<< 'build.gradle.kts'
plugins {
    id("com.example.hello") version "1.0.0" apply false
    id("com.example.goodbye") version "1.0.0" apply false
}
build.gradle.kts
<< 'build.gradle.kts'
hello-a/build.gradle.kts
plugins {
    id("com.example.hello")
}
build.gradle.kts
<< 'build.gradle.kts'
hello-b/build.gradle.kts
plugins {
    id("com.example.hello")
}
build.gradle.kts
<< 'build.gradle.kts'
goodbye-c/build.gradle.kts
plugins {
    id("com.example.goodbye")
}
build.gradle.kts
You can also encapsulate the versions of external plugins by composing the build logic using your own convention plugins.
https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:convention_plugins

2. Applying plugins from the buildSrc directory
https://docs.gradle.org/current/userguide/plugins.html#sec:buildsrc_plugins_dsl
buildSrc is an optional directory at the Gradle project root that contains build logic ~i.e., plugins~ used in
 building the main project. You can apply plugins that reside in a project’s buildSrc directory as long as they have a defined ID.
The following example shows how to tie the plugin implementation class my.MyPlugin, defined in buildSrc, to the id "my-plugin":
<< 'build.gradle.kts'
plugins {
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("myPlugins") {
            id = "my-plugin"
            implementationClass = "my.MyPlugin"
        }
    }
}
build.gradle.kts
The plugin can then be applied by ID:
<< 'build.gradle.kts'
plugins {
    id("my-plugin")
}
build.gradle.kts

3. Applying plugins using the buildscript{} block
https://docs.gradle.org/current/userguide/plugins.html#sec:applying_plugins_buildscript
The buildscript block is used for:
global dependencies and repositories required for building the project ~applied in the subprojects~.
declaring which plugins are available for use in the build script ~in the ~build.gradle.kts file itself~.
So when you want to use a library in the build script itself, you must add this library on the script classpath using buildScript:
<< 'build.gradle.kts'
import org.apache.commons.codec.binary.Base64

buildscript {
    repositories {  // this is where the plugins are located
        mavenCentral()
        google()
    }
    dependencies { // these are the plugins that can be used in subprojects or in the build file itself
        classpath group: 'commons-codec', name: 'commons-codec', version: '1.2' // used in the task below
        classpath 'com.android.tools.build:gradle:4.1.0' // used in subproject
    }
}

tasks.register('encode') {
    doLast {
        def byte[] encodedString = new Base64().encode('hello world\n'.getBytes())
        println new String(encodedString)
    }
}
build.gradle.kts
And you can apply the globally declared dependencies in the subproject that needs it:
<< 'build.gradle.kts'
plugins {
    id 'com.android.application'
}
build.gradle.kts

Binary plugins published as external jar files can be added to a project by adding the plugin to the build script classpath and then applying the plugin.
External jars can be added to the build script classpath using the buildscript{} block as described in External dependencies for the build script:
<< 'build.gradle.kts'
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:3.3.1")
    }
}

apply(plugin = "org.springframework.boot")
build.gradle.kts

4. Applying script plugins using the legacy apply~~ method
https://docs.gradle.org/current/userguide/plugins.html#sec:script_plugins
A script plugin is an ad-hoc plugin, typically written and applied in the same build script. It is applied using the legacy application method:
https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application
<< 'build.gradle.kts'
class MyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("Plugin ${this.javaClass.simpleName} applied on ${project.name}")
    }
}

apply<MyPlugin>()
build.gradle.kts
Lets take a rudimentary example of a plugin written in a file called other.gradle located in the same directory as the build.gradle file:
<< 'other.gradle.kts'
public class Other implements Plugin<Project> {
    @Override
    void apply(Project project) {
        // Does something
    }
}
other.gradle.kts
First, import the external file using:
<< 'build.gradle.kts'
apply from: 'other.gradle'
#Then you can apply it:
apply plugin: Other
build.gradle.kts
Script plugins are automatically resolved and can be applied from a script on the local filesystem or remotely:
<< 'build.gradle.kts'
apply(from = "other.gradle.kts")
build.gradle.kts
Filesystem locations are relative to the project directory, while remote script locations are specified with an HTTP URL.
 Multiple script plugins ~of either form~ can be applied to a given target.

The pluginManagement{} block is used to configure repositories for plugin resolution and to define version constraints for
 plugins that are applied in the build scripts.
The pluginManagement{} block can be used in a ~settings.gradle.kts file, where it must be the first block in the file:
<< 'settings.gradle.kts'
pluginManagement {
    plugins {
    }
    resolutionStrategy {
    }
    repositories {
    }
}
rootProject.name = "plugin-management"
settings.gradle.kts
The block can also be used in Initialization Script:
https://docs.gradle.org/current/userguide/init_scripts.html#init_scripts
<< 'init.gradle.kts'
settingsEvaluated {
    pluginManagement {
        plugins {
        }
        resolutionStrategy {
        }
        repositories {
        }
    }
}
init.gradle.kts

Custom Plugin Repositories
By default, the plugins{} DSL resolves plugins from the public Gradle Plugin Portal.
#https://plugins.gradle.org/
Many build authors would also like to resolve plugins from private Maven or Ivy repositories because they contain
proprietary implementation details or to have more control over what plugins are available to their builds.
To specify custom plugin repositories, use the repositories{} block inside pluginManagement{}:
<< 'settings.gradle.kts'
pluginManagement {
    repositories {
        maven(url = "./maven-repo")
        gradlePluginPortal()
        ivy(url = "./ivy-repo")
    }
}
settings.gradle.kts
This tells Gradle to first look in the Maven repository at ../maven-repo when resolving plugins and then to check the
 Gradle Plugin Portal if the plugins are not found in the Maven repository. If you don’t want the
 Gradle Plugin Portal to be searched, omit the gradlePluginPortal~~ line. Finally, the
  Ivy repository at ../ivy-repo will be checked.

Plugin Version Management
A plugins{} block inside pluginManagement{} allows all plugin versions for the build to be defined in a single location.
 Plugins can then be applied by id to any build script via the plugins{} block.
One benefit of setting plugin versions this way is that the pluginManagement.plugins{} does not have the
 same constrained syntax as the build script plugins{} block. This allows plugin versions to be taken from
  gradle.properties, or loaded via another mechanism.
Managing plugin versions via pluginManagement:
<< 'settings.gradle.kts'
pluginManagement {
  val helloPluginVersion: String by settings
  plugins {
    id("com.example.hello") version "${helloPluginVersion}"
  }
}
settings.gradle.kts
<< 'build.gradle.kts'
plugins {
    id("com.example.hello")
}
build.gradle.kts
<< 'gradle.properties'
helloPluginVersion=1.0.0
gradle.properties
The plugin version is loaded from gradle.properties and configured in the settings script, allowing the plugin to be
 added to any project without specifying the version.

Plugin Resolution Rules
Plugin resolution rules allow you to modify plugin requests made in plugins{} blocks, e.g., changing the
 requested version or explicitly specifying the implementation artifact coordinates.
To add resolution rules, use the resolutionStrategy{} inside the pluginManagement{} block:
<< 'settings.gradle.kts'
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.example") {
                useModule("com.example:sample-plugins:1.0.0")
            }
        }
    }
    repositories {
        maven {
            url = uri("./maven-repo")
        }
        gradlePluginPortal()
        ivy {
            url = uri("./ivy-repo")
        }
    }
}
settings.gradle.kts
This tells Gradle to use the specified plugin implementation artifact instead of its built-in default mapping from
 plugin ID to Maven/Ivy coordinates.
Custom Maven and Ivy plugin repositories must contain plugin marker artifacts and the artifacts that implement the
 plugin. Read Gradle Plugin Development Plugin for more information on publishing plugins to custom repositories.
https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin
See PluginManagementSpec for complete documentation for using the pluginManagement{} block.
https://docs.gradle.org/current/javadoc/org/gradle/plugin/management/PluginManagementSpec.html

Plugin Marker Artifacts
Since the plugins{} DSL block only allows for declaring plugins by their globally unique plugin id and version properties,
 Gradle needs a way to look up the coordinates of the plugin implementation artifact.
To do so, Gradle will look for a Plugin Marker Artifact with the coordinates plugin.id:plugin.id.gradle.plugin:plugin.version.
 This marker needs to have a dependency on the actual plugin implementation. Publishing these markers is automated by the java-gradle-plugin.
https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin
~For example, the following complete sample from the sample-plugins project shows how to publish a com.example.hello plugin and
 a com.example.goodbye plugin to both an Ivy and Maven repository using the combination of the java-gradle-plugin, the
  maven-publish plugin, and the ivy-publish plugin.
https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven
https://docs.gradle.org/current/userguide/publishing_ivy.html#publishing_ivy
<< 'build.gradle.kts'
plugins {
    `java-gradle-plugin`
    `maven-publish`
    `ivy-publish`
}

group = "com.example"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("hello") {
            id = "com.example.hello"
            implementationClass = "com.example.hello.HelloPlugin"
        }
        create("goodbye") {
            id = "com.example.goodbye"
            implementationClass = "com.example.goodbye.GoodbyePlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("maven-repo"))
        }
        ivy {
            url = uri(layout.buildDirectory.dir("ivy-repo"))
        }
    }
}
build.gradle.kts

Using a Version Catalog
When a project uses a version catalog, plugins can be referenced via aliases when applied.
Let’s take a look at a simple Version Catalog:
<< 'libs.versions.toml'
gradle/libs.versions.toml
[versions]
kotlin = "2.0.20"

[plugins]
kotlin-jvm = {id="org.jetbrains.kotlin.jvm", version.ref="kotlin"}
libs.versions.toml
<< 'build.gradle.kts'
plugins {
    alias(libs.plugins.kotlin.jvm)
}
#kotlin-jvm is available as the Gradle generated safe accessor: kotlin.jvm
build.gradle.kts

https://docs.gradle.org/current/userguide/writing_plugins.html#writing_plugins
Custom plugin
A plugin is any class that implements the Plugin interface.
https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html
To create a "hello world" plugin:
	Extend the org.gradle.api.Plugin interface.
  Override the apply method.
<< 'build.gradle.kts'
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SamplePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("SampleTask") {
            println("Hello world!")
        }
    }
}
build.gradle.kts
Note that this is a simple hello-world example and does not reflect best practices.
Script plugins are not recommended. Plugin code should not be in your ~build.gradle.kts file.
Plugins should always be written as pre-compiled script plugins, convention plugins or binary plugins.

Pre-compiled script plugin
Pre-compiled script plugins offer an easy way to rapidly prototype and experiment. They let you package build logic as
 *.gradle.kts script files using the Groovy or Kotlin DSL. These scripts reside in specific directories,
 such as src/main/groovy or src/main/kotlin.
To apply one, simply use its ID derived from the script filename ~without .gradle~. You can think of the file itself as
 the plugin, so you do not need to subclass the Plugin interface in a precompiled script.
Lets take a look at an example with the following structure:

└── buildSrc
    ├── build.gradle.kts
    └── src
       └── main
          └── kotlin
             └── my-create-file-plugin.gradle.kts

Our my-create-file-plugin.gradle.kts file contains the following code:

<< 'my-create-file-plugin.gradle.kts'
buildSrc/src/main/kotlin/my-create-file-plugin.gradle.kts
abstract class CreateFileTask : DefaultTask() {
    @get:Input
    abstract val fileText: Property<String>

    @Input
    val fileName = "myfile.txt"

    @OutputFile
    val myFile: File = File(fileName)

    @TaskAction
    fun action() {
        myFile.createNewFile()
        myFile.writeText(fileText.get())
    }
}

tasks.register("createFileTask", CreateFileTask::class) {
    group = "from my plugin"
    description = "Create myfile.txt in the current directory"
    fileText.set("HELLO FROM MY PLUGIN")
}
my-create-file-plugin.gradle.kts
<< 'build.gradle.kts'
buildSrc/build.gradle.kts
plugins {
    `kotlin-dsl`
}
build.gradle.kts
The pre-compiled script can now be applied in the ~build.gradle.kts file of any subproject:
<< 'build.gradle.kts'
plugins {
    id("my-create-file-plugin")  // Apply the plugin
}
build.gradle.kts
The createFileTask task from the plugin is now available in your subproject.

Convention Plugins
Convention plugins are a way to encapsulate and reuse common build logic in Gradle. They allow you to define a set of
 conventions for a project, and then apply those conventions to other projects or modules.
The example above has been re-written as a convention plugin as a Kotlin script called MyConventionPlugin.kt and stored in buildSrc:
<< 'buildSrc/src/main/kotlin/MyConventionPlugin.kt'
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CreateFileTask : DefaultTask() {
    @get:Input
    abstract val fileText: Property<String>

    @Input
    val fileName = project.rootDir.toString() + "/myfile.txt"

    @OutputFile
    val myFile: File = File(fileName)

    @TaskAction
    fun action() {
        myFile.createNewFile()
        myFile.writeText(fileText.get())
    }
}

class MyConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("createFileTask", CreateFileTask::class.java) {
            group = "from my plugin"
            description = "Create myfile.txt in the current directory"
            fileText.set("HELLO FROM MY PLUGIN")
        }
    }
}
buildSrc/src/main/kotlin/MyConventionPlugin.kt
The plugin can be given an id using a gradlePlugin{} block so that it can be referenced in the root:
<< 'buildSrc/build.gradle.kts'
gradlePlugin {
    plugins {
        create("my-convention-plugin") {
            id = "my-convention-plugin"
            implementationClass = "MyConventionPlugin"
        }
    }
}
buildSrc/build.gradle.kts
The gradlePlugin{} block defines the plugins being built by the project. With the newly created id, the plugin can be
 applied in other build scripts accordingly:
<< 'build.gradle.kts'
plugins {
    application
    id("my-convention-plugin") // Apply the plugin
}
build.gradle.kts

Binary Plugins
A binary plugin is a plugin that is implemented in a compiled language and is packaged as a JAR file. It is resolved as
 a dependency rather than compiled from source.
~For most use cases, convention plugins must be updated infrequently. Having each developer execute the plugin build as
 part of their development process is wasteful, and we can instead distribute them as binary dependencies.
There are two ways to update the convention plugin in the example above into a binary plugin.
Use composite builds:
<< 'settings.gradle.kts'
includeBuild("my-plugin")
settings.gradle.kts
Publish the plugin to a repository:
<< 'build.gradle.kts'
plugins {
    id("com.gradle.plugin.myconventionplugin") version "1.0.0"
}
build.gradle.kts
Consult the Developing Plugins chapter to learn more.
https://docs.gradle.org/current/userguide/custom_plugins.html#custom_plugins

https://docs.gradle.org/current/userguide/partr1_gradle_init.html
$ mkdir authoring-tutorial
$ cd authoring-tutorial
Run gradle init with parameters to generate a Java application:
$ gradle init --type java-application  --dsl kotlin
~Select defaults for any additional prompts.

Step 2. Understanding the Directory layout
The project root directory contains all source files from your project.
When you are done with Gradle init, the directory should look as follows:

.
├── gradle
    ├── libs.version.toml
│   └── wrapper
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── app
    ├── build.gradle.kts
    └── src
        ├── main
        │   └── java
        │       └── demo
        │           └── App.java
        └── test
            └── java
                └── demo
                    └── AppTest.java

Generated folder for wrapper files
Version catalog for dependencies
Gradle wrapper start scripts
Settings file to define build name and subprojects
Build script for app subproject
Default Java source folder for app subproject
Default Java test source folder for app subproject
The authoring-tutorial folder is the root project directory. Inside the root project directory are one or more
 subprojects, build scripts, and the Gradle wrapper.

~While the Gradle Wrapper is local to the root project, the Gradle executable is found in the GRADLE_USER_HOME.
The GRADLE_USER_HOME, which defaults to USER_HOME/.gradle, is also where Gradle stores its
global configuration properties, initialization scripts, caches, log files and more.

Step 3. Review the Gradle Files
The ~settings.gradle.kts file has two interesting lines:
<< 'settings.gradle.kts'
rootProject.name = "authoring-tutorial"
include("app")
#rootProject.name assigns a name to the build, overriding the default behavior of naming the build after its directory name.
#include("app") defines that the build consists of one subproject called app that contains its own source code and build logic.
#More subprojects can be added by additional include() statements.
settings.gradle.kts

Our build contains one subproject called app representing the Java application we are building. It is configured in the
 app/~build.gradle.kts file:

<< 'build.gradle.kts'
plugins {
#Apply the application plugin to add support for building a CLI application in Java.
    id("application")
}

repositories {
#Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
#Use JUnit Jupiter for testing (using the version catalog).
#This dependency is used by the application (referred using the version catalog).
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.guava)
}

java {
    toolchain {
#Define the toolchain version.
        languageVersion = JavaLanguageVersion.of(11)
    }
}

application {
#Define the main class for the application.
    mainClass = "org.example.App"
}

tasks.named<Test>("test") {
#Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
#The build script in the app subproject directory declares the dependencies the app code will need to be assembled and tested.
build.gradle.kts

$ ./gradlew run
$ ./gradlew build
$ ./gradlew build --scan

https://docs.gradle.org/current/userguide/partr2_build_lifecycle.html#partr2_build_lifecycle
Step 1. Understanding the Build Lifecycle
A Gradle build has three distinct phases:
Phase 1 - Initialization
During the initialization phase, Gradle determines which projects will take part in the build, and
 creates a Project instance for each project.
Phase 2 - Configuration
During the configuration phase, the Project objects are configured using the build scripts of all projects in the build.
 Gradle determines the set of tasks to be executed.
Phase 3 - Execution
During the execution phase, Gradle executes each of the selected tasks.

When Gradle is invoked to execute a task, the lifecycle begins. Let’s see it in action.

Step 2. Update the Settings File
Add the following line to the top of the Settings file:
<< 'settings.gradle.kts'
println("SETTINGS FILE: This is executed during the initialization phase")
settings.gradle.kts

Step 3. Update the Build Script
Add the following lines to the bottom of the Build script:
<< 'app/build.gradle.kts'
println("BUILD SCRIPT: This is executed during the configuration phase")

tasks.register("task1"){
    println("REGISTER TASK1: This is executed during the configuration phase")
}

tasks.register("task2"){
    println("REGISTER TASK2: This is executed during the configuration phase")
}

tasks.named("task1"){
    println("NAMED TASK1: This is executed during the configuration phase")
    doFirst {
        println("NAMED TASK1 - doFirst: This is executed during the execution phase")
    }
    doLast {
        println("NAMED TASK1 - doLast: This is executed during the execution phase")
    }
}

tasks.named("task2"){
    println("NAMED TASK2: This is executed during the configuration phase")
    doFirst {
        println("NAMED TASK2 - doFirst: This is executed during the execution phase")
    }
    doLast {
        println("NAMED TASK2 - doLast: This is executed during the execution phase")
    }
}
app/build.gradle.kts

Step 4. Run a Gradle Task
Run the task1 task that you registered and configured in Step 3:
$ ./gradlew task1

SETTINGS FILE: This is executed during the initialization phase

> Configure project :app
BUILD SCRIPT: This is executed during the configuration phase
REGISTER TASK1: This is executed during the configuration phase
NAMED TASK1: This is executed during the configuration phase

> Task :app:task1
NAMED TASK1 - doFirst: This is executed during the execution phase
NAMED TASK1 - doLast: This is executed during the execution phase

BUILD SUCCESSFUL in 25s
5 actionable tasks: 3 executed, 2 up-to-date
Initialization: Gradle executes ~settings.gradle.kts to determine the projects to be built and creates a Project object for each one.
Configuration: Gradle configures each project by executing the ~build.gradle.kts files. It resolves dependencies and
 creates a dependency graph of all the available tasks.
Execution: Gradle executes the tasks passed on the command line and any prerequisite tasks.
It is important to note that while task1 was configured and executed, task2 was not. This is called
 task configuration avoidance and prevents unnecessary work.

Task configuration avoidance is when Gradle avoids configuring task2 when task1 was called and task1 does NOT depend. on task2.

https://docs.gradle.org/current/userguide/partr3_multi_project_builds.html#partr3_multi_project_builds
Step 1. About Multi-Project Builds
Typically, builds contain multiple projects, such as shared libraries or separate applications that will be deployed in your ecosystem.
In Gradle, a multi-project build consists of:
~settings.gradle.kts file representing your Gradle build including required subprojects
#e.g. include("app", "model", "service")
~build.gradle.kts and source code for each subproject in corresponding subdirectories
Our build currently consists of a root project called authoring-tutorial, which has a single app subproject:

.
├── app
│   ...
│   └── build.gradle.kts
└── settings.gradle.kts

The authoring-tutorial root project
The app subproject
The app source code
The app build script
The optional settings file

Step 2. Add another Subproject to the Build
Imagine that our project is growing and requires a custom library to function.
Let’s create this imaginary lib. First, create a lib folder:
mkdir lib
cd lib
Create a file called ~build.gradle.kts and add the following lines to it:
<< 'lib/build.gradle.kts'
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.guava:guava:32.1.1-jre")
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
lib/build.gradle.kts
Your project should look like this:

.
├── app
│   ...
│   └── build.gradle.kts
├── lib
│   └── build.gradle.kts
└── settings.gradle.kts

Let’s add some code to the lib subproject. Create a new directory:
mkdir -p lib/src/main/java/com/gradle
Create a Java class called CustomLib in a file called CustomLib.java with the following source code:

<< 'lib/src/main/java/com/gradle/CustomLib.java'
package com.gradle;

public class CustomLib {
    public static String identifier = "I'm a String from a lib.";
}
lib/src/main/java/com/gradle/CustomLib.java
The project should now have the following file and directory structure:

.
├── app
│   ├── build.gradle.kts
│   └── src
│       └── main
│           └── java
│               └── authoring
│                   └── tutorial
│                       └── App.java
├── lib
│   ├── build.gradle.kts
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── gradle
│                       └── CustomLib.java
└── settings.gradle.kts

However, the lib subproject does not belong to the build, and you won’t be able to execute task3, until it is added to
 the ~settings.gradle.kts file.

To add lib to the build, update the ~settings.gradle.kts file in the root accordingly:

<< 'settings.gradle.kts'
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "authoring-tutorial"

include("app")
include("lib") // Add lib to the build
settings.gradle.kts
Let’s add the lib subproject as an app dependency in app/~build.gradle.kts:

<< 'app/build.gradle.kts'
dependencies {
    implementation(project(":lib")) // Add lib as an app dependency
}
app/build.gradle.kts
Update the app source code so that it imports the lib:

<< 'app/src/main/java/authoring/tutorial/App.java'
package authoring.tutorial;

import com.gradle.CustomLib;

public class App {
    public String getGreeting() {
        return "CustomLib identifier is: " + CustomLib.identifier;
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
app/src/main/java/authoring/tutorial/App.java
Finally, let’s run the app with the command ./gradlew run:
$ ./gradlew run
Our build for the root project authoring-tutorial now includes two subprojects, app and lib. app depends on lib. You can
 build lib independent of app. However, to build app, Gradle will also build lib.

Step 3. Understand Composite Builds
A composite build is simply a build that includes other builds. Composite builds allow you to:
Extract your build logic from your project build ~and re-use it among subprojects~
Combine builds that are usually developed independently ~such as a plugin and an application~
Decompose a large build into smaller, more isolated chunks

Step 4. Add build to the Build
Let’s add a plugin to our build. First, create a new directory called license-plugin in the gradle directory:
cd gradle
mkdir license-plugin
cd license-plugin
Once in the gradle/license-plugin directory, run gradle init. Make sure that you select the Gradle plugin project as
 well as the other options for the init task below:
$ gradle init --dsl kotlin --type kotlin-gradle-plugin --project-name license
~Select defaults for any additional prompts.
Your project should look like this:

.
├── app
│   ...
│   └── build.gradle.kts
├── lib
│   ...
│   └── build.gradle.kts
├── gradle
│    ├── ...
│    └── license-plugin
│        ├── settings.gradle.kts
│        └── plugin
│            ├── gradle
│            │   └── ....
│            ├── src
│            │   ├── functionalTest
│            │   │   └── ....
│            │   ├── main
│            │   │   └── kotlin
│            │   │       └── license
│            │   │           └── LicensePlugin.kt
│            │   └── test
│            │       └── ...
│            └── build.gradle.kts
│
└── settings.gradle.kts

Take the time to look at the LicensePlugin.kt or LicensePlugin.groovy code and the
 gradle/license-plugin/~settings.gradle.kts file. It’s important to note that this is an entirely separate build with its
  own settings file and build script:

<< 'gradle/license-plugin/settings.gradle.kts'
rootProject.name = "license"
include("plugin")
gradle/license-plugin/settings.gradle.kts
To add our license-plugin build to the root project, update the root ~settings.gradle.kts file accordingly:

<< 'settings.gradle.kts'
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "authoring-tutorial"

include("app")
include("subproject")

includeBuild("gradle/license-plugin") // Add the new build
settings.gradle.kts
You can view the structure of the root project by running ./gradlew projects in the root folder authoring-tutorial:
$ ./gradlew projects
Our build for the root project authoring-tutorial now includes two subprojects, app and lib, and another build, license-plugin.
When in the project root, running:

./gradlew build - Builds app and lib
./gradlew :app:build - Builds app and lib
./gradlew :lib:build - Builds lib only
./gradlew :license-plugin:plugin:build - Builds license-plugin only

There are many ways to design a project’s architecture with Gradle.
Multi-project builds are great for organizing projects with many modules such as mobile-app, web-app, api, lib, and
 documentation that have dependencies between them.
#Composite (include) builds are great for separating build logic (i.e., convention plugins) or testing systems (i.e., patching a library)

https://docs.gradle.org/current/userguide/partr4_settings_file.html
Step 1. Gradle scripts
Build scripts and setting files are code. They are written in Kotlin or Groovy.
You use the Kotlin DSL, Groovy DSL and Gradle APIs to write the scripts.
https://docs.gradle.org/current/kotlin-dsl
https://docs.gradle.org/current/javadoc
The methods that can be used within a Gradle script primarily include:
Gradle APIs - such as getRootProject~~ from the Settings API
https://docs.gradle.org/current/javadoc/org/gradle/api/initialization/Settings.html
Blocks defined in the DSL - such as the plugins{} block from KotlinSettingsScript
https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.kotlin.dsl/-kotlin-settings-script/index.html
Extensions defined by Plugins - such as implementation~~ and api~~ provided by the java plugin when applied

Step 2. The Settings object
The settings file is the entry point of every Gradle build.
During the initialization phase, Gradle finds the settings file in your project root directory.
When the settings file, ~settings.gradle.kts, is found, Gradle instantiates a Settings object.
One of the purposes of the Settings object is to allow you to declare all the projects to be included in the build.
You can use any of the methods and properties on the Settings interface directly in your settings file.
~For example:
<< 'settings.gradle.kts'
includeBuild("some-build")                         // Delegates to Settings.includeBuild()
reportsDir = findProject("/myInternalProject")     // Delegates to Settings.findProject()
settings.gradle.kts

Step 3. The Settings file
Let’s break down the settings file in our project root directory:
<< 'settings.gradle.kts'
#plugins({}) from the PluginDependenciesSpec API
#https://docs.gradle.org/current/dsl/org.gradle.plugin.use.PluginDependenciesSpec.html
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
#id() method from the PluginDependenciesSpec API
}

rootProject.name = "authoring-tutorial"
#getRootProject() method from the Settings API
#https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html

include("app")
include("lib")
#include() method from the Settings API

includeBuild("gradle/license-plugin")
#includeBuild() method from the Settings API
settings.gradle.kts

https://docs.gradle.org/current/userguide/partr5_build_scripts.html#partr5_build_scripts
Step 1. The Project object
Build scripts invoke Gradle APIs to configure the build.
During the configuration phase, Gradle finds the build scripts in the root and subproject directories.
When a build script, ~build.gradle.kts, is found, Gradle configures a Project object.
https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html
The purpose of the Project object is to create a collection of Task objects, apply plugins, and retrieve dependencies.
https://docs.gradle.org/current/javadoc/org/gradle/api/Task.html
You can use any of the methods and properties on the Project interface directly in your script.
~For example:
<< '~build.gradle.kts'
defaultTasks("some-task")      // Delegates to Project.defaultTasks()
reportsDir = file("reports")   // Delegates to Project.file() and the Java Plugin
~build.gradle.kts

Step 2. The Build script
Let’s break down the build script for the plugin:
<< 'gradle/license-plugin/plugin/build.gradle.kts'
#Use the plugins{} block from KotlinSettingsScript in the Kotlin DSL
#https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.kotlin.dsl/-kotlin-settings-script/index.html
#Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
#Apply the Kotlin JVM plugin to add support for Kotlin
plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
}

#Use Project.repositories() to configure the repositories for this project
#Use Maven Central for resolving dependencies
#https://repo.maven.apache.org/maven2/
repositories {
    mavenCentral()
}

#Use Project.dependencies() to configure the dependencies for this project
#Use the Kotlin JUnit 5 integration
dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

#Use the gradlePlugin{} block from GradlePluginDevelopmentExtension in the Kotlin DSL
#https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.plugin.devel/-gradle-plugin-development-extension/index.html
#Define the plugin id and implementationClass
gradlePlugin {
    val greeting by plugins.creating {
        id = "license.greeting"
        implementationClass = "license.LicensePlugin"
    }
}

#Plugins, which enhance your build capabilities, are included like this:
plugins {
    id("java")                          // core plugin, no version required
    id("org.some.plugin") version "2.8" // community plugin, version required
}

#The repositories section lets Gradle know where to pull dependencies from:
repositories {
    mavenCentral()  // get dependencies from the Maven central repository
}

#Dependencies are requirements for building your application or library:
dependencies {
    // group: 'org.apache.commons', name: 'commons-lang3', version: '3.13.0'
    implementation("org.apache.commons:commons-lang3:3.13.0")
}

#In this example, implementation() means that the commons-lang3 library must be added to the Java classpath.
#Every dependency declared for a Gradle project must apply to a scope. That is, the dependency is either needed at
# compile time, runtime, or both. This is called a configuration and the implementation configuration is used when the
#  dependency is only needed in the runtime classpath.
#Configuration blocks (not to be confused with dependency configurations above) are typically used to configure an applied plugin:
gradlePlugin {  // Define a custom plugin
    val greeting by plugins.creating {  // Define `greeting` plugin using the `plugins.creating` method
        id = "license.greeting" // Create plugin with the specified ID
        implementationClass = "license.LicensePlugin"   // and specified implementation class
    }
}

#When the java-gradle-plugin is applied, users must configure the plugin they are developing using the gradlePlugin{} configuration block.
#Tasks are units of work executed during your build. They can be defined by plugins or inline:
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}

#In the example generated by Gradle init, we define two tasks:
#functionalTest: This task is registered using tasks.register(). It configures the test task for functional tests.
#test: This task is configured using tasks.named() for the existing test task. It also configures the task to use JUnit Jupiter for unit tests.
gradle/license-plugin/plugin/build.gradle.kts

Step 3. Apply the Plugin
Let’s apply our license plugin to the app subproject:
<< 'app/build.gradle.kts'
plugins {
    application
    id("com.tutorial.license")  // Apply the license plugin
}
app/build.gradle.kts

Step 4. View Plugin Task
Build init creates a "hello world" plugin when generating a Gradle plugin project. Inside LicensePlugin is simply a
task that prints a greeting to the console, the task name is greeting:
<< 'gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt'
class LicensePlugin: Plugin<Project> {
    override fun apply(project: Project) {                          // Apply plugin
        project.tasks.register("greeting") { task ->                // Register a task
            task.doLast {
                println("Hello from plugin 'com.tutorial.greeting'")  // Hello world printout
            }
        }
    }
}
gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt
As we can see, the license plugin, when applied, exposes a greeting task with a simple print statement.

Step 5. View Plugin Tasks
When the license plugin is applied to the app project, the greeting task becomes available:
To view the task in the root directory, run:
$ ./gradlew tasks --all

Finally, run the greeting task using ./gradlew greeting or:
$ ./gradlew :app:greeting

https://docs.gradle.org/current/userguide/partr6_writing_tasks.html#partr6_writing_tasks
Step 1. Understand Tasks
A Task is an executable piece of code that contains sequences of actions.
Actions are added to a Task via the doFirst{} and doLast{} closures.
A Task can depend on other tasks.

Step 2. Register and Configure Tasks
Early on in the tutorial, we registered and configured task1 in the app build script:
<< 'app/build.gradle.kts'
#You can use the register() method to create new tasks.
tasks.register("task1"){
    println("REGISTER TASK1: This is executed during the configuration phase")
}

#You can use the named() method to configure existing tasks.
tasks.named("task1"){
    println("NAMED TASK1: This is executed during the configuration phase")
    doFirst {
        println("NAMED TASK1 - doFirst: This is executed during the execution phase")
    }
    doLast {
        println("NAMED TASK1 - doLast: This is executed during the execution phase")
    }
}
app/build.gradle.kts

Step 3. Create a custom Task
To create a custom task, you must subclass DefaultTask in Groovy DSL or DefaultTask in Kotlin DSL.
Create a custom class called LicenseTask with the code below and add it to the bottom of the
 gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt file:
<< 'gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt'
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

class LicensePlugin: Plugin<Project> {
    // Don't change anything here
}

abstract class LicenseTask : DefaultTask() {
    @Input
    val fileName = project.rootDir.toString() + "/license.txt"

    @TaskAction
    fun action() {
        // Read the license text
        val licenseText = File(fileName).readText()
        // Walk the directories looking for java files
        File(project.rootDir.toString()).walk().forEach {
            if (it.extension == "java") {
                // Read the source code
                var ins: InputStream = it.inputStream()
                var content = ins.readBytes().toString(Charset.defaultCharset())
                // Write the license and the source code to the file
                it.writeText(licenseText + content)
            }
        }
    }
}
gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt
The LicenseTask class encapsulates the task action logic and declares any inputs and outputs the task expects.
The task action is annotated with @TaskAction. Inside, the logic first finds a file called "license.txt".
This file contains text for an Apache license:
<< 'license.txt'
/*
* Licensed under the Apache License
*/
license.txt
The task then looks for files with the extension .java and adds a license header.
The task has a single input, the license file name, annotated with @Input.
Gradle uses the @Input annotation to determine if the task needs to run. If the task has not run before or if the
 input value has changed since the previous execution, then Gradle will execute the task.
~While a custom class has been created, it is not yet added to the LicensePlugin. Running LicenseTask is not currently possible.
All you can do for now is make sure ./gradlew build runs without failing:
$ ./gradlew build

https://docs.gradle.org/current/userguide/partr7_writing_plugins.html#partr7_writing_plugins
Step 1. Develop the Plugin
Let’s tie our custom LicenseTask to our plugin.
<< 'gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt'
#Update the LicensePlugin with the code for the Plugin below (don’t change anything else in the file):
class LicensePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("license", LicenseTask::class.java) { task ->
            task.description = "add a license header to source code"   // Add description
            task.group = "from license plugin"                         // Add group
        }
    }
}
gradle/license-plugin/plugin/src/main/kotlin/license/LicensePlugin.kt

Step 2. Add a license.txt file
Add a file called license.txt to the root directory of the project and add the following text to it:
<< 'license.txt'
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
license.txt

Step 3. Apply the Plugin
<< 'app/build.gradle.kts'
#Apply the plugin to the app subproject (if not done already):
plugins {
    application
    id("com.tutorial.license") // Apply custom plugin
}
app/build.gradle.kts
Make sure the plugin is correctly applied by listing the available tasks in the app subproject:
$ ./gradlew :app:tasks

Next, let’s run the task with
$ ./gradlew :app:license:

#https://docs.gradle.org/current/userguide/multi_project_builds.html#multi_project_builds
It is important to structure your Gradle project to optimize build performance.
A multi-project build is the standard in Gradle.
A multi-project build consists of one root project and one or more subprojects.
Gradle can build the root project and any number of the subprojects in a single execution.

Project locations
Multi-project builds contain a single root project in a directory that Gradle views as the root path: ..
Subprojects are located physically under the root path: ./subproject.
#https://docs.gradle.org/current/userguide/intro_multi_project_builds.html#sec:project_path
A subproject has a path, which denotes the position of that subproject in the multi-project build. In most cases,
 the project path is consistent with its location in the file system.
The project structure is created in the ~settings.gradle.kts file. The settings file must be present in the root directory.

A simple multi-project build
Let’s look at a basic multi-project build example that contains a root project and a single subproject.
The root project is called basic-multiproject, located somewhere on your machine.
From Gradle’s perspective, the root is the top-level directory ..
The project contains a single subproject called ./app:

.
├── app
│   ...
│   └── build.gradle.kts
└── settings.gradle.kts

This is the recommended project structure for starting any Gradle project. The build init plugin also
generates skeleton projects that follow this structure - a root project with a single subproject:
#https://docs.gradle.org/current/userguide/build_init_plugin.html#build_init_plugin
The ~settings.gradle.kts file describes the project structure to Gradle:
<< 'settings.gradle.kts'
rootProject.name = "basic-multiproject"
include("app")
#In this case, Gradle will look for a build file for the app subproject in the ./app directory.
settings.gradle.kts
You can view the structure of a multi-project build by running the projects command:
$ ./gradlew -q projects

In this example, the app subproject is a Java application that applies the application plugin and configures the
 main class. The application prints Hello World to the console:
#https://docs.gradle.org/current/userguide/application_plugin.html#application_plugin
<< 'app/build.gradle.kts'
plugins {
    id("application")
}

application {
    mainClass = "com.example.Hello"
}
app/build.gradle.kts
<< 'app/src/main/java/com/example/Hello.java'
package com.example;

public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
app/src/main/java/com/example/Hello.java
You can run the application by executing the run task from the application plugin in the project root:
$ ./gradlew -q run
Hello, world!

Adding a subproject
In the settings file, you can use the include method to add another subproject to the root project:
<< 'settings.gradle.kts'
include("project1", "project2:child1", "project3:child1")
settings.gradle.kts
The include method takes project paths as arguments. The project path is assumed to be equal to the
relative physical file system path. For example, a path services:api is mapped by default to a folder
#./services/api (relative to the project root .).
https://docs.gradle.org/current/userguide/intro_multi_project_builds.html#sec:project_path
More examples of how to work with the project path can be found in the DSL documentation of Settings.include~java.lang.String[]~.
#https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings:include(java.lang.String[])
Let’s add another subproject called lib to the previously created project.
All we need to do is add another include statement in the root settings file:
<< 'settings.gradle.kts'
rootProject.name = "basic-multiproject"
include("app")
include("lib")
settings.gradle.kts
Gradle will then look for the build file of the new lib subproject in the ./lib/ directory:

.
├── app
│   ...
│   └── build.gradle.kts
├── lib
│   ...
│   └── build.gradle.kts
└── settings.gradle.kts

Project Descriptors
To further describe the project architecture to Gradle, the settings file provides project descriptors.
You can modify these descriptors in the settings file at any time.
To access a descriptor, you can:
<< 'settings.gradle.kts'
include("project-a")
println(rootProject.name)
println(project(":project-a").name)
settings.gradle.kts
Using this descriptor, you can change the name, project directory, and build file of a project:
<< 'settings.gradle.kts'
rootProject.name = "main"
include("project-a")
project(":project-a").projectDir = file("custom/my-project-a")
project(":project-a").buildFileName = "project-a.gradle.kts"
settings.gradle.kts
#https://docs.gradle.org/current/javadoc/org/gradle/api/initialization/ProjectDescriptor.html
Consult the ProjectDescriptor class in the API documentation for more information.

Modifying a subproject path
Let’s take a hypothetical project with the following structure:

.
├── app
│   ...
│   └── build.gradle.kts
├── subs // Gradle may see this as a subproject
│   └── web // Gradle may see this as a subproject
│       └── my-web-module // Intended subproject
│           ...
│           └── build.gradle.kts
└── settings.gradle.kts

#If your ~settings.gradle.kts looks like this:
<< 'settings.gradle.kts'
include(':subs:web:my-web-module')
settings.gradle.kts
Gradle sees a subproject with a logical project name of :subs:web:my-web-module and two, possibly unintentional, other
 subprojects logically named :subs and :subs:web. This can lead to phantom build directories, especially when using allprojects{} or subproject{}.
To avoid this, you can use:
<< 'settings.gradle.kts'
include(':my-web-module')
project(':my-web-module').projectDir = "subs/web/my-web-module"
settings.gradle.kts
So that you only end up with a single subproject named :my-web-module.
So, while the physical project layout is the same, the logical results are different.

Naming recommendations
As your project grows, naming and consistency get increasingly more important.
 To keep your builds maintainable, we recommend the following:
1. Keep default project names for subprojects: It is possible to configure custom project names in the
 settings file. However, it’s an unnecessary extra effort for the developers to track which projects belong to what folders.
2. Use lower case hyphenation for all project names: All letters are lowercase, and words are separated with a dash - character.
3. Define the root project name in the settings file: The rootProject.name effectively assigns a name to the
 build, used in reports like Build Scans. If the root project name is not set, the name will be the
  container directory name, which can be unstable
#  (i.e., you can check out your project in any directory). The name will be generated randomly if the
#   root project name is not set and checked out to a file system’s root (e.g., / or C:~.

https://docs.gradle.org/current/userguide/declaring_dependencies_between_subprojects.html
What if one subproject depends on another subproject? What if one project needs the artifact produced by another project?
This is a common use case for multi-project builds. Gradle offers project dependencies for this.
https://docs.gradle.org/current/userguide/declaring_dependencies_adv.html#sub:project_dependencies

Depending on another project
Let’s explore a theoretical multi-project build with the following layout:

.
├── api
│   ├── src
│   │   └──...
│   └── build.gradle.kts
├── services
│   └── person-service
│       ├── src
│       │   └──...
│       └── build.gradle.kts
├── shared
│   ├── src
│   │   └──...
│   └── build.gradle.kts
└── settings.gradle.kts

In this example, there are three subprojects called shared, api, and person-service:
The person-service subproject depends on the other two subprojects, shared and api.
The api subproject depends on the shared subproject.
We use the : separator to define a project path such as services:person-service or :shared.
#https://docs.gradle.org/current/userguide/intro_multi_project_builds.html#sec:project_path
# Consult the DSL documentation of Settings.include(java.lang.String[]) for more information about defining project paths.
#https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings:include(java.lang.String[])

<< 'settings.gradle.kts'
rootProject.name = "dependencies-java"
include("api", "shared", "services:person-service")
settings.gradle.kts
<< 'shared/build.gradle.kts'
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
}
shared/build.gradle.kts
<< 'api/build.gradle.kts'
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
    implementation(project(":shared"))
}
api/build.gradle.kts
<< 'services/person-service/build.gradle.kts'
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
    implementation(project(":shared"))
    implementation(project(":api"))
}
services/person-service/build.gradle.kts
A project dependency affects execution order. It causes the other project to be built first and adds the output with the
 classes of the other project to the classpath. It also adds the dependencies of the other project to the classpath.
~If you execute
./gradlew :api:compile
, first the shared project is built, and then the api project is built.

Depending on artifacts produced by another project
Sometimes, you might want to depend on the output of a specific task within another project rather than the
 entire project. However, explicitly declaring a task dependency from one project to another is discouraged as it
  introduces unnecessary coupling between tasks.
The recommended way to model dependencies, where a task in one project depends on the output of another, is to
 produce the output and mark it as an "outgoing" artifact. Gradle’s dependency management engine allows you to
  share arbitrary artifacts between projects and build them on demand.
#https://docs.gradle.org/current/userguide/cross_project_publications.html#cross_project_publications

https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html
Subprojects in a multi-project build typically share some common dependencies.
Instead of copying and pasting the same Java version and libraries in each subproject build script, Gradle provides a
 special directory for storing shared build logic that can be automatically applied to subprojects.

Share logic in buildSrc
buildSrc is a Gradle-recognized and protected directory which comes with some benefits:
Reusable Build Logic:
buildSrc allows you to organize and centralize your custom build logic, tasks, and plugins in a structured manner. The
 code written in buildSrc can be reused across your project, making it easier to maintain and share common build functionality.
Isolation from the Main Build:
Code placed in buildSrc is isolated from the other build scripts of your project. This helps keep the
 main build scripts cleaner and more focused on project-specific configurations.
Automatic Compilation and Classpath:
The contents of the buildSrc directory are automatically compiled and included in the classpath of your main build. This
means that classes and plugins defined in buildSrc can be directly used in your project’s build scripts without any additional configuration.
Ease of Testing:
Since buildSrc is a separate build, it allows for easy testing of your custom build logic. You can write tests for
 your build code, ensuring that it behaves as expected.
Gradle Plugin Development:
~If you are developing custom Gradle plugins for your project, buildSrc is a convenient place to house the
 plugin code. This makes the plugins easily accessible within your project.
The buildSrc directory is treated as an included build.
#https://docs.gradle.org/current/userguide/composite_builds.html#composite_build_intro
~For multi-project builds, there can be only one buildSrc directory, which must be in the root project directory.
The downside of using buildSrc is that any change to it will invalidate every task in your project and require a rerun.
buildSrc uses the same source code conventions applicable to Java, Groovy, and Kotlin projects. It also provides direct access to the Gradle API.
#https://docs.gradle.org/current/userguide/java_plugin.html#javalayout
A typical project including buildSrc has the following layout:

.
├── buildSrc
│   ├── src
│   │   └──main
│   │      └──kotlin
│   │         └──MyCustomTask.kt
│   ├── shared.gradle.kts
│   └── build.gradle.kts
├── api
│   ├── src
│   │   └──...
│   └── build.gradle.kts
├── services
│   └── person-service
│       ├── src
│       │   └──...
│       └── build.gradle.kts
├── shared
│   ├── src
│   │   └──...
│   └── build.gradle.kts
└── settings.gradle.kts

Create the MyCustomTask task.
A shared build script.
Uses the MyCustomTask task and shared build script.
In the buildSrc, the build script ~shared.gradle.kts is created. It contains dependencies and
 other build information that is common to multiple subprojects:

<< 'shared.gradle.kts'
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.32")
}
shared.gradle.kts
In the buildSrc, the MyCustomTask is also created. It is a helper task that is used as part of the build logic for multiple subprojects:

<< 'MyCustomTask.kt'
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class MyCustomTask : DefaultTask() {
    @TaskAction
    fun calculateSum() {
        // Custom logic to calculate the sum of two numbers
        val num1 = 5
        val num2 = 7
        val sum = num1 + num2

        // Print the result
        println("Sum: $sum")
    }
}
MyCustomTask.kt
The MyCustomTask task is used in the build script of the api and shared projects. The task is automatically available because it’s part of buildSrc.
The ~shared.build.kts file is also applied:

<< 'build.gradle.kts'
// Apply any other configurations specific to your project

// Use the build script defined in buildSrc
apply(from = rootProject.file("buildSrc/shared.gradle"))

// Use the custom task defined in buildSrc
tasks.register<MyCustomTask>("myCustomTask")
build.gradle.kts

Share logic using convention plugins
Gradle’s recommended way of organizing build logic is to use its plugin system.
We can write a plugin that encapsulates the build logic common to several subprojects in a project.
This kind of plugin is called a convention plugin.
~While writing plugins is outside the scope of this section, the recommended way to build a Gradle project is to
 put common build logic in a convention plugin located in the buildSrc.
Let’s take a look at an example project:

.
├── buildSrc
│   ├── src
│   │   └──main
│   │      └──kotlin
│   │         └──myproject.java-conventions.gradle.kts
│   └── build.gradle.kts
├── api
│   ├── src
│   │   └──...
│   └── build.gradle.kts
├── services
│   └── person-service
│       ├── src
│       │   └──...
│       └── build.gradle.kts
├── shared
│   ├── src
│   │   └──...
│   └── build.gradle.kts
└── settings.gradle.kts

Create the myproject.java-conventions convention plugin.
Applies the myproject.java-conventions convention plugin.
This build contains three subprojects:

<< 'settings.gradle.kts'
rootProject.name = "dependencies-java"
include("api", "shared", "services:person-service")
settings.gradle.kts
The source code for the convention plugin created in the buildSrc directory is as follows:

<< 'buildSrc/src/main/kotlin/myproject.java-conventions.gradle.kts'
plugins {
    id("java")
}

group = "com.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
}
buildSrc/src/main/kotlin/myproject.java-conventions.gradle.kts
~For the convention plugin to compile, basic configuration needs to be applied in the build file of the buildSrc directory:

<< 'buildSrc/build.gradle.kts'
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
buildSrc/build.gradle.kts
The convention plugin is applied to the api, shared, and person-service subprojects:

<< 'api/build.gradle.kts'
plugins {
    id("myproject.java-conventions")
}

dependencies {
    implementation(project(":shared"))
}
api/build.gradle.kts
<< 'shared/build.gradle.kts'
plugins {
    id("myproject.java-conventions")
}
shared/build.gradle.kts
<< 'services/person-service/build.gradle.kts'
plugins {
    id("myproject.java-conventions")
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":api"))
}
services/person-service/build.gradle.kts

~Do not use cross-project configuration
An improper way to share build logic between subprojects is cross-project configuration via the subprojects {} and allprojects {} DSL constructs.
Avoid using subprojects {} and allprojects {}.
With cross-configuration, build logic can be injected into a subproject which is not obvious when looking at its build script.
In the long run, cross-configuration usually grows in complexity and becomes a burden. Cross-configuration can also
 introduce configuration-time coupling between projects, which can prevent optimizations like configuration-on-demand from working properly.
Convention plugins versus cross-configuration
The two most common uses of cross-configuration can be better modeled using convention plugins:
Applying plugins or other configurations to subprojects of a certain type.
Often, the cross-configuration logic is if subproject is of type X, then configure Y. This is equivalent to
 applying X-conventions plugin directly to a subproject.
Extracting information from subprojects of a certain type.
This use case can be modeled using outgoing configuration variants.
#https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects

https://docs.gradle.org/current/userguide/composite_builds.html
A composite build is a build that includes other builds.
A composite build is similar to a Gradle multi-project build, except that instead of including subprojects, entire builds are included.
Composite builds allow you to:
Combine builds that are usually developed independently, for instance, when trying out a bug fix in a library that your application uses.
Decompose a large multi-project build into smaller, more isolated chunks that can be worked on independently or together as needed.
A build that is included in a composite build is referred to as an included build. Included builds do not share any
configuration with the composite build or the other included builds. Each included build is configured and executed in isolation.

Defining a composite build
The following example demonstrates how two Gradle builds, normally developed separately, can be combined into a composite build.

my-composite
├── gradle
├── gradlew
├── settings.gradle.kts
├── build.gradle.kts
├── my-app
│   ├── settings.gradle.kts
│   └── app
│       ├── build.gradle.kts
│       └── src/main/java/org/sample/my-app/Main.java
└── my-utils
    ├── settings.gradle.kts
    ├── number-utils
    │   ├── build.gradle.kts
    │   └── src/main/java/org/sample/numberutils/Numbers.java
    └── string-utils
        ├── build.gradle.kts
        └── src/main/java/org/sample/stringutils/Strings.java

The my-utils multi-project build produces two Java libraries, number-utils and string-utils. The my-app build produces an
 executable using functions from those libraries.
The my-app build does not depend directly on my-utils. Instead, it declares binary dependencies on the libraries produced by my-utils:

<< 'my-app/app/build.gradle.kts'
plugins {
    id("application")
}

application {
    mainClass = "org.sample.myapp.Main"
}

dependencies {
    implementation("org.sample:number-utils:1.0")
    implementation("org.sample:string-utils:1.0")
}
my-app/app/build.gradle.kts

Defining a composite build via --include-build
The --include-build command-line argument turns the executed build into a composite, substituting dependencies from the
 included build into the executed build.
~For example, the output of ./gradlew run --include-build ../my-utils run from my-app:
$ ./gradlew --include-build ../my-utils run

Defining a composite build via the settings file
It’s possible to make the above arrangement persistent by using Settings.includeBuild~java.lang.Object~ to declare the
 included build in the ~settings.gradle.kts file.
#https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings:includeBuild(java.lang.Object)
The settings file can be used to add subprojects and included builds simultaneously.
Included builds are added by location:
<< 'settings.gradle.kts'
includeBuild("my-utils")
settings.gradle.kts
In the example, the ~settings.gradle.kts file combines otherwise separate builds:
<< 'settings.gradle.kts'
rootProject.name = "my-composite"

includeBuild("my-app")
includeBuild("my-utils")
settings.gradle.kts
To execute the run task in the my-app build from my-composite, run
./gradlew my-app:app:run.
You can optionally define a run task in my-composite that depends on my-app:app:run so that you can execute
./gradlew run:
<< 'build.gradle.kts'
tasks.register("run") {
    dependsOn(gradle.includedBuild("my-app").task(":app:run"))
}
build.gradle.kts

Including builds that define Gradle plugins
A special case of included builds are builds that define Gradle plugins.
These builds should be included using the includeBuild statement inside the pluginManagement {} block of the settings file.
Using this mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself:
<<'settings.gradle.kts'
pluginManagement {
    includeBuild("../url-verifier-plugin")
}
settings.gradle.kts

Restrictions on included builds
Most builds can be included in a composite, including other composite builds. There are some restrictions.
In a regular build, Gradle ensures that each project has a unique project path. It makes projects identifiable and addressable without conflicts.
In a composite build, Gradle adds additional qualification to each project from an included build to
 avoid project path conflicts. The full path to identify a project in a composite build is called a
  build-tree path. It consists of a build path of an included build and a project path of the project.
By default, build paths and project paths are derived from directory names and structure on disk. Since included builds can
 be located anywhere on disk, their build path is determined by the name of the containing directory. This can sometimes lead to conflicts.
To summarize, the included builds must fulfill these requirements:
Each included build must have a unique build path.
Each included build path must not conflict with any project path of the main build.
These conditions guarantee that each project can be uniquely identified even in a composite build.
~If conflicts arise, the way to resolve them is by changing the build name of an included build:
<<'settings.gradle.kts'
includeBuild("some-included-build") {
    name = "other-name"
}
settings.gradle.kts
When a composite build is included in another composite build, both builds have the same parent.
In other words, the nested composite build structure is flattened.

Interacting with a composite build
Interacting with a composite build is generally similar to a regular multi-project build. Tasks can be executed, tests can
 be run, and builds can be imported into the IDE.
Executing tasks
Tasks from an included build can be executed from the command-line or IDE in the same way as tasks from a
 regular multi-project build. Executing a task will result in task dependencies being executed, as well as those
  tasks required to build dependency artifacts from other included builds.
You can call a task in an included build using a fully qualified path, for example,
 :included-build-name:project-name:taskName. Project and task names can be abbreviated.
#https://docs.gradle.org/current/userguide/command_line_interface.html#sec:name_abbreviation
$ ./gradlew :included-build:subproject-a:compileJava
> Task :included-build:subproject-a:compileJava
$ ./gradlew :i-b:sA:cJ
> Task :included-build:subproject-a:compileJava
To exclude a task from the command line, you need to provide the fully qualified path to the task.
#https://docs.gradle.org/current/userguide/command_line_interface.html#sec:excluding_tasks_from_the_command_line
Included build tasks are automatically executed to generate required dependency artifacts, or the including build can
 declare a dependency on a task from an included build.
#https://docs.gradle.org/current/userguide/composite_builds.html#included_build_task_dependencies

Declaring dependencies substituted by an included build
By default, Gradle will configure each included build to determine the dependencies it can provide. The algorithm for
 doing this is simple. Gradle will inspect the group and name for the projects in the included build and
 substitute project dependencies for any external dependency matching ${project.group}:${project.name}.
By default, substitutions are not registered for the main build.
To make the ~sub~projects of the main build addressable by ${project.group}:${project.name}, you can tell Gradle to
 treat the main build like an included build by self-including it:
<<'settings.gradle.kts'
 includeBuild(".").
settings.gradle.kts
There are cases when the default substitutions determined by Gradle are insufficient or must be corrected for a
 particular composite. For these cases, explicitly declaring the substitutions for an included build is possible. For example,
  a single-project build called anonymous-library, produces a Java utility library but does not declare a value for
 the group attribute:
<<'build.gradle.kts'
plugins {
    java
}
#When this build is included in a composite, it will attempt to substitute for the dependency module
# undefined:anonymous-library (undefined being the default value for project.group, and anonymous-library being the root project name).
build.gradle.kts
  Clearly, this isn’t useful in a composite build.
To use the unpublished library in a composite build, you can explicitly declare the substitutions that it provides:
<<'settings.gradle.kts'
includeBuild("anonymous-library") {
    dependencySubstitution {
        substitute(module("org.sample:number-utils")).using(project(":"))
    }
}
settings.gradle.kts
With this configuration, the my-app composite build will substitute any dependency on org.sample:number-utils with a
 dependency on the root project of anonymous-library.

Deactivate included build substitutions for a configuration
~If you need to resolve a published version of a module that is also available as part of an included build, you can
 deactivate the included build substitution rules on the ResolutionStrategy of the Configuration that is resolved.
  This is necessary because the rules are globally applied in the build, and Gradle does not consider published versions during resolution by default.
#https://docs.gradle.org/current/userguide/declaring_dependencies_adv.html#sec:resolvable-consumable-configs
#https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.ResolutionStrategy.html
~For example, we create a separate publishedRuntimeClasspath configuration that gets resolved to the published versions of
modules that also exist in one of the local builds. This is done by deactivating global dependency substitution rules:
<<'build.gradle.kts'
configurations.create("publishedRuntimeClasspath") {
    resolutionStrategy.useGlobalDependencySubstitutionRules = false

    extendsFrom(configurations.runtimeClasspath.get())
    isCanBeConsumed = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
}
build.gradle.kts
A use-case would be to compare published and locally built JAR files.

Depending on tasks in an included build
~While included builds are isolated from one another and cannot declare direct dependencies, a composite build can
 declare task dependencies on its included builds. The included builds are accessed using
# Gradle.getIncludedBuilds() or Gradle.includedBuild(java.lang.String), and a task reference is obtained via the IncludedBuild.task(java.lang.String) method.
#https://docs.gradle.org/current/dsl/org.gradle.api.invocation.Gradle.html#org.gradle.api.invocation.Gradle:includedBuilds
#https://docs.gradle.org/current/dsl/org.gradle.api.invocation.Gradle.html#org.gradle.api.invocation.Gradle:includedBuild(java.lang.String)
#https://docs.gradle.org/current/dsl/org.gradle.api.initialization.IncludedBuild.html#org.gradle.api.initialization.IncludedBuild:task(java.lang.String)
Using these APIs, it is possible to declare a dependency on a task in a particular included build:
<<'build.gradle.kts'
tasks.register("run") {
    dependsOn(gradle.includedBuild("my-app").task(":app:run"))
}
build.gradle.kts
Or you can declare a dependency on tasks with a certain path in some or all of the included builds:
<<'build.gradle.kts'
tasks.register("publishDeps") {
    dependsOn(gradle.includedBuilds.map { it.task(":publishMavenPublicationToMavenRepository") })
}
build.gradle.kts

Limitations of composite builds
Limitations of the current implementation include:
No support for included builds with publications that don’t mirror the project default configuration.
See Cases where composite builds won’t work.
#https://docs.gradle.org/current/userguide/composite_builds.html#included_build_substitution_limitations
Multiple composite builds may conflict when run in parallel if more than one includes the same build.
Gradle does not share the project lock of a shared composite build between Gradle invocations to prevent concurrent execution.

https://docs.gradle.org/current/userguide/multi_project_configuration_and_execution.html

https://docs.gradle.org/current/userguide/more_about_tasks.html
A task represents some independent unit of work that a build performs, such as compiling classes,
creating a JAR, generating Javadoc, or publishing archives to a repository.




