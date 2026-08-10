# Blukit: Data Safety Declaration

This document outlines how **Blukit** handles user data, privacy, and security in compliance with Google Play Store standards.

## 1. Data Collection & Sharing

**Blukit does not collect or share any user data with third parties.**

| Data Type | Collected | Shared | Purpose |
| :--- | :--- | :--- | :--- |
| **Personal Info** (Nickname) | No | No | Strictly stored locally to identify the user to nearby peers. |
| **Messages** | No | No | Stored in a local encrypted database for the user's history. |
| **Location** | No | No | Bluetooth/Wi-Fi scanning requires location permissions on older Android versions, but location data is never read or stored. |
| **Device IDs** | No | No | A random UUID is generated locally to uniquely identify the peer in a decentralized mesh. |

## 2. Encryption

**All data is encrypted in transit.**

- **Peer-to-Peer Traffic**: All messaging payloads are encrypted using **AES-256-GCM** via the hardware-backed **Android Keystore System**.
- **Shared Secrets**: Encryption keys are established between peers using **Diffie-Hellman (ECDH)**, ensuring that only the sender and receiver can read the messages.

## 3. Local Privacy & Ephemerality

- **No Cloud Sync**: Messages never leave the local device radio range (50-100m).
- **Automated Purge**: All local chat logs are automatically deleted from your device after 12 hours.
- **UGC Moderation**: The app includes a user-friendly system for **Reporting and Blocking** offensive users and content locally, ensuring a safe decentralized environment.
- **Stealth Mode**: A dedicated "Theater Mode" uses an OLED-optimized pitch-black theme to prevent screen glare in dark environments.

## 4. Permissions

Blukit uses granular permissions to protect user privacy:
- `BLUETOOTH_SCAN`: Used with the `neverForLocation` flag to ensure scanning is only used for peer discovery.
- `BLUETOOTH_CONNECT`: Required to establish secure sockets between peers.
- `BLUETOOTH_ADVERTISE`: Required to make the device discoverable to nearby peers.

---
**Last Updated**: 2026-08-10
