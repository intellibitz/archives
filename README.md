# Blukit: Natural Offline Chat

**PRIVATE P2P MESSAGING. INSTANT. LOCAL.**

Blukit is a standard, easy-to-use chat app with a powerful secret: it operates 100% offline. No servers, no cloud, no internet required. Just open the app and start chatting with people in your immediate physical proximity.

---

## 🤖 AI ASSISTANT: AUTONOMOUS INTELLIGENCE
Blukit features an on-device AI Assistant that handles complex optimizations in the background, allowing you to focus on the conversation.
- **Silent Eco**: The app automatically throttles its engine and stops animations when it detects low battery or when the Android OS enters "Battery Saver" mode to preserve your hardware energy.
- **Silent Stealth**: The app intelligently stops broadcasting your identity when you aren't active, making you a "silent listener" automatically without any manual configuration.
- **Context Synthesis**: The Assistant analyzes conversation trends to subtly adjust UI themes and extract actionable items from your messages and documents locally.

---

## 🏛️ SIMPLE TERMINOLOGY
Blukit uses natural, human-centric language. Developers and AI agents must strictly avoid "mesh jargon" or technical complexity in the user-facing interface.
- **PEOPLE**: Users currently within your physical range.
- **GROUPS**: Shared spaces for collaboration and community messaging.
- **PRIVATE CHATS**: Secure, end-to-end encrypted conversations between individuals.
- **HISTORY**: Your private, chronological record of all messages and shared files.
- **SHARE TO ALL**: The act of broadcasting information to everyone nearby.
- **AUTHENTICITY**: Verifying the source and integrity of information via the local swarm.

---

## ⚡ KEY INTERACTIONS
Messaging as it should be—simple and private.

### **1. Open & Chat**
By default, you land in the **Public Hub**. Send a message instantly to whoever is nearby. No setup, no accounts, no waiting.

### **2. Connect Locally**
Check the **Nearby** tab to see people in your immediate range. Tap a name to start a private, secure conversation that remains "anchored" to your current context.

### **3. Stay Private**
Every private interaction is encrypted at the hardware level. Blukit ensures your data remains yours, visible only to the intended recipients.

---

## 📱 USER INTERFACE
Clean, ergonomic, and familiar.

- **Natural Chat Experience**: Standard message bubbles, intuitive navigation, and an elegant toolbar focus on the conversation, not the technology.
- **Adaptive Design**: The interface scales perfectly across Phones, Tablets, and Foldables. Large devices automatically use **List-Detail multi-pane layouts** for simultaneous list browsing and chatting.
- **OLED Optimized**: A true-black interface designed for maximum privacy and power efficiency.

- **Silent Feedback**: Subtle haptics reinforce successful connections and message delivery.

---

## 🛠️ ARCHITECTURAL STACK
1. **Modular Core**: Decoupled protocol logic (Crypto, Handshaking, Models) into a standalone `:core` module for scalability and KMP readiness.
2. **High-Performance Persistence**: Room 3.0 (KMP) with Paging 3 support for efficient, low-memory history management.
3. **End-to-End Security**: P-256 ECDH hardware-backed key agreement + AES-256-GCM authenticated encryption.
4. **Data Integrity**: Deterministic LWW-CRDT resolution for conflict-free multi-device synchronization.

## 🚀 DEVELOPER SETUP
1. **Toolchain**: JDK 21+, Android Studio Ladybug+.
2. **Architecture**: Modular MVVM + UDF (Unidirectional Data Flow) with Paging 3 streams.
3. **Core Dependencies**: Koin (DI), Room 3.0, Paging 3.5, Compose Adaptive.
4. **Permissions**: Requires Bluetooth and Location (strictly for local device discovery).

---

*Intellibitz*  
*PRIVATE COMMUNICATION. OWNED BY YOU.*
