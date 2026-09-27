# User Interfaces & Mobile Clients (`ui/`)

This directory encompasses mobile application codebases (primarily Android), web client standards specifications, and documentation authoring tooling.

---

## Directory Overview

```
ui/
├── android/           # Android applications, tools, and test suites
│   ├── IntelliDroid/  # Primary modern Android client project (Gradle)
│   ├── MEvents/       # Mobile events and location tracker
│   ├── TwRends/       # Twitter trends Android client
│   ├── UDigg/         # Social discovery and Digg client
│   ├── wuffittracker/ # GPS tracker and instrumentation suite
│   ├── booksExchange/ # Books exchange mobile client (bex-droid)
│   ├── fiteclub/      # Fiteclub mobile client
│   └── mobeegal/      # Mobile local search client
├── client/            # Web Client Standards & Living Specs
│   ├── README.md      # Standards index and navigation table
│   └── *.md           # HTML, CSS, JS, HTTP/3, WebSockets, Storage, URL specs
└── asciidoc/          # AsciiDoc & Asciidoctor documentation engine guides
```

---

## Projects & Applications

### Mobile Applications Suite: [`ui/android/`](./ui/android/README.md)

Central hub for all Android mobile applications, modernized to **Android SDK 35**, **Kotlin 2.0.21**, **Gradle 8.13**, and **AGP 8.7.2**:

- **[IntelliDroid](./ui/android/IntelliDroid/README.md)**: Flagship enterprise messaging and collaboration client (multi-flavor, Coroutines, Room, Firebase FCM, Socket.IO).
- **[MEvents](./ui/android/MEvents/README.md)**: Mobile event feed and alert tracker (100% Kotlin).
- **[TwRends](./ui/android/TwRends/README.md)**: Real-time Twitter trending topics visualization (100% Kotlin).
- **[UDigg](./ui/android/UDigg/README.md)**: Social bookmarking and Digg content discovery reader (100% Kotlin).
- **[wuffittracker](./ui/android/wuffittracker/README.md)**: GPS location navigation, SMS telemetry, and instrumentation test suite (100% Kotlin).
- **[booksExchange](./ui/android/booksExchange/README.md)**: Barcode/ISBN scanning book exchange (`bex-droid` client + `bex-engine` GAE cloud).
- **[fiteclub](./ui/android/fiteclub/README.md)**: Martial arts sparring network (Android client + GAE Python/Django backend).
- **[mobeegal](./ui/android/mobeegal/README.md)**: Proximity discovery and XMPP instant chat client with web portal.

### Web Client Living Standards: [`ui/client/`](./ui/client/README.md)

Curated documentation and references for modern frontend architecture:

- HTML Living Standard
- CSS Cascading Style Sheets
- ECMAScript / JavaScript Language Specifications
- HTTP/3 and QUIC transport protocol
- WebSockets and XMLHttpRequest

### Android Development Utilities

- **[`adb-pull-alldata-device.sh`](./ui/android/adb-pull-alldata-device.sh)**: Pulls application state and databases from an attached device or emulator.
- **[`adb-push-alldata-device.sh`](./ui/android/adb-push-alldata-device.sh)**: Restores state databases and assets into active emulator instances.
- **[`install-kvm.sh`](./ui/android/install-kvm.sh)**: Configures KVM hardware acceleration for the Android Emulator on Linux.
- **[`local.properties.example`](./ui/android/local.properties.example)**: Configuration template for Android SDK path binding.

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
