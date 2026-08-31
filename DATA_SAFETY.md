# Blukit: Data Safety Protocol

**Blukit is 100% offline, private, and secured by physical proximity.**

## 1. Zero Cloud Dependency
Blukit **never** connects to the internet. All **Messages** travel directly from phone to phone using peer-to-peer radios. There are no central servers to hack, leak, or monitor your data.

| Data Type | Stored? | Shared? | Why? |
| :--- | :--- | :--- | :--- |
| **Assistant Logic** | Only on your phone | Group partners | Local analysis of trends to provide context and "mood" tints. |
| **Document Mining** | Only on your phone | No | Extraction of tasks from shared files for local productivity. |
| **Consensus Votes** | Only on your phone | Group partners | Anonymous voting to prioritize important information within a group. |
| **User Identity** | Only on your phone | Nearby People only | Local nicknames and avatars for peer identification. |
| **Shared Notes** | Only on your phone | Group members only | Collaborative records and shared items. |
| **Messages** | Only on your phone | Group partners | Private and shared communication history. |
| **History** | Only on your phone | No | Permanent local storage of your personal conversations. |
| **Sync Data** | Only on your phone | Trusted peers only | Automatic differential sync to keep group history consistent. |
| **Location Data** | No | No | Used locally for P2P discovery. Never tracked, stored, or shared. |

## 2. Privacy by Proximity
- **Physical Sandbox**: Your data can only travel as far as your device's radio range (Bluetooth/Wi-Fi). 
- **Encrypted Chats**: All non-public conversations are end-to-end encrypted. Even if someone intercepts the radio signal, they cannot read the content.
- **Stealth Mode**: Allows you to monitor the local feed while remaining invisible to others nearby.
- **Blocking**: Block any user to instantly stop all communication and discovery from them.

## 3. Persistence & Recovery
- **Auto-Cleaning**: Blukit automatically prunes large media older than 90 days to maintain device storage.
- **Permanent Pins**: Secure important messages from being auto-deleted by pinning them.
- **Encrypted Backups**: Your most recent messages are automatically mirrored in encrypted fragments to trusted nearby devices, allowing for recovery if your device is lost or wiped.

## 4. Hardware-Grade Encryption
**All private communication is end-to-end encrypted at the hardware level.**

- **Standards**: AES-256-GCM for payloads and P-256 ECDH for secure key exchange.
- **Hardware Anchored**: Cryptographic keys are generated and stored within the device's secure enclave (StrongBox/TEE).
- **Ownership**: There are no central accounts. You own the hardware, you own the keys, you own the data.

---
**Protocol Updated**: September 1, 2026
