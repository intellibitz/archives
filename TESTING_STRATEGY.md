# Blukit Testing Strategy & Report

**Date:** August 15, 2026  
**Status:** 100% Core Test Coverage · Extraordinary & Verified  
**Package:** `cc.thevar.blukit`

---

## 1. Testing Philosophy
Blukit follows a "Reliability First" philosophy, ensuring that decentralized, offline communication remains stable across diverse Android hardware. We utilize a multi-layered testing pyramid to verify core connection links, security protocols, and sentient UI integrity.

## 2. Test Coverage Summary

| Test Type | Count | Focus Areas | Tools Used |
|-----------|-------|-------------|------------|
| **Unit Tests** | 22 | Harmony Logic, Link Rituals, Vibe Mappers | JUnit 4, MockK, Turbine, Robolectric |
| **Instrumented Tests** | 32 | Self-Healing Vault, Hardware Crypto, Hub expansion | AndroidX Test, Room In-Memory, Compose Test |
| **Total Passed** | **54** | — | — |

---

## 3. Detailed Test Breakdown

### 3.1 Unit Tests (Local JVM)
- **MainViewModelTest**: Business logic for user identity and link ritual management.
- **BluetoothViewModelTest**: Reactive UI state transitions for **The Vibes** and **Harmony**.
- **PrinciplesTest**: Verifies adherence to "Powers" (e.g., bi-directional link ritual).

### 3.2 Integration & Data Layer (Instrumented)
- **Room Database**: Verifies message insertion and the automated 12-hour TTL purge logic.
- **Identity Repository**: Ensures the **Self-Healing Vault** correctly recovers from Keystore corruption on devices like OnePlus.

### 3.3 Security & Cryptography (Instrumented)
- **Crypto Manager**: Verifies the end-to-end encryption pipeline.
    - ✅ SecP256r1 Hardware KeyPair generation.
    - ✅ ECDH Shared Secret derivation.
    - ✅ AES-256-GCM tag validation for Vibes and Ties.

### 3.4 Functional & UI (Compose Test)
- **FlowsTest**: Verifies **"Link Ritual"** flow (ACCEPT/DENY from Magic Bar), landing on **The Vibes**, and identity customization.
- **NavigationTest**: Verifies that branding expansion and intel stats correctly switch hub modes.
- **CommandmentsTest**: Ensures no invasive permissions and verifies "The Vibes" terminology purity.

---

## 4. Hardware Verification (4-Device Fleet)
All core flows (Sending vibes, requesting links, accepting rituals) have been manually verified across:
- **Google Pixel 10 Pro XL** (Master)
- **OnePlus CPH2747** (Self-Healing confirmed)
- **Motorola Moto G82** (Linked)
- **Xiaomi 24115RA8EI** (Linked)

---

## 5. How to Run Tests

### Local Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

### Instrumented Tests (on fleet)
```bash
./gradlew :app:connectedDebugAndroidTest
```

---

*Blukit is now hardened for high-density stadium testing.* 🛡️🫂
