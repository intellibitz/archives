# Blukit — Decentralized Offline Mesh Chat

**A privacy-first, zero-infrastructure P2P messaging app for high-density venues where internet fails.**

---

## 🌟 What is Blukit?

Blukit (`cc.thevar.blukit`) is an AI-powered smart mesh chat that enables instant local peer-to-peer communication in stadiums, movie theaters, and crowded malls — **without any internet, cellular, or Wi-Fi connectivity**.

### Key Capabilities

| Feature | Status |
|---------|--------|
| 📡 The Square (Public Hub) | ✅ Autonomous Landing Hub |
| 💬 Direct Whisper (Private) | ✅ Smart AI-powered Flow |
| 🔄 Mesh History Sync | ✅ 20-message auto-recovery |
| 📩 Delivery Badges (✓✓) | ✅ Real-time P2P ACK |
| 🎯 Visual Radar (Real RSSI Proximity Display) | ✅ Implemented |
| 🔐 AES-256-GCM + ECDH + HKDF Encryption | ✅ Hardened hardware-backed |
| 🌑 Stealth Theater Mode (OLED pitch-black) | ✅ #000000 background, amber accents |
| ⚡ Haptic Silent Alerts | ✅ Double-pulse vibration waveform |
| 🗑️ 12-hour TTL Auto-Wipe | ✅ WorkManager PurgeWorker |
| 🛡️ Report & Block Moderation | ✅ Proactive spam protection |
| 🌍 Global i18n (18+ languages) | ✅ Worldwide readiness |
| ♿ Adaptive Layouts (phone/foldable/tablet) | ✅ Android 15 Edge-to-Edge compliant |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                               │
│  ┌──────────┬──────────┬───────────┬──────────────────┐    │
│  │Profile   │Radar     │Chat       │Stadium           │    │
│  │Screen    │Screen    │Screen     │Lobby             │    │
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
│  │Contacts) │GCM+HKDF) │Conn)      │                  │    │
│  └──────────┴──────────┴───────────┴──────────────────┘    │
│              PurgeWorker (12h TTL) + RadioStateManager     │
└─────────────────────────────────────────────────────────────┘
```

### Verified Technical Stack

| Layer | Technology | Version | Verified |
|-------|-----------|---------|----------|
| Language | Kotlin + Compose Compiler | 2.2.10 | ✅ |
| Build System | AGP 9.3.1 | — | ✅ |
| UI | Jetpack Compose M3 BOM | 2024.09.00 | ✅ |
| Navigation | Jetpack Navigation 3 | 1.0.0-alpha01 | ✅ |
| Adaptive Layouts | Compose M3 Adaptive | 1.3.1 | ✅ |
| P2P Engine | Google Nearby Connections API | 19.0.0 | ✅ |
| Persistence | Room Database | 2.7.0 | ✅ |
| Security | AES-256-GCM + ECDH + HKDF | — | ✅ |
| Platform | Android 15 (API 35/37) | — | ✅ |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or newer
- Android Studio Ladybug+
- Physical Android devices (API 26+) for P2P testing

### Build & Run
```bash
./gradlew assembleRelease  # Generates optimized production AAB
./gradlew installDebug    # Installs to connected device
```

---

## 🛡️ Data Safety & Compliance

Blukit follows the **Blukit Commandments**:
1. **Bluetooth-First**: Mandatory permissions limited to Bluetooth group.
2. **Optional Radios**: WiFi and Location are optional; app handles their absence silently.
3. **Total Anonymity**: No accounts, tracking, or telemetry.
4. **Hardware Security**: All keys and user data are hardware-backed and encrypted.

Full details: [`doc/DATA_SAFETY.md`](doc/DATA_SAFETY.md) · [`doc/PRIVACY_POLICY.md`](doc/PRIVACY_POLICY.md) · [`doc/BLUKIT.md`](doc/BLUKIT.md)

---

## 📜 License

Apache License 2.0 — See [`LICENSE`](LICENSE) for details.

---

*Built with ❤️ by [Intellibitz](https://github.com/intellibitz)*  
*P2P Mesh Messaging for Everyone.*
