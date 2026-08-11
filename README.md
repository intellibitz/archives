# Blukit — Decentralized Offline Mesh Chat

**A privacy-first, zero-infrastructure P2P messaging app for high-density venues where internet fails.**

---

## 🌟 What is Blukit?

Blukit (`cc.thevar.blukit`) enables instant local peer-to-peer communication in stadiums, movie theaters, concert venues, transit hubs, and crowded malls — **without any internet, cellular, or Wi-Fi connectivity**. It uses your device's radio hardware (Bluetooth LE + Bluetooth Classic + Wi-Fi Direct) to create a self-organizing mesh network.

### Key Capabilities

| Feature | Status |
|---------|--------|
| 📡 Stadium Lobby (Public Broadcast) | 🔧 In-progress — see Implementation Spec |
| 💬 Direct Whisper (1-on-1 Encrypted Chat) | ✅ Implemented |
| 🎯 Visual Radar (Peer Proximity Display) | ✅ Implemented with radar canvas |
| 🔐 AES-256-GCM + ECDH Encryption | ✅ Android Keystore hardware-backed |
| 🌑 Stealth Theater Mode (OLED pitch-black) | ✅ #000000 background, amber accents |
| ⚡ Haptic Silent Alerts | ✅ Double-pulse vibration waveform |
| 🗑️ 12-hour TTL Auto-Wipe | ✅ WorkManager PurgeWorker |
| 🛡️ Report & Block Moderation | ✅ Long-press on any message bubble |
| 🌍 Global i18n (18+ languages) | ✅ en/es/fr/zh/ja/ru/de/ar/hi/ta/te/kn/ml/mr/bn/gu/pa/ur |
| ♿ Adaptive Layouts (phone/foldable/tablet) | ✅ Compose M3 NavigationSuiteScaffold |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                               │
│  ┌──────────┬──────────┬───────────┬──────────────────┐    │
│  │Profile   │Radar     │Chat/      │Lobby (In-progress)│    │
│  │Screen    │Screen    │Lobby      │Screen              │    │
│  └──────────┴──────────┴───────────┴──────────────────┘    │
│            Navigation 3 + Material 3 Adaptive               │
├─────────────────────────────────────────────────────────────┤
│                      ViewModel Layer                         │
│  ┌────────────────────┬──────────────────────────────┐      │
│  │ MainViewModel      │ BluetoothViewModel           │      │
│  │ (Profile state)    │ (P2P discovery/chat state)  │      │
│  └────────────────────┴──────────────────────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                     Data / Network Layer                     │
│  ┌──────────┬──────────┬───────────┬──────────────────┐    │
│  │Room DB   │CryptoMgr │P2PCtrl    │IdentityRepo       │    │
│  │(Messages,│(AES-256  │(Nearby    │(EncryptedPrefs)   │    │
│  │Contacts) │GCM+ECDH) │Conn)      │                  │    │
│  └──────────┴──────────┴───────────┴──────────────────┘    │
│              PurgeWorker (12h TTL) + RadioStateManager     │
└─────────────────────────────────────────────────────────────┘
```

### Verified Technical Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin + Compose Compiler | 2.2.10 |
| UI | Jetpack Compose M3 BOM | 2024.09.00 |
| Navigation | Jetpack Navigation 3 | 1.0.0-alpha01 |
| Adaptive Layouts | Compose M3 Adaptive | 1.3.x |
| P2P Engine | Google Nearby Connections API | play-services-nearby 19.0.0 |
| Persistence | Room DB + EncryptedSharedPreferences | 2.7.0 / 1.1.0-alpha06 |
| Security | Android Keystore + AES-256-GCM + ECDH | — |
| Concurrency | Kotlin Coroutines + Flow | 1.10.2 |
| Background Tasks | WorkManager | 2.10.0 |
| Build | Gradle AGP 9.3.1, Java 17 Toolchains | — |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or newer
- Android Studio Ladybug+ (or any IDE with Compose preview support)
- No internet dependency needed to build — Gradle Wrapper handles everything

### Build
```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK (uses keystore.properties)
```

### Run on Device
```bash
./gradlew installDebug  # Installs to connected Android device
```

---

## 📂 Project Structure (Verified Against Codebase)

```
Blukit/
├── app/src/main/java/cc/thevar/blukit/
│   ├── BlukitApplication.kt           ✅ App entry, PurgeWorker
│   ├── MainActivity.kt                ✅ Entry activity, edge-to-edge
│   │
│   ├── data/
│   │   ├── crypto/CryptoManager.kt    ✅ ECDH + AES-256-GCM
│   │   ├── local/
│   │   │   ├── ChatDatabase.kt        ✅ Room DB (3 entities)
│   │   │   ├── dao/MessageDao.kt      ✅ Flow-based queries
│   │   │   ├── dao/PeerDao.kt         ✅ Peer persistence
│   │   │   └── entities/              ✅ Contact, Message, Peer entities
│   │   ├── repository/IdentityRepository.kt  ✅ EncryptedSharedPreferences
│   │   ├── system/
│   │   │   ├── HapticManager.kt       ✅ Vibration alerts
│   │   │   └── RadioStateManager.kt   🔧 Bluetooth API compat fix needed
│   │   └── worker/PurgeWorker.kt      ✅ 12-hour TTL auto-wipe
│   │
│   ├── domain/model/
│   │   ├── ConnectionStatus.kt        ✅ Sealed P2P event types
│   │   ├── MessagePayload.kt          ✅ Serializable message model
│   │   └── P2PDevice.kt              ✅ Discovery data class
│   │
│   ├── network/p2p/
│   │   ├── P2PController.kt           ✅ Interface definition
│   │   └── NearbyP2PController.kt     🔧 Critical fixes needed (see spec)
│   │
│   └── ui/
│       ├── BlukitApp.kt              🔧 DiscoveryScreen wiring fix
│       ├── navigation/Routes.kt       ✅ NavKey sealed interface
│       ├── theme/                     ✅ Colors, Typography, Theme
│       ├── screens/
│       │   ├── ChatScreen.kt         🔧 Peer identity display needed
│       │   ├── DiscoveryScreen.kt    ✅ Permission + radio wrapper
│       │   ├── ProfileScreen.kt      ✅ Complete onboarding flow
│       │   └── RadarScreen.kt        🔧 RSSI-based positioning needed
│       ├── viewmodels/
│       │   ├── BluetoothViewModel.kt 🔧 Lobby broadcast method needed
│       │   ├── MainViewModel.kt      ✅ Profile state management
│       │   └── BluetoothUiState.kt  ✅ UI state data class
│       └── previews/MarketPreviews.kt✅ Play Store asset generation
│
├── app/src/main/res/values/*/strings.xml   ✅ 18+ language translations
├── gradle/libs.versions.toml               ✅ Dependency versions catalog
├── keystore.properties                     🔒 Secret key config (gitignored)
├── blukit-release-key.jks                  🔒 Release keystore file
│
├── doc/                                    📄 Documentation
│   ├── BLUKIT_MASTER_IMPLEMENTATION_SPEC.md  🆕 Merged blueprint + gap fills
│   ├── LIFELINE.md                          📅 Project decision log
│   ├── DATA_SAFETY.md                       🛡️ Play Store compliance
│   ├── PRIVACY_POLICY.md                    🔒 Privacy for app store
│   ├── STORE_PRESENCE.md                    📝 Store listing text/assets
│   └── assets/                              🖼️ Icons, screenshots, feature graphic
├── README.md                                ← This file
├── CONTRIBUTING.md                          🤝 Contribution guidelines
├── CODE_OF_CONDUCT.md                       🤝 Code of conduct
└── LICENSE (Apache 2.0)                     ⚖️ License
```

---

## 🔧 Current Implementation Status & Known Gaps

### ✅ Implemented (Production-Ready Features)
- Zero-friction nickname + emoji onboarding (encrypted locally)
- Radar screen with animated concentric circles and peer nodes
- 1-on-1 encrypted chat via Google Nearby Connections P2P_CLUSTER
- AES-256-GCM encryption with Android Keystore ECDH key exchange
- Stealth Theater Mode (OLED #000000, amber accents)
- Haptic silent message alerts (double-pulse waveform)
- 12-hour TTL automatic chat purge (WorkManager)
- Block/Mute moderation on any received message
- Edge-to-edge, adaptive layouts for all form factors
- PiP (Picture-in-Picture) support for multi-tasking
- Comprehensive i18n across 18+ languages
- Play Store asset generation (previews produce assets)

### 🔧 Pending Gap-Fills (See `doc/BLUKIT_MASTER_IMPLEMENTATION_SPEC.md`)

| Priority | Gap | Effort | Description |
|----------|-----|--------|-------------|
| P0 | Stadium Lobby / Public Broadcast | 6-8 hrs | Core blueprint feature not yet implemented |
| P0 | DiscoveryScreen wiring | 30 min | Radar displayed directly, permission wrapper unused |
| P1 | RSSI-based radar positioning | 2 hrs | Peer positions must use actual proximity data |
| P1 | Chat peer identity display | 1 hr | TopAppBar must show who you're chatting with |
| P1 | GCM auth tag validation in CryptoManager | 1 hr | Security fix: validate decryption tags |
| P2 | Bluetooth API compat (API 31+) | 2 hrs | Fix deprecated BluetoothAdapter usage |
| P2 | Contact management + history UI | 4-5 hrs | Wire contacts table, add clear history button |

---

## 🛡️ Data Safety & Compliance

Blukit collects **zero data** externally. All data stays on-device:

| Declaration | Value |
|------------|-------|
| Data Collected | None |
| Data Shared | None |
| Encryption in Transit | AES-256-GCM + ECDH |
| Data Deletion | 12-hour auto-wipe + manual clear history |
| Target Rating | Everyone (3+) with UGC block/moderation |

Full details: [`doc/DATA_SAFETY.md`](doc/DATA_SAFETY.md) · [`doc/PRIVACY_POLICY.md`](doc/PRIVACY_POLICY.md)

---

## 📦 Play Store Deployment

**Developer Account:** `6848893333714998276`  
**Package:** `cc.thevar.blukit` · **Name:** Blukit - Offline Mesh Chat  

### Steps (see [`doc/BLUKIT_Play_Store_Publishing_Blueprint.md`](.agent/Blukit_Play_Store_Publishing_Blueprint.md))
1. Complete Play Console identity verification
2. Build signed `.aab`: `./gradlew bundleRelease`
3. Upload to **Closed Testing** track (minimum 20 testers, 14 days required)
4. Apply for Production access after testing period
5. Staged rollout → Full production release

---

## 🤝 Contributing

We welcome contributions! See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

### Recommended First Tasks
1. Implement Stadium Lobby screen (see Implementation Spec §5, Phase P2)
2. Fix DiscoveryScreen navigation wiring (§4 Gap #2)
3. Fix CryptoManager GCM auth tag (§6 Vulnerability S-01)
4. Add Contact management UI (§5, Phase P4)

---

## 📜 License

Apache License 2.0 — See [`LICENSE`](LICENSE) for details.

---

*Built with ❤️ by [Intellibitz](https://github.com/intellibitz)*  
*Support: muthu.ramadoss@gmail.com*
