# IntelliDroid

**IntelliDroid** is the flagship Android enterprise messaging, collaboration, and multi-service mobile client in the **IntelliBitz** ecosystem.

The application is built on modern Android standards: **Kotlin 2.0.21**, **Android SDK 35**, **AGP 8.7.2**, **Gradle 8.13**, and **Java 17**.

---

## Technical Architecture

```
ui/android/IntelliDroid/
├── app/
│   ├── build.gradle.kts     # Main module build configuration (Gradle Kotlin DSL)
│   ├── keystore.properties  # Release signing configuration template
│   ├── proguard-rules.pro   # ProGuard / R8 code shrinking rules
│   └── src/
│       ├── main/            # Core activities, services, adapters, models, providers
│       ├── test/            # Local JVM unit tests (Robolectric & Mockito)
│       └── androidTest/     # On-device instrumentation & Espresso UI tests
├── build.gradle.kts         # Root build script
├── settings.gradle.kts      # Project module bindings and repository declarations
└── gradle/                  # Gradle Wrapper 8.13 distribution
```

---

## Key Features & Stack

- **Modern Language & Toolchain**: 100% Kotlin Coroutines and Flow (`kotlinx-coroutines-android:1.11.0`), Kotlin Parcelize, KAPT annotation processing.
- **Modern Jetpack Architecture**:
  - **DataBinding & ViewBinding**: Declarative UI layout binding.
  - **Jetpack Navigation**: SafeArgs Kotlin plugin (`androidx.navigation.safeargs.kotlin`).
  - **Room Database**: Local SQLite abstraction and entity caching with Coroutines support.
  - **Lifecycle & ViewModel**: Lifecycle-aware view models, LiveData, and ReactiveStreams.
  - **WorkManager**: Background task scheduling and execution.
- **Enterprise Messaging & Networking**:
  - **Firebase Cloud Messaging (FCM)**: Push notifications and messaging services.
  - **Socket.IO Client**: Real-time bi-directional event-driven websocket connectivity.
  - **Google LibPhoneNumber**: Phone number parsing, formatting, and international validation.
  - **Volley Networking**: Efficient asynchronous HTTP network request queueing.
- **Rich Media & System Integration**:
  - **AndroidX Media2**: Media sessions, video/audio players, and routing.
  - **Google Cast & MediaRouter**: Casting media streams to external devices.
  - **Chrome Custom Tabs**: Secure in-app browser sessions.
- **Diagnostics & Debugging**:
  - **Facebook Stetho**: Chrome Developer Tools inspection of network traffic, SQLite databases, and view hierarchy in debug builds.

---

## Environments & Product Flavors

IntelliDroid uses the `color` flavor dimension to separate deployment stages:

| Flavor | Application ID                 | Target Environment                        |
| :----- | :----------------------------- | :---------------------------------------- |
| `dev`  | `intellibitz.intellidroid.dev` | Local development and mocked backends     |
| `qa`   | `intellibitz.intellidroid.qa`  | Quality assurance and integration testing |
| `uat`  | `intellibitz.intellidroid.uat` | User acceptance testing and staging       |
| `prod` | `intellibitz.intellidroid`     | Production release                        |

---

## Build & Test Instructions

All builds use the included Gradle Wrapper:

```bash
# Navigate to project directory
cd ui/android/IntelliDroid

# Build Debug APK for Development flavor
./gradlew assembleDevDebug

# Build Debug APK for Production flavor
./gradlew assembleProdDebug

# Assemble all release variants (requires keystore.properties for signing)
./gradlew assembleRelease

# Run unit tests on JVM
./gradlew testDevDebugUnitTest

# Run on-device instrumentation tests
./gradlew connectedDevDebugAndroidTest
```

---

## Configuration

To specify the Android SDK path locally, create a `local.properties` file in `ui/android/IntelliDroid/` (or copy from [`ui/android/local.properties.example`](./ui/android/local.properties.example)):

```properties
sdk.dir=/path/to/your/android/sdk
```

For release signing, create a `keystore.properties` in `ui/android/IntelliDroid/app/`:

```properties
storeFile=path/to/keystore.jks
storePassword=yourStorePassword
keyAlias=yourKeyAlias
keyPassword=yourKeyPassword
```

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
