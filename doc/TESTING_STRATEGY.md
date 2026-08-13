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

### 3.1 Unit Tests (Local JVM)
Located in `app/src/test/`. These tests run on the JVM using Robolectric when Android dependencies are required.
- **MainViewModelTest**: Business logic for user identity and chat history management.
- **BluetoothViewModelTest**: Reactive UI state transitions for discovery and connectivity.
- **PrinciplesTest**: Verifies that the app adheres to its "Powers" and "Commandments" (e.g., decentralized broadcasts).

### 3.2 Integration & Data Layer (Instrumented)
Located in `app/src/androidTest/`. These tests run on an Android device or emulator.
- **Room Database (`MessageDaoTest`, `PeerDaoTest`, `ContactDaoTest`)**: Verifies message insertion, ordering, and the automated 12-hour TTL purge logic.
- **Identity Repository (`IdentityRepositoryTest`)**: Ensures persistent user profiles and device IDs are correctly stored in hardware-backed `EncryptedSharedPreferences`.

### 3.3 Security & Cryptography (Instrumented)
- **Crypto Manager (`CryptoManagerTest`)**: Verifies the end-to-end encryption pipeline.
    - ✅ SecP256r1 KeyPair generation via Android Keystore.
    - ✅ ECDH Shared Secret derivation.
    - ✅ AES-256-GCM authenticated encryption/decryption with tag validation.

### 3.4 Functional & UI (Compose Test)
- **FlowsTest**: Verifies **"Smart Vibes"** flow (contextual connection requests triggered by first vibes), initial landing on The Air, and persona customization.
- **NavigationTest**: Verifies that all bottom navigation tabs (Air, Ties, Vibe) correctly switch screens.
- **CommandmentsTest**: Ensures no invasive permissions are requested and verified architecture constraints.

---

## 4. How to Run Tests

### Local Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

### Instrumented Tests (on device/emulator)
```bash
./gradlew :app:connectedDebugAndroidTest
```

---

## 5. Maintenance
When adding new features:
1. **Unit Test** the logic in ViewModels or Repositories.
2. **Integration Test** any new database entities or network protocols.
3. **UI Test** the user flow if there's a significant change in navigation or smart flows.

---

*Blukit is now verified and hardened for global production testing.* 🛡️
