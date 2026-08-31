# Blukit: Offline Life Logger

**OFFLINE P2P DATA LOGGING. INSTANT. LOCAL.**

Blukit is a 100% offline, peer-to-peer ecosystem that lets you log your activities and interact with your local groups—without using the internet. It transforms your phone into a local data store, preserving your records in an immutable database.

---

## 🌬️ AIR: AMBIENT INTELLIGENT RESONANCE
Unlike standard chat apps, Blukit uses on-device **AIR** to process your records.
- **Document Mining**: Automatically extracts tasks, entities, and summaries from shared files (PDF, Text, JSON) without internet.
- **Sphere Synthesis**: Local analysis groups the messages in your Groups to provide clarity, adjusting UI colors based on context.
- **Actionable Insights**: The system identifies frequent activities, trends (Academic, Social, Transit), and automatically injects detected tasks into the resonance field.
- **Privacy**: All processing happens locally. Your records never leave your device.

---

## 🏛️ TECHNICAL LEXICON
- **AIR**: Ambient Intelligent Resonance. The local synth engine for offline intelligence.
- **DISCOVERY**: Your local network state. A real-time awareness of the Peers and Groups around you.
- **ANCHORED GROUPS**: Private sub-groups linked to a public Sphere, discoverable only by authorized peers via anchor advertisements.
- **LOCAL FEED**: The Message Stream. A real-time, 100% offline feed of messages in your physical proximity.
- **GROUP**: A shared space for messages and records.
- **MESSAGE**: A permanent unit of data. A record of an event.
- **PEER**: The origin of a message. You and those near you.
- **HISTORY**: Your immutable, chronological record. Data is harmonized using LWW-CRDT logic.

---

## ⚡ INTERACTIONS
Log records quickly and securely.

### **1. Discover**
Open the app to see who is nearby. The discovery screen shows peers with visual connection indicators. The background status glow indicates the activity level of the local network.

### **2. Message**
Enter a Group. Tap the input to log a thought or activity. Haptic feedback reinforces the interaction as your message is sent to the local network. Monitor the feed to see real-time mesh activity.

### **3. Preserve**
Every message is a record. Blukit ensures your data is preserved in history, automatically replicated to nearby peers for redundancy.

---

## 📱 USER INTERFACE
A compact, ergonomic design.

- **Interface Design**: Immersive visual effects like Activity Heatmaps and visual feedback make the network state visible.
- **Adaptive Scaffolding**: Powered by `NavigationSuiteScaffold`, the interface adapts dynamically across Phones (Bottom Bar), Tablets (Nav Rail), and Foldables.
- **Dark Mode (OLED)**: An absolute-black (#030507) interface with high-contrast borders for maximum power efficiency and visual clarity.
- **Haptic Feedback**: Multi-stage haptic feedback and consistent typography reinforce interactions.
- **Peer Management**: Long-press any Peer to block, identify, or initiate a private message.
- **Group Management**: Fully manage members, assign roles, and control archive status in private groups.

---

## 🛠️ ARCHITECTURAL STACK
1. **100% Offline**: Zero cloud dependencies. Uses Bluetooth LE and Wi-Fi Aware for discovery.
2. **Security**: P-256 ECDH for key exchange + AES-256-GCM for payload encryption. Anchored in Android StrongBox/TEE.
3. **Data Engine**: Deterministic LWW-CRDT database for seamless multi-master synchronization.
4. **Persistence**: Peer-to-peer redundancy. Encrypted fragments are distributed to trusted peers to prevent data loss.

## 🚀 DEVELOPER SETUP
1. **Toolchain**: JDK 17+, Android Studio Ladybug+.
2. **Dependencies**: Koin (DI), Compose Adaptive (Layout), Navigation3.
3. **Permissions**: Requires `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, and `ACCESS_FINE_LOCATION`.

---

*Intellibitz*  
*OWN YOUR DATA.*
