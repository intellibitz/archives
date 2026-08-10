# Blukit

**Blukit** is a world-class, decentralized, privacy-first offline messaging application. It is designed for instant, local peer-to-peer (P2P) communication in high-density environments where internet connectivity is non-existent or unreliable—such as stadiums, movie theaters, transit hubs, and crowded venues.

---

## 🌟 Key Capabilities

- **Advanced P2P Mesh**: Powered by the **Google Nearby Connections API** (`P2P_CLUSTER`), Blukit creates a robust local mesh network, intelligently switching between Bluetooth LE, Bluetooth Classic, and Wi-Fi Direct for optimal reliability.
- **Hardware-Backed Security**: Every message is protected with end-to-end **AES-256-GCM** encryption established via Diffie-Hellman (ECDH) key exchange, leveraging the **Android Keystore System**.
- **Smart Visual Radar**: An intuitive, animated discovery interface that renders nearby peers as interactive nodes based on proximity and signal strength.
- **Stealth Theater Mode**: A dedicated, OLED-optimized pitch-black theme with high-contrast accents and haptic-only alerts, enabling discrete communication in dark environments.
- **Privacy & Ephemerality**: Blukit follows a "Zero Data Collection" policy. All messages are stored strictly in a local **Room Database** and are automatically purged every 12 hours via an integrated TTL engine.
- **Zero-Friction Onboarding**: No accounts, passwords, or phone numbers required. Users simply pick a nickname and emoji avatar to start chatting instantly.

---

## 🛠️ Technical Stack

- **Language**: Kotlin 2.x
- **UI**: Jetpack Compose + Material 3 Adaptive (supporting phones, tablets, and foldables)
- **Navigation**: Jetpack Navigation 3 (State-driven, type-safe)
- **Networking**: Google Nearby Connections API
- **Persistence**: Room Database + Encrypted DataStore
- **Concurrency**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager (for automated data purging)

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or newer installed on your system.
- The Android SDK and Gradle are automatically handled by the **Gradle Wrapper**.

### Installation

1. **Clone the repository**:
   ```bash
   git clone git@github.com:intellibitz/blukit.git
   ```
2. **Navigate to the project directory**:
   ```bash
   cd blukit
   ```
3. **Build and Install**:
   Use the Gradle Wrapper to download all necessary tools and build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🏗️ Architecture

Blukit follows the **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** pattern. The project is 100% self-contained, ensuring consistent builds across any OS or IDE.

For a deep dive into the project's evolution and architectural decisions, please refer to the **[Project Lifeline](doc/LIFELINE.md)**.

---

## 🛡️ Data Safety & Compliance

Blukit is built with production standards in mind and is fully compliant with Google Play Store standards.
- **[Data Safety Declaration](doc/DATA_SAFETY.md)**
- **[Privacy Policy](doc/PRIVACY_POLICY.md)**

---

## 🤝 Contributing

We welcome contributions to Blukit! Please read our **[CONTRIBUTING.md](CONTRIBUTING.md)** for details on our code of conduct and the process for submitting pull requests.

---

## ⚖️ License

This project is licensed under the **Apache License 2.0**. See the **[LICENSE](LICENSE)** file for the full text.

---
*Built with ❤️ by [Intellibitz](https://github.com/intellibitz)*
*Support: [muthu.ramadoss@gmail.com](mailto:muthu.ramadoss@gmail.com)*
