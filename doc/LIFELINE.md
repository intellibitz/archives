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
| 2026-08-11 | **Critical P2P Fix (v7)** | Resolved a production issue where discovery failed on Android 13+ due to missing `NEARBY_WIFI_DEVICES` permission and strict Location requirements. Hardened `NearbyP2PController` with failure listeners. |

## 🔗 Architectural Backbone
- **P2P Engine**: `Nearby Connections API` (`P2P_CLUSTER` strategy).
- **Cryptography**: `AES-256-GCM` (authenticated encryption).
- **UI State**: `Kotlin Flow` + `StateFlow` (reactive streams).
- **Compliance**: `Report & Block` + `Auto-Purge` (UGC moderation).
