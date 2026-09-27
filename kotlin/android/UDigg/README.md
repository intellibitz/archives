# UDigg

**UDigg** is an Android mobile client for social content discovery, story aggregation, and article voting.

Modernized to **100% Kotlin 2.0.21**, **Android SDK 35**, **AGP 8.7.2**, and **Gradle 8.13** using the Gradle Kotlin DSL (`build.gradle.kts`).

---

## Architecture & Codebase

```
ui/android/UDigg/
├── build.gradle.kts     # Build configuration (SDK 35, Kotlin 2.0.21, AGP 8.7.2)
├── settings.gradle.kts  # Project root settings
├── gradle/              # Gradle Wrapper 8.13 distribution
└── src/main/
    ├── AndroidManifest.xml
    ├── kotlin/com/griffingroup/udigg/
    │   ├── UDiggActivity.kt   # Main UI displaying stories, voting, and link navigation
    │   ├── HttpManager.kt     # HTTP client fetching story feeds and metadata
    │   └── IOUtils.kt         # Stream and buffer I/O utility extensions
    └── res/                   # Layouts, drawables, and string resources
```

---

## Build & Test Instructions

```bash
cd ui/android/UDigg

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test
```

The compiled APK will be generated at `build/outputs/apk/debug/UDigg-debug.apk`.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
