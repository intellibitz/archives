# Blukit — The Vibing Air

**A decentralized, zero-infrastructure offline mesh for high-density venues where internet fails.**

---

## 🌟 What is Blukit?

Blukit (`cc.thevar.blukit`) is an emotive P2P experience. It is the Vibing Air where you can feel the vibes of everyone around you instantly. Ties are the quiet side effects of these shared moments.

### Key Capabilities

| 🌬️ **The Air** | ✅ High-density stadium visualization with integrated vibes ticker |
| 👥 **Your Ties** | ✅ Secure private bonds with real-time vibe reflection |
| 🎭 **Your Vibe** | ✅ Context-aware anonymous identity with scenario moods |
| 📛 **Unified Badge** | ✅ Global top-left anchor for branding and mesh diagnostics |
| 🔐 **Stealth Security** | ✅ AES-256-GCM + ECDH + HKDF hardware-backed encryption |
| 🌑 **OLED Stealth** | ✅ Pitch-black UI with vibing Amber and Rose accents |
| 🗑️ **12h Vanish** | ✅ Automated TTL purge for absolute privacy |
| 📱 **Fireproof Vibe** | ✅ 3-device verified (Pixel, Moto, Xiaomi) · SDK 37 ready |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                               │
│  ┌──────────┬──────────┬───────────┐    Navigation 3         │
│  │The Air   │Your Ties │Your Vibe  │    M3 Adaptive          │
│  └──────────┴──────────┴───────────┘    Unified Badge        │
├─────────────────────────────────────────────────────────────┤
│                      ViewModel Layer                         │
│  ┌────────────────────┬──────────────────────────────┐      │
│  │ MainViewModel      │ BluetoothViewModel           │      │
│  │ (Identity Sync)    │ (Mesh State & Vibes Ticker)  │      │
│  └────────────────────┴──────────────────────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                     Data / Network Layer                     │
│  ┌──────────┬──────────┬───────────┬──────────────────┐    │
│  │Room DB   │CryptoMgr │Composite  │IdentityRepo       │    │
│  │(Storage) │(Security)│P2P Engine │(Encrypted)       │    │
│  └──────────┴──────────┴───────────┴──────────────────┘    │
│            PurgeWorker (12h TTL) + Fireproof RadioManager    │
└─────────────────────────────────────────────────────────────┘
```

### Verified Technical Stack

| Layer | Technology | Status |
|-------|-----------|--------|
| Language | Kotlin 2.2.10 | ✅ |
| UI | Compose OLED Stealth | ✅ |
| Navigation | Navigation 3 | ✅ |
| Mesh | Nearby + Native BLE | ✅ |
| Security | Hardware AES-256 | ✅ |
| Platform | Android 15 (API 37) | ✅ |

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+ & Android Studio Ladybug+
- Physical Android devices for P2P testing

### Build & Run
```bash
./gradlew installDebug  # Vibe into the mesh
```

---

## 🛡️ Data Safety & Compliance

Blukit follows the **Vibing Air Commandments**:
1. **Air Required**: Bluetooth is the only bridge.
2. **Optional Breezes**: WiFi and Location are optional on modern devices.
3. **Total Anonymity**: No tracking, no accounts, just vibes.
4. **Absolute Stealth**: Data vanishes every 12 hours.

Full details: [`doc/BLUKIT.md`](doc/BLUKIT.md) · [`doc/PRIVACY_POLICY.md`](doc/PRIVACY_POLICY.md)

---

*Built with ❤️ by [Intellibitz](https://github.com/intellibitz)*  
*Feel the vibes.*
