# Blukit Decision Log & Project Lifeline

| Date | Milestone | Technical Context / Decision |
| :--- | :--- | :--- |
| 2026-08-09 | **Project Birth** | Initialized as "blukit", a decentralized offline P2P chat. |
| 2026-08-10 | **Core Architecture** | Selected Navigation 3, Material 3 Adaptive, and Google Nearby Connections API. |
| 2026-08-10 | **Security Hardening** | Implemented AES-256-GCM encryption with Android Keystore + ECDH key exchange. |
| 2026-08-10 | **Persistence & Purge** | Established Room DB with automated 12-hour TTL purge via WorkManager. |
| 2026-08-10 | **Compliance & Presence** | Finalized Play Store metadata, Privacy Policy, and UGC moderation (Report/Block). |
| 2026-08-11 | **Global i18n Expansion** | Added support for 18+ languages including Tamil, Hindi, Japanese, and Arabic. |
| 2026-08-11 | **Production Hardening** | Enabled R8 fullMode, resource shrinking, and native debug symbols. |
| 2026-08-11 | **Release 1.0.0 (v6)** | First release uploaded to Play Console Closed Testing. |
| 2026-08-11 | **Critical P2P Fix (v7)** | Resolved critical discovery failures on Android 13+; added `NEARBY_WIFI_DEVICES` support. |
| 2026-08-11 | **UX Gold Standard (v8)** | Aligned with Android 15 Edge-to-Edge standards and implemented PiP multitasking. |
| 2026-08-11 | **Zero-Warning Alignment** | Standardized NDK strategy to `FULL` and explicit R8 rule packaging for Version Code 10. |
| 2026-08-11 | **Roadmap Implementation** | Completed implementation of all roadmap phases: Stadium Lobby, Contact Management, and HKDF Security. |
| 2026-08-12 | **Commandments Enforcement** | Enforced core principles: Bluetooth-only by default, optional/silent WiFi and Location, hardened anonymity. |
| 2026-08-12 | **Power Implementation** | Finalized "POWERS": Stadium Lobby as the primary landing hub, decoupled chat from connection status, and reinforced public information dissemination. |
| 2026-08-12 | **Human-Centric Release (v12)** | Production release drop of Version 1.0.6. Fully jargon-free UI, autonomous communal hub (The Square), and 100% test pass rate across 30+ tests. |
| 2026-08-12 | **Hardware Verification** | Successfully validated decentralized mesh on Moto G82 and Xiaomi physical devices. Hardened handshake retries, autonomous history sync, and P2P delivery ACKs. |
| 2026-08-12 | **API & UX Hardening** | Refactored deprecated Room and BroadcastReceiver APIs. Enforced smart AI-powered mesh flows and aligned with the "Blukit Powers" (Watch, Whisper, Shout). |

## 🔗 Architectural Backbone
- **P2P Engine**: `Nearby Connections API` (`P2P_STAR` strategy).
- **Cryptography**: `AES-256-GCM` (authenticated hardware-backed encryption).
- **UX Hub**: `The Square` (Autonomous Landing Hub).
