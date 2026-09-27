# Android Mobile Applications Suite (`ui/android/`)

A comprehensive suite of Android mobile applications, client-server architectures, and device automation tooling within the **IntelliBitz** ecosystem.

All active projects are modernized to **Android SDK 35**, **Kotlin 2.0.21**, **Gradle 8.13**, and **Android Gradle Plugin (AGP) 8.7.2**, using Gradle Kotlin DSL (`build.gradle.kts`).

---

## Architecture & Project Matrix

| Project                                                                                                | Type / Role                 | Stack & Runtimes                                          | Status                   | Documentation                                                                                                            |
| :----------------------------------------------------------------------------------------------------- | :-------------------------- | :-------------------------------------------------------- | :----------------------- | :----------------------------------------------------------------------------------------------------------------------- |
| **[IntelliDroid](./ui/android/IntelliDroid)**   | Flagship Enterprise Client  | Kotlin 2.0, SDK 35, Coroutines, Room, Firebase, Socket.IO | Active / Modernized      | [`IntelliDroid/README.md`](./ui/android/IntelliDroid/README.md)   |
| **[MEvents](./ui/android/MEvents)**             | Mobile Events & Alerts      | Kotlin 2.0, SDK 35, Gradle Kotlin DSL, HttpManager        | Modernized (100% Kotlin) | [`MEvents/README.md`](./ui/android/MEvents/README.md)             |
| **[TwRends](./ui/android/TwRends)**             | Trending Topics Tracker     | Kotlin 2.0, SDK 35, Gradle Kotlin DSL, JSON parser        | Modernized (100% Kotlin) | [`TwRends/README.md`](./ui/android/TwRends/README.md)             |
| **[UDigg](./ui/android/UDigg)**                 | Social Content Discovery    | Kotlin 2.0, SDK 35, Gradle Kotlin DSL, Digg API           | Modernized (100% Kotlin) | [`UDigg/README.md`](./ui/android/UDigg/README.md)                 |
| **[wuffittracker](./ui/android/wuffittracker)** | GPS Navigation & Tracking   | Kotlin 2.0, SDK 35, SMS Handler, Content Provider         | Modernized (100% Kotlin) | [`wuffittracker/README.md`](./ui/android/wuffittracker/README.md) |
| **[booksExchange](./ui/android/booksExchange)** | P2P Book Discovery & Cloud  | Android client (`bex-droid`), ZXing scanner, GAE server   | Reference Architecture   | [`booksExchange/README.md`](./ui/android/booksExchange/README.md) |
| **[fiteclub](./ui/android/fiteclub)**           | Social Martial Arts Network | Android client + Google App Engine Django backend         | Reference Architecture   | [`fiteclub/README.md`](./ui/android/fiteclub/README.md)           |
| **[mobeegal](./ui/android/mobeegal)**           | Local Search & Dating       | Smack XMPP messaging, GPS mock provider, Web portal       | Reference Architecture   | [`mobeegal/README.md`](./ui/android/mobeegal/README.md)           |

---

## Directory Organization

```
ui/android/
├── IntelliDroid/       # Flagship enterprise messaging & collaboration application
│   ├── app/            # Main application module (multi-flavor: dev, qa, uat, prod)
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── MEvents/            # Mobile events, alerts, and calendar tracking client
│   ├── src/main/kotlin/com/griffingroup/mevents/
│   └── build.gradle.kts
├── TwRends/            # Real-time Twitter trend aggregation and visualization
│   ├── src/main/kotlin/com/griffingroup/twrends/
│   └── build.gradle.kts
├── UDigg/              # Social bookmarking and Digg content reader
│   ├── src/main/kotlin/com/griffingroup/udigg/
│   └── build.gradle.kts
├── wuffittracker/      # GPS navigation, SMS location listener, and instrumentation tests
│   ├── src/main/kotlin/com/androidworks/navsys/wuffit/
│   ├── src/androidTest/kotlin/
│   └── build.gradle.kts
├── booksExchange/      # Book exchange application (`bex-droid` client + `bex-engine` GAE server)
├── fiteclub/           # Social sparring network (Android client + GAE Django server)
├── mobeegal/           # Proximity search client (Smack XMPP) and web portal (`mobeegal.in`)
├── adb-pull-alldata-device.sh   # Extracts databases & app data from connected devices
├── adb-push-alldata-device.sh   # Restores app data and assets to device storage
├── install-kvm.sh               # Configures Linux KVM acceleration for Android Emulator
└── local.properties.example     # SDK location configuration template
```

---

## Modern Build & Test Commands

### 1. Flagship Application ([IntelliDroid](./ui/android/IntelliDroid))

```bash
cd ui/android/IntelliDroid

# Assemble debug builds across environments
./gradlew assembleDevDebug
./gradlew assembleProdRelease

# Run unit tests
./gradlew testDevDebugUnitTest
```

### 2. Standalone Modernized Applications

Each project features a dedicated Gradle Wrapper configured for Gradle 8.13 and JDK 17:

```bash
# MEvents: Build & verify
cd ui/android/MEvents
./gradlew assembleDebug

# TwRends: Build & verify
cd ../TwRends
./gradlew assembleDebug

# UDigg: Build & verify
cd ../UDigg
./gradlew assembleDebug

# wuffittracker: Build APK & test APK
cd ../wuffittracker
./gradlew assembleDebug assembleDebugAndroidTest
```

---

## Device & Emulator Utilities

- **[`local.properties.example`](./ui/android/local.properties.example)**: Copy to `local.properties` in any project directory to set your local Android SDK path:
  ```properties
  sdk.dir=/home/username/Android/Sdk
  ```
- **[`install-kvm.sh`](./ui/android/install-kvm.sh)**: Sets up Kernel-based Virtual Machine (KVM) hardware acceleration permissions on Linux hosts to maximize emulator performance.
- **[`adb-pull-alldata-device.sh`](./ui/android/adb-pull-alldata-device.sh)**: Shell script for dumping SQLite databases, shared preferences, and cache files from `/data/data/<package>/` directly to local storage for debugging.
- **[`adb-push-alldata-device.sh`](./ui/android/adb-push-alldata-device.sh)**: Restores state databases and assets into active emulator instances.

---

## Licensing

All projects within this directory are part of the IntelliBitz repository and are distributed under the authoritative root **[MIT License](./LICENSE)**.
