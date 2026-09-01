# Blukit Architecture 🛡️🌑

This document outlines the decentralized, modular architecture of Blukit.

## 1. High-Level Modularization

Blukit is divided into two primary modules to ensure separation of concerns and prepare for Kotlin Multiplatform (KMP).

### **`:core` (Domain & Data Protocol)**
The foundational module containing the "Soul" of the Blukit protocol.
- **`domain.model`**: Unified data structures (`Message`, `Group`, `Source`) shared across all platforms.
- **`domain.protocol`**: Handshake and frame definitions (`HandshakeProtocol`).
- **`domain.security`**: Session management and cryptographic orchestration (`SecureSessionManager`, `CryptoManager`).
- **`domain.usecase`**: Pure business logic (e.g., `ConsensusUseCase` for voting, `RitualUseCase` for group events).
- **`data.local`**: Persistence layer using **Room 3.0 (KMP)** and DAOs.

### **`:app` (Android Implementation)**
The platform-specific layer handling UI and low-level networking.
- **`network.p2p`**: Implementation of the `ConnectionController` using Google Nearby Connections and native BLE.
- **`ui.viewmodels`**: Coordination layer (`ConnectionViewModel`) using **UDF (Unidirectional Data Flow)**.
- **`ui.navigation`**: Decoupled navigation graph (`BlukitNavGraph`) using **Navigation 3** and **Material 3 Adaptive** APIs.
- **`ui.screens`**: Modular, state-hoisted Composables optimized for multi-pane layouts.

---

## 2. Navigation & Adaptive UI

Blukit implements a modular navigation system designed for a wide range of form factors.

### **Navigation 3 & Scene Strategies**
The app uses a centralized `BlukitNavGraph` which leverages `ListDetailSceneStrategy` from the Material 3 Adaptive library.
- **Multi-Pane**: On large screens (Tablets, Foldables), the "Nearby" list and "Group/Message" details are displayed side-by-side. Includes `DetailPlaceholder` for a polished "unselected" state.
- **Authoritative Backstack**: `NavigationViewModel` owns a `mutableStateListOf<NavKey>`. `BlukitNavGraph` directly consumes this list, ensuring UI and business logic are always in sync without fragile reactive bridging.
- **Onboarding Flow**: Identity creation is handled via a dedicated `Route.Onboarding`, decoupling auth logic from the main Scaffold orchestration.

### **Scaffold Modularization**
The top-level UI is split into:
1. `BlukitScaffold`: Handles `NavigationSuiteScaffold` orchestration (Rail vs Bottom Bar) and top-bar state.
2. `BlukitNavGraph`: Handles route-to-screen mapping and pane composition.

---

## 3. Data Flow & Performance


### **Paging 3 Integration**
Blukit uses **Paging 3** to manage message history.
- **DAO Level**: DAOs return `PagingSource<Int, Message>`, allowing Room to handle windowed SQLite queries.
- **Repository**: Exposes `Flow<PagingData<Message>>`, ensuring that thousand-message threads don't cause memory spikes.
- **UI**: Composables consume `LazyPagingItems`, providing smooth, efficient scrolling even in heavy local streams.

### **Social Logic (LWW-CRDT)**
To maintain a deterministic state across an offline swarm:
1. Every message has a `timestamp` and a `noteVersion`.
2. **Last-Write-Wins (LWW)** logic resolves conflicts (e.g., in `MessageRepository.upsertMessage`).
3. Consensus is achieved via `ConsensusUseCase` by aggregating "Connection Weights" across the swarm.

---

## 3. Security Protocol

Blukit implements a **Hardware-Anchored Security** model:
1. **Identity**: A permanent P-256 EC key pair is generated in the device's **TEE (StrongBox)**.
2. **Establishment**: Handshakes involve exchanging public keys via `HandshakeProtocol`.
3. **Key Agreement**: `CryptoManager` performs ECDH to derive a shared secret, followed by HKDF for key derivation.
4. **Transport**: All mesh traffic is encrypted with **AES-256-GCM**, ensuring both confidentiality and integrity without a central CA.

---

## 4. Hardware Harmony (Autonomous Intelligence)

The `HarmonyManager` and `AssistantManager` work together to optimize device energy:
- **Low Power Mode**: When battery is < 15%, the P2P engine throttles scanning and disables heavy UI animations.
- **Stealth Mode**: Inactivity triggers a broadcast shutdown, moving the node to a "listen-only" state to preserve radio life.
- **Context Synthesis**: Silent local analysis detects "Trends" (Social, Academic, Action) to adjust the UI aura and haptic feedback.

---

## 6. Testing Strategy 🧪

Blukit follows a "Reliability First" approach with a focus on local testing.

### **Unit Tests**
- **Business Logic**: Located in `app/src/test`. Key targets: `NavigationViewModel`, `ConnectionViewModel` (state transitions), and `ConsensusUseCase`.
- **Infrastructure**: Room DAO tests and CRDT resolution logic are verified at the unit level.

### **Verification Commands**
Run all unit tests:
```bash
./gradlew :app:testDebugUnitTest
```
