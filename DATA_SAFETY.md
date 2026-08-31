# Blukit: Data Safety Protocol

**Blukit is 100% offline, private, and secured by proximity.**

## 1. Zero Cloud Dependency
Blukit **never** connects to the internet. All **Messages** travel directly from phone to phone using peer-to-peer radios. All intelligence is processed locally.

| Data Type | Stored? | Shared? | Why? |
| :--- | :--- | :--- | :--- |
| **AI Synthesis** | Only on your phone | Group partners | Local analysis of trends to provide context without manual sorting. |
| **Document Mining** | Only on your phone | No | Extraction of tasks from shared files for local productivity. |
| **Consensus (Votes)** | Only on your phone | Group partners | Anonymous voting to prioritize important information. |
| **Identity & Roles** | Only on your phone | Nearby People only | Local identification and group permissions. |
| **Shared Records** | Only on your phone | Group members only | Collaborative notes and shared items. |
| **Messages** | Only on your phone | Group partners | To maintain private and shared communication history. |
| **History** | Only on your phone | No | Permanent local storage of your conversations. |
| **History Sync** | Only on your phone | Trusted peers only | Automatic synchronization to ensure your history is up to date. |
| **Geofencing** | No | No | Used locally for location-based suggestions. Never tracked. |

## 2. Privacy by Proximity
- **Local Network**: All public groups are discoverable within radio range.
- **Encrypted Private Chats**: Private conversations are end-to-end encrypted. Your data never leaves your physical proximity.
- **Stealth Mode**: Hide your presence while still being able to monitor the local feed.
- **User Governance**: Block any user to instantly stop all communication and discovery from them.

## 3. Storage & Persistence
- **Automatic Pruning**: Blukit automatically deletes large media older than 90 days to save space.
- **Permanent Pins**: Important messages can be pinned to prevent automatic deletion.
- **Encrypted Backups**: Recent messages are automatically and securely mirrored to trusted nearby devices for recovery if you lose your data.

## 4. Hardware Encryption
**All private communication is end-to-end encrypted.**

- **Encryption Standards**: AES-256-GCM for payloads and P-256 ECDH for key negotiation.
- **Hardware Anchored**: Cryptographic keys are generated and stored in the Android Keystore (StrongBox/TEE).
- **Ownership**: You own your data. There are no central accounts or servers.

---
**Protocol Updated**: August 31, 2026
