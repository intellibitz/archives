# MEvents

**MEvents** is a lightweight Android mobile client for tracking and viewing community and personal events.

Modernized to **100% Kotlin 2.0.21**, **Android SDK 35**, **AGP 8.7.2**, and **Gradle 8.13** using the Gradle Kotlin DSL (`build.gradle.kts`).

---

## Architecture & Codebase

```
ui/android/MEvents/
├── build.gradle.kts     # Build configuration (SDK 35, Kotlin 2.0.21, AGP 8.7.2)
├── settings.gradle.kts  # Project root settings
├── gradle/              # Gradle Wrapper 8.13 distribution
└── src/main/
    ├── AndroidManifest.xml
    ├── kotlin/com/griffingroup/mevents/
    │   ├── MEventsActivity.kt # Main activity managing event feed display and user interaction
    │   ├── HttpManager.kt     # HTTP request handling and networking routines
    │   └── IOUtils.kt         # Stream and buffer I/O utility extensions
    └── res/                   # Layouts, drawables, and string resources
```

---

## Build & Test Instructions

```bash
cd ui/android/MEvents

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test
```

The compiled APK will be generated at `build/outputs/apk/debug/MEvents-debug.apk`.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
