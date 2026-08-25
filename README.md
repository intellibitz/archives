# blukit: crowd resonance

**CROWD (B) RESONANCE. ANYWHERE.**

blukit is built for the **Crowd**, not the **Web**. It is a 100% offline, peer-to-peer **Media Mesh** that lets you spread pulses, share media, and establish secure chains in any physical environment—festivals, subways, crowd hubs, or universities—without a single byte touching the internet.

---

## 🌬️ CROWD LEXICON
- **EVENT**: The root entry point. Global spectrum view of the mesh.
- **CROWD**: Public, discoverable frequencies where name *is* identity. Shared physical containers.
- **CHAIN**: Private, encrypted interaction layers. Anchored to a Crowd context.
- **PULSE**: The atomic unit of energy. A single resonant pulse.

---

## ⚡ THE CROWD FIELD
Experience the **Unified Pulse Frequency**.

### **Crowd Hierarchy (The Resonance Drill-Down)**
- **THE CROWD (Landing)**: The global spectrum view. Surfaces all **Events**—the public frequencies nearby. Pure discovery zone; joining or creating events are the primary actions.
- **CROWD FIELD (Public)**: Shared physical containers. Once joined, the interaction hub awakens. Lists **Child Crowds**, **Ties**, and **Resonances**. Recursive by design.
- **CHAIN FIELD (Private)**: Secure, encrypted interaction layers anchored to a Crowd context. **All Chains are Private**. Lists **Ties** and **Resonances**.
- **PULSE FIELD (Granular)**: The deepest level of the mesh. Breaks down **Resonances** into its constituent pulses.
- **TIMELINE FIELD (History)**: A visual chronological path of student memories and shared milestones.

### **Core Interaction Paradigm**
- **Resonance Summaries**: A "Summary-First" visual paradigm. Headers display high-level context (Title, Member Count, "ENTER" affordance) and generic type labels (e.g., `EVENT` or `PRIVATE CHAIN`).
- **Ticker Sectioning**: The landing view uses explicit tactical sectioning like **"JOIN ACTIVE EVENTS BELOW"** with integrated creation affordances like **"CREATE NEW EVENT"**.
- **Context-First Navigation**: Actions are scoped to their containers. You must "Join" or "Enter" a frequency before the Pulse Hub awakens, ensuring conceptual integrity and preventing global spam.
- **Event Ownership**: Public Crowds created by users are owned by the creator, allowing them to set a unique **Event Persona** (emoji) that identifies the frequency across the mesh. The default "THE CROWD" remains collectively owned.
- **Intelligent Ticker (The Life Stream)**: A real-time feed of the mesh's energy. Dynamically surfaces units from the current context and its children, with unit counts for Resonances.
- **Scoped Sharing & Roles**: Specialized roles (e.g., Student, Faculty, Artist) within templates to manage interaction density and scoped visibility.
- **Rose Resonance**: Private Chains and Mutual Pulses resonate in **Stealth Rose**, distinguishing them from the Amber public Field.
- **Media Mesh**: High-speed, localized media sharing using composite **Radios** (BLE + WiFi).
- **Crowd Heatmaps**: The Field background glows with spectral intensity based on local pulse frequency.
- **Academic Rituals (Smart Reminders)**: Automated "Awakening" of frequencies with smart reminders for lectures and assignments.
- **Crowd Canvas (Pinned Energy)**: Collaborative persistence in the header for high-priority pulses.
- **Smart Assignment Tracking**: Create, assign, and track academic tasks offline within a Chain using conflict-free versioning.
- **Pulse Decay (Storage Pruning)**: Self-cleaning logic prunes media older than 90 days (exempting Pinned/Senior Pulses).
- **Mesh Relay (Ephemeral Hops)**: 3-hop ephemeral relaying for pulses beyond immediate radio range.
- **Chain Projection**: Collective emoji identities that project larger pulses on the Discovery Radar.
- **Senior Vault**: Mark specific Chains for permanent preservation (exempt from all decay).
- **Crowd Vault (Auto-Archiving)**: Over a 4-year journey, the ticker stays clean. A protocol automatically moves crowds that haven't pulsed in 30 days into a **"Sunk Pulse"** vault.
- **Differential Pulse Sync**: High-speed WiFi radio sync that only bridges missing history, making updates 10x faster.

---

## 📱 HARMONY HUB
A high-density, ergonomic architecture for tactical crowd control:

### **Tactical Header (Top Overlay)**
- **Row 0 (Global Command)**: Low-profile system bar with tactical toggles for Stealth/Power + **BLUKIT BRANDING** next to the **PRIVACY** tie + Real-time Amber radio status (BT/WiFi).

### **Humanity Stage (Radar Header)**
- **Row 1 (Contextual Navigation)**: Anchored directly to the Radar Field for ergonomic reach. Unified navigation breadcrumbs (showing deep paths like `THE CROWD > CONCERT`), the active **Event/Crowd Title**, and a high-contrast **User Count** badge.
- **Tactical Icon Labels**: System tools in the header include high-contrast labels (e.g., **"RADAR"**, **"SEARCH"**, **"HISTORY"**) in ultra-small tactical fonts for maximum clarity.

### **Interaction Hub (Bottom Overlay)**
*Available only inside a Crowd or Chain*
- **Pulse Hub**: The interaction hub awakens once a context is established. Featuring an **Aura Glow** that signals active focus.
- **Discovery Radar**: Spatial view with a centralized **Context Anchor** (Named Event), your prominent **Identity Anchor** ("YOU") nearby, and other Users in orbit.
- **Spectral Dimming**: The radar field intelligently dims when the Pulse Hub is engaged, focusing the user's energy on the current interaction while maintaining spatial mesh awareness.
- **Tactical Identity**: Your persona node provides immediate recognition by showing your name (up to 3 characters) and an explicit **"YOU"** anchor.

---

## 🎯 MARKET POSITION
Blukit positions itself as the premier peer-to-peer communication solution for:
- **Campus Communities**: Tailored for university life, lectures, and secure student groups.
- **Event Management**: High-density coordination for festivals, concerts, and conferences.
- **Physical Venues**: Shared frequencies for offices, hubs, and stadiums.
- **Local Gatherings**: Spontaneous neighborhood groups and physical social circles.
- **Tactical Coordination**: Secure, offline communication in critical physical areas.

---

## 🛠️ ARCHITECTURAL SUPREMACY
1. **100% Offline**: Zero cloud. No servers. No internet. Data stays in the air.
2. **Header + Entries Pattern**: A unified visual language across all widgets (Field, Hubs, Ticker, Radar). Headers provide collective context; Entries provide atomic interaction.
3. **Media Mesh**: Composite radio orchestration (Nearby Connections + BLE Fallback).
4. **Hardware Encrypted**: ECDH handshakes + AES-256-GCM for absolute local privacy.
5. **Deterministic Scoped Naming**: Public Crowd IDs are generated as paths (e.g., `crowd_MALL_FOOD_COURT`), ensuring global uniqueness.
6. **Partitioned Group Scaling**: Intelligent member partitioning for crowds exceeding 500 members.
7. **Decentralized-First**: Prioritize mesh efficiency and hardware-encrypted energy.
8. **Shared :core**: Koin DI, UDF with `BluetoothUiState`, and primitive `Canvas` UI foundation.

---

## 🛠️ DEVELOPMENT HYGIENE
- **Temporary Files**: To maintain a clean workspace and prevent accidental leaks of transient data, all temporary files, logs, or scratch scripts MUST be created within the `.gitignore_folder` directory.

---

## 🤖 THE AGENT PROTOCOL (FOR LLM COLLABORATORS)

### **Master Operational Directive**
*   **Production Excellence**: All code must be production-ready, bug-free, and accompanied by comprehensive unit tests.
*   **Security & Safety**: Prioritize secure coding practices (ECDH, AES-256) and thread-safe operations.
*   **Performance Engineering**: Write highly performant code with a focus on low battery consumption—crucial for persistent mesh operations.
*   **Architectural Integrity**: Android, Kotlin, and Gradle code must be modular, decoupled, and adhere to clean architecture principles.
*   **Comprehensive Documentation**: Every file MUST include detailed header comments, MUST include detailed function comments, and detailed inline comments explaining its purpose, logic, and integration points. This ensures LLM collaborators can instantly grasp the code's intent and function.
*   **Modern Toolstack**: Always leverage the latest stable libraries, tools, and idiomatic Kotlin/Compose patterns.
*   **Knowledge Custodian**: Act as the project's living documentation and maintainer of the `Event > Crowd > Chain > Pulse` nested scoping.

### **Conversational Protocol**
*   **Summary First**: Provide a "Quick Energy Report" before any task.
*   **Strategic Dialogue**: Suggest improvements; find better alternatives. Don't just follow instructions—engineer solutions.
*   **Explicit Confirmation**: Confirm breaking changes before applying.

---

## 🚀 START THE PULSE
```bash
./gradlew installDebug
```

---

*Built with ❤️ for the Crowd by [Intellibitz](https://github.com/intellibitz)*  
*CROWD (B) RESONANCE. JOIN THE CROWD.*
