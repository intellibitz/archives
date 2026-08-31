# Blukit: Private Offline Chat

**OFFLINE P2P MESSAGING. INSTANT. SECURE.**

Blukit is a 100% offline, peer-to-peer messaging app that lets you communicate with people nearby—without using the internet. It transforms your phone into a secure data node, preserving your messages in a private local database.

---

## 🤖 AI ASSISTANT: ON-DEVICE INTELLIGENCE
Blukit uses on-device AI to help you manage your conversations silently.
- **Document Mining**: Automatically extracts tasks and summaries from shared files (PDF, Text, JSON) without internet.
- **Contextual Awareness**: Local analysis groups your messages to provide clarity and automated task detection.
- **Privacy**: All AI processing happens locally. Your data never leaves your device.

---

## 🏛️ TERMINOLOGY
- **NEARBY**: Your local network state. A real-time view of People and Groups around you.
- **GROUPS**: Shared spaces for messages and collaboration.
- **PRIVATE CHATS**: Encrypted one-on-one or small group conversations.
- **LIVE FEED**: A real-time, 100% offline stream of activity in your physical proximity.
- **MESSAGE**: A unit of communication, stored securely on your device.
- **HISTORY**: Your private, chronological record of all interactions.

---

## ⚡ KEY INTERACTIONS
Simple and intuitive messaging.

### **1. Discover**
Open the app to see who is nearby. Connect instantly to start chatting with people in your immediate physical range.

### **2. Message**
Join a Group or start a Private Chat. Messages are delivered directly from phone to phone using Bluetooth and Wi-Fi.

### **3. Secure**
Every message is encrypted. Blukit ensures your data is private, with end-to-end encryption for all non-public interactions.

---

## 📱 USER INTERFACE
Clean, ergonomic, and efficient.

- **Adaptive Scaffolding**: The interface adapts dynamically across Phones, Tablets, and Foldables.
- **Dark Mode**: Optimized for OLED displays to maximize power efficiency.
- **Haptic Feedback**: Subtle physical cues reinforce successful connections and message delivery.
- **Group Management**: Easily manage members and roles in your private groups.

---

## 🛠️ ARCHITECTURAL STACK
1. **100% Offline**: Zero cloud dependencies. Uses Bluetooth LE and Wi-Fi Aware.
2. **Security**: P-256 ECDH for key exchange + AES-256-GCM for encryption.
3. **Data Engine**: Deterministic LWW-CRDT for seamless offline synchronization.
4. **Persistence**: Peer-to-peer redundancy for encrypted fragments.

## 🚀 DEVELOPER SETUP
1. **Toolchain**: JDK 17+, Android Studio Ladybug+.
2. **Dependencies**: Koin (DI), Compose Adaptive (Layout), Navigation3.
3. **Permissions**: Requires Bluetooth and Location (for P2P discovery).

---

*Intellibitz*  
*PRIVATE COMMUNICATION.*
