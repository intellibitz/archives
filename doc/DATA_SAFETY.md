# Blukit - Spread the Vibes: Data Safety Declaration

This document outlines how **Blukit - Spread the Vibes** handles user data, privacy, and security in compliance with Google Play Store standards.

## 1. Data Collection & Sharing

**Blukit does not collect or share any user data with third parties.**

| Data Type | Collected | Shared | Purpose |
| :--- | :--- | :--- | :--- |
| **Personal Info** (Nickname) | No | No | Strictly stored locally to identify you to nearby voices. |
| **Messages** | No | No | Stored in a local encrypted database for your history. |
| **Location** | No | No | Bluetooth scanning requires location permissions on older Android versions, but location data is never read or stored. |
| **Device IDs** | No | No | A random UUID is generated locally to uniquely identify you in the collective. |

## 2. Encryption

**All data is encrypted in transit.**

- **Vibe-to-Vibe Traffic**: All messaging payloads are encrypted using **AES-256-GCM** via the hardware-backed **Android Keystore System**.
- **Shared Secrets**: Encryption keys are established between voices using **Diffie-Hellman (ECDH)**, ensuring that only you and your tie can hear the vibes.

## 3. Local Privacy & Ephemerality

- **No Cloud Sync**: Voices never leave the local device radio range (50-100m).
- **Automated Purge**: All local logs are automatically deleted from your device after 12 hours.
- **Vibe Moderation**: The app includes a sentient system for **Blocking** offensive voices locally, ensuring a safe collective environment.
- **Quiet Light**: A dedicated OLED-optimized pitch-black theme to prevent screen glare in dark environments.

## 4. Permissions

Blukit uses granular permissions to protect user privacy:
- `BLUETOOTH_SCAN`: Used with the `neverForLocation` flag to ensure scanning is only used for vibe discovery.
- `BLUETOOTH_CONNECT`: Required to establish secure rituals between voices.
- `BLUETOOTH_ADVERTISE`: Required to make your presence felt in The Vibes.

---
**Last Updated**: August 15, 2026
