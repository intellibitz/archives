import java.util.*

/*
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.config.KotlinCompilerVersion
*/

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

group = "intellibitz"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

android {
    namespace = "intellibitz.intellidroid"
    compileSdk = 35

    buildFeatures {
        dataBinding = true
    }

    defaultConfig {
        applicationId = "intellibitz.intellidroid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags("-frtti -fexceptions")
            }
        }
    }

    flavorDimensions += "color"
    productFlavors {
        create("dev") {
            dimension = "color"
            minSdk = 24
            targetSdk = 35
            applicationId = "intellibitz.intellidroid.dev"
            versionCode = 1
            versionName = "1.0-dev"
        }
        create("qa") {
            dimension = "color"
            minSdk = 24
            targetSdk = 35
            applicationId = "intellibitz.intellidroid.qa"
            versionCode = 1
            versionName = "1.0-qa"
        }
        create("uat") {
            dimension = "color"
            minSdk = 24
            targetSdk = 35
            applicationId = "intellibitz.intellidroid.uat"
            versionCode = 1
            versionName = "1.0-uat"
        }
        create("prod") {
            dimension = "color"
            minSdk = 24
            targetSdk = 35
        }
    }

    val keystorePropsFile = project.file("${projectDir}/keystore.properties")
    if (keystorePropsFile.exists()) {
        val props = Properties()
        keystorePropsFile.inputStream().use { props.load(it) }
        signingConfigs {
            create("release") {
                storeFile = file("${projectDir}/" + props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            isMinifyEnabled = false
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        create("jnidebug") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".jnidebug"
            isJniDebuggable = true
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        quiet = true
        abortOnError = false
    }

    useLibrary("org.apache.http.legacy")
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        optIn.addAll("kotlinx.coroutines.ExperimentalCoroutinesApi", "kotlinx.coroutines.FlowPreview")
    }
}

/*
    implementationOptions {
        incremental=true
    }
*/
/*
    sourceSets {
        main {
            manifest.srcFile "src/main/AndroidManifest.xml"
            java.srcDirs = ["src/main/java"]
            resources.srcDirs = ["src/main/res"]
            aidl.srcDirs = ["src/main/java"]
            renderscript.srcDirs = ["src/main/java"]
            res.srcDirs = ["src/main/res"]
            assets.srcDirs = ["assets"]
            jni.srcDirs = []
            jniLibs.srcDirs = ["libs"]
        }
        debug.setRoot("build-types/debug")
        release.setRoot("build-types/release")
    }
*/

dependencies {
//    implementation(project(":shared"))
//    implementation fileTree(dir: "libs", include: ["**"])
//    implementation fileTree(dir: "libs", include: ["*.jack"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    kapt("androidx.room:room-compiler:2.2.5")
//    kapt ("com.github.bumptech.glide:compiler:4.10.0")
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0-rc01")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")

//https://developer.android.com/jetpack/androidx/versions
//https://developer.android.com/kotlin/ktx
// Provides compatibility wrappers for a number of framework APIs,
// such as Context.obtainDrawable() and View.performAccessibilityAction().
    implementation("androidx.core:core-ktx:1.5.0-alpha01")
    implementation("androidx.activity:activity-ktx:1.2.0-alpha07")
// Adds support for encapsulation of user interface and functionality with fragments,
// enabling applications to provide layouts that adjust between small and large-screen devices.
// This module has dependencies on compat, core-utils, core-ui, and media-compat.
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.navigation:navigation-runtime-ktx:2.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.2.0")
    implementation ("androidx.room:room-ktx:2.2.5")
    implementation ("androidx.sqlite:sqlite-ktx:2.1.0")
    implementation ("androidx.work:work-runtime-ktx:2.4.0")
// The v7 palette support library includes the Palette class, which lets you extract prominent colors
// from an image. For example, a music app could use a Palette object to extract the major colors from
// an album cover, and use those colors to build a color-coordinated song title card.
    implementation("androidx.palette:palette-ktx:1.0.0")
// https://developers.google.com/android/guides/setup
//    https://developers.google.com/android/guides/setup#split
    //    do not include the full library.. implementation will be terribly slow.. do selective play libs only
// https://firebase.google.com/docs/android/setup
//    https://firebase.google.com/docs/android/setup#kotlin+ktx
    implementation("com.google.firebase:firebase-messaging:20.2.4")
    implementation("com.google.gms:google-services:4.3.3")
    implementation("com.google.android.gms:play-services-gcm:17.0.0")

// The Annotation package provides APIs to support adding annotation metadata to your apps.
    implementation("androidx.annotation:annotation:1.2.0-alpha1")
/*
This library adds support for the Action Bar user interface design pattern. This library includes
support for material design user interface implementations.
Note: This library depends on the v4 Support Library.
Here are a few of the key classes included in the v7 appcompat library:
ActionBar - Provides an implementation of the action bar user interface pattern. For more information on
using the Action Bar, see the Action Bar developer guide.
AppCompatActivity - Adds an application activity class that can be used as a base class for activities
that use the Support Library action bar implementation.
AppCompatDialog - Adds a dialog class that can be used as a base class for AppCompat themed dialogs.
ShareActionProvider - Adds support for a standardized sharing action (such as email or posting to
social applications) that can be included in an action bar.
 */
    implementation("androidx.appcompat:appcompat:1.3.0-alpha01")
//  The Custom Tabs package provides APIs to support adding and managing custom tabs in your apps.
//    The Custom Tabs Support library adds support for various classes, such as Custom Tabs Service and Custom Tabs Callback.
    implementation("androidx.browser:browser:1.3.0-alpha05")
// This library adds support for the CardView widget, which lets you show information inside cards
// that have a consistent look on any app. These cards are useful for material design implementations,
// and are used extensively in layouts for TV apps.
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
// After you download the Android Support Libraries, this library adds support for the GridLayout class,
// which allows you to arrange user interface elements using a grid of rectangular cells.
// For detailed information about the v7 gridlayout library APIs, see the android.support.v7.widget package in the API reference.
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    /*
This library is designed to be used for Android 3.2 (API level 13) and higher.
It adds support for the Fragment user interface pattern with the (FragmentCompat) class and
additional fragment support classes. For more information about fragments, see the Fragments
developer guide. For detailed information about the v13 Support Library APIs,
see the android.support.v13 package in the API reference.
 */
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
// Provides a number of utility classes, such as AsyncTaskLoader and PermissionChecker.
    implementation("androidx.legacy:legacy-support-core-utils:1.0.0")
//Implements a variety of UI-related components, such as ViewPager, NestedScrollView, and ExploreByTouchHelper.
    implementation("androidx.legacy:legacy-support-core-ui:1.0.0")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
/*
This library provides MediaRouter, MediaRouteProvider, and related media classes that support Google Cast.
In general, the APIs in the v7 mediarouter library provide a means of controlling the routing of media
channels and streams from the current device to external screens, speakers, and other destination devices.
The library includes APIs for publishing app-specific media route providers, for discovering and
selecting destination devices, for checking media status, and more. For detailed information about
the v7 mediarouter library APIs, see the android.support.v7.media package in the API reference.
 */
/*
https://developer.android.com/topic/libraries/support-library/revisions.html
Revision 25.2.0
(January 2017)
Important: There is a known bug in the android.support.v7.media.MediaRouter class in
 revision 25.2.0 and 25.1.0 of the Support Library. If your app uses the v7 MediaRouter,
  you should use Support Library Revision 25.0.1 until this bug is resolved in a later
   release.
 */
    implementation("androidx.mediarouter:mediarouter:1.2.0-alpha02")
// Backports portions of the media framework, including MediaBrowser and MediaSession.
    implementation("androidx.media:media:1.2.0-alpha04")
//    // Interacting with MediaSessions
    implementation("androidx.media2:media2-session:1.3.0")
//    // optional - UI widgets for VideoView and MediaControlView
    implementation("androidx.media2:media2-widget:1.3.0")
//    // optional - Implementation of a SessionPlayer
    implementation("androidx.media2:media2-player:1.3.0")
/*
The Design package provides APIs to support adding material design components and patterns to your apps.
The Design Support library adds support for various material design components and patterns for app
developers to build upon, such as navigation drawers, floating action buttons (FAB), snackbars, and tabs.
 */
    implementation("com.google.android.material:material:1.3.0-alpha02")
/*
The preference package provides APIs to support adding preference objects, such as CheckBoxPreference and
ListPreference, for users to modify UI settings.
The v7 Preference library adds support for interfaces, such as Preference.OnPreferenceChangeListener
and Preference.OnPreferenceClickListener, and classes, such as CheckBoxPreference and ListPreference.
 */
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.paging:paging-runtime:3.0.0-alpha04")
/*
The Percent package provides APIs to support adding and managing percentage based dimensions in your app.
The Percent Support library adds support for the PercentLayoutHelper.PercentLayoutParams interface and
various classes, such as PercentFrameLayout and PercentRelativeLayout.
 */
    implementation("androidx.percentlayout:percentlayout:1.0.0")
//    The recyclerview library adds the RecyclerView class. This class provides support for the RecyclerView widget,
// a view for efficiently displaying large data sets by providing a limited window of data items.
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha05")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
//https://developer.android.com/studio/build/multidex
//If your minSdkVersion is set to 21 or higher, multidex is enabled by default and you do not need the multidex support library.
    // Enabling multidex support. (only for sdk 20 or lower)
    implementation("androidx.multidex:multidex:2.0.1")

    //    Socket IO
    implementation("io.socket:socket.io-client:1.0.0") {
        // excluding org.json which is provided by Android
        exclude(mapOf("group" to "org.json", "module" to "json"))
    }
    implementation("com.googlecode.libphonenumber:libphonenumber:9.0.39")
    implementation("com.android.volley:volley:1.2.1")
//https://developer.android.com/jetpack/androidx/releases/work
//    // optional - RxJava2 support
//    implementation "androidx.work:work-rxjava2:$work_version"
//    // optional - GCMNetworkManager support
//    implementation "androidx.work:work-gcm:$work_version"
//    // optional - Test helpers
//    androidTestImplementation "androidx.work:work-testing:$work_version"
//    implementation("com.android.support:multidex:1.0.3")
/*
    implementation("com.android.widget.intellibitz.intellidroid.widget.advrecyclerview:intellibitz.intellidroid.widget.advrecyclerview:0.9.1@aar") {
        transitive = true
    }
*/
//    implementation "androidx.room:room-runtime:2.2.5"
//    implementation "com.github.bumptech.glide:glide:4.10.0"
//    implementation "com.google.code.gson:gson:2.8.6"
//    implementation "com.squareup.okhttp3:logging-interceptor:4.7.2"
//    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
//    implementation "com.squareup.retrofit2:retrofit:2.9.0"

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test-junit"))
//        http://android-doc.github.io/tools/building/multidex.html
    androidTestImplementation("androidx.multidex:multidex-instrumentation:2.0.0") {
        exclude(mapOf("group" to "com.android.support", "module" to "multidex"))
    }
//    https://developer.android.com/training/testing/set-up-project
    // Optional -- Robolectric environment
    androidTestImplementation("androidx.test:core:1.2.0")
    // Core library
// Required -- JUnit 4 framework
    androidTestImplementation("junit:junit:4.13.2")
    // Optional -- Mockito framework
    androidTestImplementation("org.mockito:mockito-core:3.3.3")
// AndroidJUnitRunner and JUnit Rules
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.annotation:annotation:1.1.0")
    androidTestImplementation("androidx.appcompat:appcompat:1.3.0-alpha01")
    androidTestImplementation("androidx.recyclerview:recyclerview:1.2.0-alpha05")
    androidTestImplementation("com.google.android.material:material:1.3.0-alpha02")

// Assertions
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.ext:truth:1.2.0")
    androidTestImplementation("com.google.truth:truth:0.42")
// Espresso core
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
// Espresso-contrib for DatePicker, RecyclerView, Drawer actions, Accessibility checks, CountingIdlingResource
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.2.0")
// Espresso-web for WebView support
    androidTestImplementation("androidx.test.espresso:espresso-web:3.2.0")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.2.0")
// Espresso-intents for validation and stubbing of Intents
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.2.0")

    // The following Espresso dependency can be either "implementation"
    // or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK's compile classpath or the test APK
    // classpath.// Espresso-idling-resource for synchronization with background jobs
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.2.0")
// UiAutomator
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    // Optional -- Hamcrest library
    androidTestImplementation("org.hamcrest:hamcrest-library:1.4-atlassian-1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0") {
        exclude(mapOf("group" to "com.android.support", "module" to "support-annotations"))
    }

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
// Required -- JUnit 4 framework
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.espresso:espresso-core:3.2.0")
    // Optional -- Robolectric environment
    testImplementation("androidx.test:core:1.2.0")
    // Optional -- Mockito framework
    testImplementation("org.mockito:mockito-core:3.3.3")
// AndroidJUnitRunner and JUnit Rules
    testImplementation("androidx.test:runner:1.2.0")
    testImplementation("androidx.test:rules:1.2.0")
/*
    testImplementation("androidx.multidex:multidex-instrumentation:2.0.0") {
        exclude(mapOf("group" to "com.android.support", "module" to "multidex"))
    }
*/
//    testImplementation("androidx.test.ext:junit:1.1.1")
//    testImplementation("androidx.arch.core:core-testing:2.1.0")
//    testImplementation("androidx.annotation:annotation:1.1.0")
    testImplementation("org.json:json:20260814")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.2.1")


    debugImplementation("com.facebook.stetho:stetho:1.6.0")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:1.6.0")
    debugImplementation("com.facebook.stetho:stetho-urlconnection:1.6.0")
    debugImplementation("com.facebook.stetho:stetho-js-rhino:1.6.0")

//    implementation ("com.android.support:support-core-utils:28.0.0")
    // Set this dependency to use JUnit 4 rules
//    androidTestImplementation "com.android.support.test:rules:0.6-alpha"
    // Set this dependency to build and run Espresso tests
//    androidTestImplementation "com.android.support.test.espresso:espresso-core:2.3-alpha"
    // Set this dependency to build and run UI Automator tests
//    androidTestImplementation "com.android.support.test.uiautomator:uiautomator-v18:2.1.2"
//    Junit framework testcase support libraries
//    androidTestImplementation ("com.android.support.test:runner:1.0.2")

//    implementation "com.google.android.gms:play-services:8.4.0"
//    implementation "com.google.android.gms:play-services:9.0.2"
//    implementation "com.google.android.gms:play-services-gcm:9.0.2"
    //    implementation "com.amazonaws:aws-android-sdk-core:2.2.14"
//    implementation "com.amazonaws:aws-android-sdk-cognito:2.2.14"
//    implementation "com.amazonaws:aws-android-sdk-s3:2.2.14"
//    implementation "com.amazonaws:aws-android-sdk-ddb:2.2.14"
//    annotationProcessor "intellibitz.intellidroid:1.0"
//    implementation("androidx.core:core-ktx:+")
    //    AWS SDK
    //    the following android support requires minsdkversion 13 or higher.. currently 11
//    implementation "com.android.support:support-v13:"+23.2.1
//    implementation "com.android.support:preference-v14:"+23.2.1
//    implementation "com.android.support:customtabs:"+23.2.1
/*
    implementation("io.socket:engine.io-client:0.7.0") {
        // excluding org.json which is provided by Android
        exclude group: "org.json", module: "json"
    }
    implementation "com.mcxiaoke.volley:library:1.0.19"
    implementation "com.squareup.picasso:picasso:2.5.2"
    implementation "com.squareup.retrofit2:retrofit:2.0.0-beta3"
    implementation "com.squareup.okio:okio:1.6.0"
    implementation "com.squareup.okhttp3:okhttp:3.0.1"
    implementation "com.squareup.okhttp3:okhttp-ws:3.0.1"
*/

}

