# WuffIT Tracker

**WuffIT Tracker** is an Android GPS navigation, field tracking, and SMS-coordinated location telemetry application.

Modernized to **100% Kotlin 2.0.21**, **Android SDK 35**, **AGP 8.7.2**, and **Gradle 8.13** using the Gradle Kotlin DSL (`build.gradle.kts`).

---

## Architecture & Codebase

```
ui/android/wuffittracker/
├── build.gradle.kts     # Build configuration (SDK 35, Kotlin 2.0.21, AGP 8.7.2)
├── settings.gradle.kts  # Project root settings
├── gradle/              # Gradle Wrapper 8.13 distribution
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── kotlin/com/androidworks/navsys/wuffit/
    │   │   ├── WuffITApplication.kt         # Custom Application context initialization
    │   │   ├── activity/
    │   │   │   ├── WuffITTracker.kt         # Primary tracking dashboard and map view
    │   │   │   ├── DisplayLocation.kt       # Renders coordinates, address, and pinpoint
    │   │   │   ├── RequestLocation.kt       # Sends location requests via SMS dispatch
    │   │   │   ├── SelectTracker.kt         # Manages multi-target tracking list
    │   │   │   └── SetupWuffIT.kt           # Device credentials and setup wizard
    │   │   ├── content/
    │   │   │   ├── Tracker.kt               # Content model and contract constants
    │   │   │   ├── TrackerProvider.kt       # SQLite ContentProvider for waypoint storage
    │   │   │   └── WuffITSMSReceiver.kt     # BroadcastReceiver for inbound GPS SMS alerts
    │   │   └── service/
    │   │       └── SMSHandler.kt            # Background SMS encoding and parser
    │   └── res/                             # Layouts, item cards, drawables, and strings
    └── androidTest/                         # On-device instrumentation test suite
        └── kotlin/com/androidworks/navsys/wuffit/
            ├── WuffITApplicationTest.kt     # Application context test
            └── activity/
                └── WuffITTrackerTest.kt     # Activity lifecycle & provider validation
```

---

## Technical Highlights

- **Content Provider Pattern**: Implements `TrackerProvider` to persist waypoints, timestamps, and tracker status in a structured SQLite database accessible via `content://` URIs.
- **SMS Telemetry Protocol**: Out-of-band location exchange using encoded SMS messages, enabling device tracking even in areas without mobile data coverage.
- **Maps Abstraction**: Uses clean map stubs (`MapsStubs.kt`) ensuring portability and compile-time compatibility with modern Android SDK 35 runtimes.
- **Instrumentation Testing**: Validated with AndroidX test runners and Kotlin test libraries (`assembleDebugAndroidTest`).

---

## Build & Test Instructions

```bash
cd ui/android/wuffittracker

# Build Debug APK
./gradlew assembleDebug

# Build Instrumentation Test APK
./gradlew assembleDebugAndroidTest

# Run unit tests on JVM
./gradlew test

# Run connected instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
