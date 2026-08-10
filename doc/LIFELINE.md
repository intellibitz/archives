# Blukit: Project Lifeline

This document serves as the authoritative source for the **Blukit** project's history, architectural decisions, and future roadmap. It is a living document that tracks the soul and evolution of the project.

---

## 🌟 Project Essence

**Blukit** is a modern Android framework and toolkit designed to simplify and standardize Bluetooth-based identity exchange and data transfer. Our goal is to provide a "plug-and-play" experience for developers needing secure, reliable, and standardized proximity-based interactions.

---

## 🏗️ Architectural Backbone

Blukit follows the **Clean Architecture** principles, adapted for a modern Android/Compose environment:

### 1. UI Layer (Jetpack Compose)
- **State-Driven**: UI reacts to `StateFlow` emitted by ViewModels.
- **Navigation 3**: Utilizing the latest Jetpack Navigation 3 for a declarative, type-safe navigation experience.
- **Material 3**: Themed using Material Design 3 for a modern look and feel.

### 2. Domain Layer
- **Controllers**: Interfaces like `BluetoothController` define the capabilities without being tied to specific implementations.
- **Models**: Domain models (e.g., `BluetoothDeviceDomain`, `BluetoothMessage`) encapsulate the business logic.

### 3. Data Layer
- **Repositories**: `IdentityRepository` manages user identity and local state via Encrypted DataStore.
- **Implementations**: `NearbyP2PController` utilizes the **Google Nearby Connections API** for robust, multi-point decentralized communication.
- **Persistence**: **Room Database** caches messages and contacts with automated TTL purging.
- **Security**: `CryptoManager` provides AES-256-GCM encryption leveraging the Android Keystore.

---

## 🛠️ Technical Keypoints

### Bluetooth & P2P Mesh
- **Production Engine**: Transitioned from manual RFCOMM sockets to the **Google Nearby Connections API** (`P2P_CLUSTER`) for superior reliability and automatic radio switching (BLE, Classic, Wi-Fi).
- **Smart Radar**: Interactive UI for peer discovery using RSSI metrics and animated visual feedback.
- **Privacy-First Discovery**: Uses `neverForLocation` permission flags to protect user data.

### Build System & Portability
- **Self-Contained**: The project uses Gradle Toolchains (Java 17) and `android.auto-download=true` to ensure zero-config builds on new machines.
- **CI/CD**: A GitHub Actions workflow (`android.yml`) validates every commit against a clean build environment.

---

## 📜 Decision Log (The "Why")

| Date | Decision | Rationale |
| :--- | :--- | :--- |
| 2026-08-10 | **Git Readiness** | Adopted Google Standards (README, LICENSE, etc.) to ensure the project is ready for open-source contribution and follows industry best practices. |
| 2026-08-10 | **Java 17 / AGP 8.x** | Standardized on Java 17 to leverage modern Kotlin features and stay aligned with the latest Android Gradle Plugin requirements. |
| 2026-08-10 | **Navigation 3** | Chose Navigation 3 (Alpha) to future-proof the app's navigation logic and benefit from better State/ViewModel integration. |
| 2026-08-10 | **JDK Toolchain Alignment** | Removed forced `jbr-25` dependency in favor of a stable, external JDK 17 toolchain for better Android/Kotlin compatibility. |
| 2026-08-10 | **Memory Optimization** | Increased Gradle/Kotlin heap limits to 8GB/4GB to ensure build stability and AI responsiveness during development. |
| 2026-08-10 | **Decentralized Core** | Implemented Room persistence, AES-256 encryption via Keystore, and Kotlin Serialization for a high-performance, secure P2P chat engine. |
| 2026-08-10 | **Production Engine** | Integrated Google Nearby Connections API (P2P_CLUSTER), implemented Visual Radar, OLED Stealth Mode, and Automated TTL Purge. |
| 2026-08-10 | **Phase 5 Hardening** | Refined privacy permissions, implemented reactive Radio State Monitoring, optimized Radar animation battery usage, and finalized Data Safety declarations. |
| 2026-08-10 | **Production Release** | Generated production signing keys, established secure `keystore.properties` workflow, and verified signed Android App Bundle (AAB) generation. |
| 2026-08-10 | **Compliance & Presence** | Finalized Play Store metadata, Privacy Policy, and implemented UGC moderation (Block/Mute) features. Established a dedicated screenshot generation system via Compose Previews. |
| 2026-08-10 | **Remote Sync** | Successfully synchronized the production-ready codebase and comprehensive documentation with the remote GitHub repository at `intellibitz/blukit`. |

---

## ⏳ Historical Timeline

- **Phase 0: The Spark**: Project initialization with core Bluetooth and Identity classes.
- **Phase 1: Foundation**: Established the MVVM architecture and basic UI screens (Discovery, Chat, Profile).
- **Phase 2: Standardization**:
    - Initialized Git and renamed default branch to `main`.
    - Added `LICENSE`, `README`, `CONTRIBUTING`, and `CODE_OF_CONDUCT`.
    - Configured a robust `.gitignore`.
- **Phase 3: Portability**:
    - Integrated GitHub Actions CI.
    - Enabled Gradle auto-SDK download.
    - Updated to Java 17 toolchains.
- **Phase 4: Advanced Persistence**: Established Room database, AES-256 encryption, and the Automated TTL Purge engine.
- **Phase 5: Production Readiness (Current)**:
    - Integrated Google Nearby Connections API.
    - Implemented Visual Radar and Stealth Theater Mode.
    - Refined privacy permissions and reactive Radio State Monitoring.
    - Finalized `doc/LIFELINE.md` and `doc/DATA_SAFETY.md`.

---

## ⚡ Resource Allocation

To ensure peak performance for the IDE, Build System, and AI Agent, the following resource allocations are recommended:

### 1. Gradle & Kotlin (Project Level)
Configured in `gradle.properties`:
- **Gradle Daemon**: 8GB Heap (`-Xmx8g`), 2GB Min (`-Xms2g`).
- **Kotlin Daemon**: 4GB Heap (`-Xmx4g`).
- **Optimization**: Parallel Garbage Collection enabled.

### 2. IDE & AI Agent (Manual Adjustment)
The AI agent shares memory with the IDE. To increase allocation:
1. Go to **Help > Change Memory Settings**.
2. Set the **Maximum Heap Size** to at least **8GB** (12GB recommended for large projects).
3. Restart Android Studio.
This ensures the AI has enough headroom to process large context windows and the IDE remains responsive during background indexing.

---

## 🚀 Roadmap (Future Aspirations)

- [ ] **Multi-Platform Support**: Explore Kotlin Multiplatform (KMP) for sharing the domain and data logic with iOS.
- [ ] **File & Image Exchange**: Leverage the existing encrypted payload structure for binary data transfers.
- [ ] **Extended Mesh Range**: Implement multi-hop message routing to cover larger venues.
- [ ] **Unit Test Coverage**: Expand coverage to 90%+ for the P2P networking logic.
