# Blukit Testing Strategy & Report

**Date:** August 12, 2026  
**Status:** Comprehensive Test Coverage Implemented  
**Package:** `cc.thevar.blukit`

---

## 1. Testing Philosophy
Blukit follows a "Reliability First" philosophy, ensuring that decentralized, offline communication remains stable across diverse Android hardware. We utilize a multi-layered testing pyramid to verify core business logic, security protocols, and UI integrity.

## 2. Test Coverage Summary

| Test Type | Count | Focus Areas | Tools Used |
|-----------|-------|-------------|------------|
| **Unit Tests** | 13 | ViewModels, Mappers, P2P Controller Logic | JUnit 4, MockK, Turbine, Robolectric |
| **Instrumented Tests** | 15 | Database, Repositories, Hardware-Backed Crypto, UI | AndroidX Test, Room In-Memory, Compose Test |
| **Total Passed** | **28** | — | — |

---

## 3. Detailed Test Breakdown

### 3.1 Data Layer (Instrumented)
- **Room Database (`ChatDatabaseTest`)**: Verifies message insertion, ordering, and the automated 12-hour TTL purge logic.
- **Identity Repository (`IdentityRepositoryTest`)**: Ensures persistent user profiles and device IDs are correctly stored in hardware-backed `EncryptedSharedPreferences`.
- **Contact Repository (`ContactRepositoryTest`)**: Verifies peer persistence and last-seen tracking.

### 3.2 Logic & Networking (Unit + Robolectric)
- **ViewModels (`MainViewModelTest`, `BluetoothViewModelTest`)**: Verifies UI state management, navigation triggers, and proper interaction with repositories.
- **P2P Controller (`NearbyP2PControllerTest`)**: Mocks Google Nearby Connections API to ensure discovery and advertising lifecycles are correctly managed.
- **Mappers (`MappersTest`)**: Validates the conversion between Domain models (`MessagePayload`) and Data entities (`MessageEntity`).

### 3.3 Security & Cryptography (Instrumented)
- **Crypto Manager (`CryptoManagerTest`)**: Verifies the end-to-end encryption pipeline.
    - ✅ SecP256r1 KeyPair generation via Android Keystore.
    - ✅ ECDH Shared Secret derivation.
    - ✅ AES-256-GCM authenticated encryption/decryption with tag validation.

### 3.4 User Interface (Compose Test)
- **Onboarding Flow (`ProfileScreenTest`)**: Ensures nickname validation and "Start Exploring" logic.
- **Messaging Experience (`ChatScreenTest`, `LobbyScreenTest`)**: Verifies message rendering, multi-peer identity display, and UI state consistency.

---

## 4. How to Run Tests

### Local Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests (on device/emulator)
```bash
./gradlew connectedDebugAndroidTest
```

---

*Blukit is now verified and hardened for global production testing.* 🛡️
