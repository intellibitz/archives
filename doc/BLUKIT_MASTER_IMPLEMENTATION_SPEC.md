# Blukit: Master Implementation Specification (Merged & Gap-Filled)

**Version:** 1.0 — August 11, 2026  
**Status:** Production-Ready Architecture + Gap-Fill Blueprint  
**Package:** `cc.thevar.blukit` · **Play Console Account:** `6848893333714998276`

---

## Table of Contents

1. [Executive Summary & Vision](#1-executive-summary--vision)
2. [Verified Technical Stack](#2-verified-technical-stack)
3. [Architecture Blueprint (Actual Codebase)](#3-architecture-blueprint-actual-codebase)
4. [Gap Analysis: Blueprint vs. Reality](#4-gap-analysis-blueprint-vs-reality)
5. [Gap-Fill Implementations — What to Add & Change](#5-gap-fill-implementations--what-to-add--change)
6. [Security & Cryptography Deep Review](#6-security--cryptography-deep-review)
7. [Play Store Deployment Checklist (Verified)](#7-play-store-deployment-checklist-verified)
8. [Implementation Priority Roadmap](#8-implementation-priority-roadmap)

---

## 1. Executive Summary & Vision

### What Blukit Is

Blukit is a **decentralized, privacy-first offline messaging application** for instant local peer-to-peer (P2P) communication in high-density environments where cellular or Wi-Fi connectivity is unavailable or unreliable — stadiums, movie theaters, concert venues, transit hubs, crowded malls, and large events.

### Core Principles (Verified Against Codebase)

| Principle | Blueprint Spec | Actual Implementation |
|-----------|---------------|----------------------|
| Zero-Friction Onboarding | No accounts, phone, email — just nickname + emoji | ✅ `IdentityRepository` + EncryptedSharedPreferences |
| Serverless Mesh P2P | Pure local radio, no internet routing | ✅ Google Nearby Connections API (`P2P_CLUSTER`) |
| Ephemeral Data | Auto-purge chat logs after session | ✅ Room DB + 12-hour TTL PurgeWorker via WorkManager |
| Hardware-Secured Crypto | AES-256-GCM + ECDH key exchange | ✅ Android Keystore + SecP256r1 curve |
| Stealth Theater Mode | OLED pitch-black (#000000) for dark venues | ✅ `BlukitTheme` with `StealthColorScheme` |
| Haptic Silent Alerts | Vibration-only notifications | ✅ `HapticManager` with double-pulse waveform |
| UGC Moderation | Report & Block peers locally | ✅ Long-press on chat bubbles + `blockedUsers` flow |
| Global i18n | 18+ languages | ✅ strings.xml in en, es, fr, zh, ja, ru, de, ar, hi, ta, te, kn, ml, mr, bn, gu, pa, ur |

---

## 2. Verified Technical Stack

All verified against `build.gradle.kts` and `gradle/libs.versions.toml`:

| Layer | Technology | Version | Verified |
|-------|-----------|---------|----------|
| Language | Kotlin + Compose Compiler | 2.2.10 / Kotlin DSL | ✅ |
| Android Gradle Plugin | AGP | 9.3.1 | ✅ |
| UI Framework | Jetpack Compose M3 BOM | 2024.09.00 | ✅ |
| Navigation | Jetpack Navigation 3 | 1.0.0-alpha01 | ✅ |
| Adaptive Layouts | Compose M3 Adaptive | 1.3.0-rc01 + 1.3.1 (NavSuite) | ✅ |
| P2P Engine | Google Nearby Connections API | 19.0.0 (`play-services-nearby`) | ✅ |
| Persistence | Room Database | 2.7.0 + KSP | ✅ |
| Cryptography | AndroidX Security Crypto | 1.1.0-alpha06 | ✅ |
| Reactive Streams | Kotlin Coroutines | 1.10.2 | ✅ |
| Serialisation | kotlinx.serialization.json | 1.9.0 | ✅ |
| Background Tasks | WorkManager | 2.10.0 | ✅ |
| Permission Handling | Accompanist Permissions | 0.37.3 | ✅ |
| Image Loading | Coil Compose | 2.7.0 | ⚠️ Declared but unused — candidate for image payloads |
| DI | ViewModel Factory (manual) | Built-in | ✅ |
| Toolchain | Java 17 via Gradle Toolchains | foojay-resolver 1.0.0 | ✅ |
| Min SDK | Android 8.0 (API 26) | — | ✅ |
| Target SDK | Android 15 (API 35) / Compile API 37 | ⚠️ compileSdk=37 but targetSdk=35 | Note: Verify against latest Google Play policy |

### Dependencies That Should Be Removed/Cleaned

- **Camera dependencies** (`camera-camera2`, `camera-core`, `camera-lifecycle`, `camera-view`) — declared but never referenced in code. Candidate for removal unless image payloads are planned.
- **Retrofit/OkHttp/Moshi** — declared but unused (blueprint mentions them but current code uses `kotlinx.serialization.json` instead).
- **Hilt dependencies** — listed in libs.versions.toml but not wired into build.gradle.kts. Current DI is manual ViewModel Factory (acceptable for this scale).

---

## 3. Architecture Blueprint (Actual Codebase)

### File-by-File Map

```
cc.thevar.blukit/
├── BlukitApplication.kt           ✅ App entry, PurgeWorker setup
├── MainActivity.kt                ✅ Edge-to-edge, theme wiring, dependency injection
│
├── data/
│   ├── crypto/
│   │   └── CryptoManager.kt       ✅ ECDH key exchange + AES-256-GCM (needs auth tag fix)
│   ├── local/
│   │   ├── ChatDatabase.kt        ✅ Room DB with 3 entities (messages, contacts, peers)
│   │   ├── dao/
│   │   │   ├── MessageDao.kt      ✅ Flow-based queries + TTL delete
│   │   │   └── PeerDao.kt         ✅ Peer persistence for reconnection safety
│   │   └── entities/
│   │       ├── ContactEntity.kt   ✅ Local contact records
│   │       ├── MessageEntity.kt   ✅ Message storage with status tracking
│   │       ├── PeerEntity.kt      ✅ Endpoint ID + public key + lastSeen
│   │       └── Mappers.kt         ✅ toMessageEntity / toBluetoothPayload
│   ├── repository/
│   │   └── IdentityRepository.kt  ✅ EncryptedSharedPreferences for all profile data
│   ├── system/
│   │   ├── HapticManager.kt       ✅ VibrationEffect waveform double-pulse
│   │   └── RadioStateManager.kt   ⚠️ Uses deprecated BluetoothAdapter (needs fix)
│   └── worker/
│       └── PurgeWorker.kt         ✅ 12-hour TTL auto-wipe via WorkManager
│
├── domain/model/
│   ├── ConnectionStatus.kt        ✅ Sealed hierarchy for P2P events
│   ├── MessagePayload.kt          ✅ Serializable message model (text + image type)
│   └── P2PDevice.kt              ✅ Discovery data with proximity logic
│
├── network/p2p/
│   ├── P2PController.kt           ✅ Interface defining discovery/connection contract
│   └── NearbyP2PController.kt     ✅ Google Nearby Connections implementation (critical fixes needed)
│
├── ui/
│   ├── BlukitApp.kt              ⚠️ Nav wiring issues: RadarScreen used directly, DiscoveryScreen unused
│   ├── navigation/
│   │   └── Routes.kt              ✅ Sealed NavKey routes: Profile, Discovery, Chat
│   ├── theme/
│   │   ├── Color.kt               ✅ Standard + Stealth color schemes defined
│   │   ├── Theme.kt               ✅ BlukitTheme composable with dynamic/dark/stealth switching
│   │   └── Type.kt               ⚠️ Minimal typography; could be expanded for M3 compliance
│   ├── screens/
│   │   ├── ChatScreen.kt          ⚠️ Missing peer identity display, delivery status badges
│   │   ├── DiscoveryScreen.kt     ✅ Permission + radio state wrapper (never wired in nav)
│   │   ├── ProfileScreen.kt       ✅ Nickname, emoji selection, stealth toggle — complete
│   │   └── RadarScreen.kt         ⚠️ Peer positioning uses circular layout, not true RSSI-based distance
│   ├── viewmodels/
│   │   ├── BluetoothUiState.kt    ✅ UI state data class with scanned devices + messages
│   │   ├── BluetoothViewModel.kt  ⚠️ Missing startScan/stopScan/public broadcast methods
│   │   └── MainViewModel.kt       ✅ Profile management complete
│   └── previews/
│       └── MarketPreviews.kt      ✅ Compose previews for Radar, Chat, Profile (Play Store asset gen)
```

### Data Flow Summary

```
User → ProfileScreen → IdentityRepository (EncryptedSharedPreferences)
         ↓
    BluetoothViewModel → NearbyP2PController → Google Nearby Connections API
                              ↓
         RadioStateManager ← System Broadcasts
                              ↓
          ChatScreen / RadarScreen  ← StateFlow collections
                              ↓
        CryptoManager ← AES-256-GCM encrypt/decrypt
                              ↓
       Room Database (MessageDao, PeerDao) ← TTL PurgeWorker (12h)
```

---

## 4. Gap Analysis: Blueprint vs. Reality

### CRITICAL GAPS (Must Fix Before Production)

#### Gap #1: Stadium Lobby / Public Broadcast Mode Does Not Exist
- **Blueprint says:** "Stadium Lobby (Public Broadcast): Anonymous multi-point local broadcast channel reaching all discovered Blukit peers within 50–100m radio range."
- **Reality:** Codebase has zero lobby concept. `ChatScreen` is hardcoded to display messages between two peers. No `LobbyScreen` or public broadcasting mode exists. The `sendMessage(null)` pattern in the P2PController tries to broadcast but has no dedicated UI, no lobby state management, and no way for users to distinguish lobby vs. whisper chats.

#### Gap #2: `DiscoveryScreen` Exists But Is Never Used
- **Blueprint says:** Discovery screen handles permissions + radar view.
- **Reality:** `BlukitApp.kt` routes `Route.Discovery` directly to `RadarScreen` — bypassing the permission/radio-state wrapper in `DiscoveryScreen`. The `onStartScan`/`onStopScan`/`onStartServer` callbacks are never connected to anything.

#### Gap #3: ChatScreen Does Not Display Peer Identity
- **Blueprint says:** "Direct Whisper (1-on-1) encrypted messaging channels."
- **Reality:** `ChatScreen` shows messages but the TopAppBar only says "Stadium Lobby" and "Connected to nearby peers." Users don't know which peer they're chatting with. No peer name, emoji, or signal indicator in the chat header.

#### Gap #4: P2P Connection Model Is Inconsistent With Mesh
- **Blueprint says:** "Multi-point topology, automatic radio switching."
- **Reality:** `isConnected: StateFlow<Boolean>` is binary — it doesn't represent the mesh reality where multiple peers are simultaneously connected. There's no `peerConnectionState: StateFlow<Map<String, PeerConnectionStatus>>` to track individual peer states.

#### Gap #5: Radar Screen Does Not Use Real RSSI Data
- **Blueprint says:** "Interactive visual display rendering active nearby peers as concentric distance indicators based on signal strength (RSSI)."
- **Reality:** `NearbyP2PController.startDiscovery()` creates `P2PDevice` with `signalStrength = 0` always. No RSSI value is extracted from `DiscoveredEndpointInfo`. Peer positions on the radar are distributed circularly by index, not by actual proximity.

#### Gap #6: Encryption Missing GCM Authentication Tag Validation
- **Blueprint says:** "Receiver nodes validate payload authenticity via GCM tags before passing decrypted strings to Room."
- **Reality:** `CryptoManager.encrypt()` prepends only the IV (12 bytes), NOT an authentication tag. `decrypt()` does not call `cipher.doFinal(encryptedPart)` in a way that validates the GCM tag — if the tag is missing, corrupted data passes through silently.

#### Gap #7: Bluetooth API Compatibility Issue
- **Blueprint says:** "Granular Android 12+ permissions... neverForLocation flag."
- **Reality:** `RadioStateManager` uses deprecated `BluetoothAdapter.getDefaultAdapter()` which requires old `BLUETOOTH` permission (maxSdk=30). Modern code should use `context.getSystemService(BluetoothManager::class.java)`. The manifest already has the right modern permissions but the code path for Bluetooth on API 31+ is still wired through deprecated APIs.

### HIGH PRIORITY GAPS

#### Gap #8: No "Clear History" or Logout UI
- **Blueprint says:** "Explicitly closing a chat session or triggering 'Clear History' executes an immediate deletion."
- **Reality:** Only the background PurgeWorker exists. No UI action to manually clear history or logout/reset profile.

#### Gap #9: No Contact / Recent Peers Management
- **Blueprint says:** "Contacts table with lastSeen tracking."
- **Reality:** `ContactEntity` is defined but never populated, queried, or displayed anywhere. The contacts table exists in the schema but serves no functional purpose today.

### MODERATE GAPS

#### Gap #10: ChatScreen Message Input UX
- Long messages without max-lines limit — could overflow layout.
- No keyboard handling edge cases (IME overlap).

#### Gap #11: No Handling for Nearby API Unavailable
- Some older or custom ROM devices don't support Google Nearby Connections API. No graceful degradation path is implemented.

#### Gap #12: ProfileScreen Emoji Selection UX
- Only 8 emojis available. Consider a more comprehensive emoji picker.

#### Gap #13: i18n String Completeness
- New strings added to base `strings.xml` but translations in other languages are not synchronized (many will fall back to English).

---

## 5. Gap-Fill Implementations — What to Add & Change

### Implementation Block A: Stadium Lobby / Public Broadcast System

**File to create:** `ui/screens/LobbyScreen.kt`  
**Concept:** A separate screen that handles multi-peer public broadcasting (not point-to-point). Messages are sent to ALL connected peers simultaneously. Chat messages flow in both directions (receive from any peer, broadcast your own).

**Key changes needed:**
1. Add a new route: `Route.Lobby` (in `Routes.kt`)
2. Create `LobbyScreen.kt` with message bubbles similar to `ChatScreen` but showing sender names and handling multi-peer input
3. Update `P2PController` interface to expose `connectedPeers: StateFlow<Set<String>>` instead of just `isConnected: StateFlow<Boolean>`
4. Update `NearbyP2PController` to track all connected endpoints, not just a binary flag
5. Wire Lobby into navigation from the Radar screen (tap a "📢 Lobby" FAB/button)

**Data flow for Lobby:**
```
User types in LobbyScreen input → BluetoothViewModel.broadcastLobbyMessage()
  → P2PController.broadcastAllPeers(content)
    → For each peer in connectedPeers: encrypt + send via Nearby Connections
      → Each peer's CryptoManager decrypts → insert into Room DB
        → All peers' ChatMessages update via Flow emissions
```

### Implementation Block B: Fix Navigation — Wire DiscoveryScreen Properly

**File to modify:** `ui/BlukitApp.kt`  
**Current issue:** Routes to raw `RadarScreen`, not `DiscoveryScreen`.

**Change:** Replace the `Route.Discovery` NavEntry from:
```kotlin
// CURRENT (wrong):
Route.Discovery -> NavEntry(key) { RadarScreen(state, onDeviceClick) }
```
To:
```kotlin
// CORRECT:
Route.Discovery -> NavEntry(key) {
    DiscoveryScreen(
        state = bluetoothState,
        onStartScan = bluetoothViewModel::startScan,
        onStopScan = bluetoothViewModel::stopScan,
        onDeviceClick = bluetoothViewModel::connectToDevice,
        onStartServer = bluetoothViewModel::startLobbyBroadcast  // NEW
    )
}
```

### Implementation Block C: ChatScreen — Add Peer Identity Display

**File to modify:** `ui/screens/ChatScreen.kt`  

1. **Add peer parameters:** Change the function signature to accept `peerName: String`, `peerEmoji: String`, `peerSignalStrength: Int`
2. **Update TopAppBar title** to show `"👤 $peerName"` instead of just "Stadium Lobby"
3. **Add delivery status badges** (sent/delivered/read) next to sent messages
4. **Add a "Back" action** in the TopAppBar with `onDismissRequest` that navigates back

### Implementation Block D: RadarScreen — RSSI-Based Proximity Positioning

**File to modify:** `network/p2p/NearbyP2PController.kt` + `ui/screens/RadarScreen.kt`

In `startDiscovery()`, extract and use signal strength:
```kotlin
// In onEndpointFound callback, use DiscoveredEndpointInfo if available
// Since Nearby Connections doesn't always provide RSSI in discovery info,
// we need to maintain a connection-based RSSI tracker:
override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
    if (result.status.isSuccess) {
        activeConnections.add(endpointId)
        // Track RSSI when connected (Nearby provides this during payload transfer updates)
        peerRssi[endpointId] = info?.signalStrength ?: -80  // fallback to moderate
    }
}
```

In `RadarScreen.kt`, use actual proximity values:
- Calculate radius from signal strength: `-30 dBm → closest ring (60dp), -70 dBm → middle (140dp), -90+ dBm → outer edge (220dp)`
- Distribute peers angularly by index within each proximity band

### Implementation Block E: Fix CryptoManager — Add GCM Auth Tag

**File to modify:** `data/crypto/CryptoManager.kt`

The fix is in the encrypt/decrypt methods:

```kotlin
fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
    val cipher = Cipher.getInstance(TRANSFORMATION) // "AES/GCM/NoPadding"
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val iv = cipher.iv  // typically 12 bytes for GCM
    val encrypted = cipher.doFinal(data)
    
    // Prepend: [1-byte tag length prefix][12-byte IV][encrypted data + implicit GCM tag]
    // GCM automatically appends a 16-byte auth tag to doFinal() output in some implementations
    val authTagLen = 16  // bytes
    val result = ByteArray(1 + iv.size + encrypted.size)
    result[0] = authTagLen.toByte()
    iv.copyInto(result, 1)
    encrypted.copyInto(result, 1 + iv.size)
    
    return result
}

fun decrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray {
    val authTagLen = encryptedData[0].toInt() and 0xFF
    val ivPart = encryptedData.copyOfRange(1, 1 + 12)
    val encryptedPart = encryptedData.copyOfRange(1 + 12, encryptedData.size)
    
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128 * authTagLen, ivPart))
    
    // This will throw AEADBadTagException if the tag doesn't match — 
    // which is exactly the security validation we need!
    return cipher.doFinal(encryptedPart)
}
```

### Implementation Block F: Fix RadioStateManager — Modern Bluetooth API

**File to modify:** `data/system/RadioStateManager.kt`

Replace:
```kotlin
// OLD (deprecated):
private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
private val bluetoothAdapter = bluetoothManager.adapter  // deprecated on API 31+
```

With:
```kotlin
// NEW (modern):
@Volatile private var bluetoothEnabled by atomicBooleanOf(false)
init {
    updateBluetoothState()
    registerBluetoothReceiver()
}

private fun updateBluetoothState() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        bluetoothEnabled = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothEnabled = adapter?.isEnabled ?: false
    }
}

private fun registerBluetoothReceiver() {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    updateBluetoothState()
                    trySend(getCurrentStates())
                }
            }
        }
    }
    // ... register as before
}
```

### Implementation Block G: Add Manual Clear History & Logout UI

**File to create:** `ui/screens/SettingsScreen.kt` (or add to ProfileScreen)

Add buttons to ProfileScreen or a new Settings route:
- **"Clear All Chat History"** → calls `messageDao.clearAllMessages()` + shows confirmation snackbar
- **"Reset Profile / Log Out"** → removes nickname from EncryptedSharedPreferences, navigates back to onboarding

### Implementation Block H: Contact Management — Wire Up Contacts Table

**New files needed:**
1. `data/repository/ContactRepository.kt` — CRUD operations for contacts
2. `ui/screens/ContactsScreen.kt` — List of recently discovered peers with their last-seen timestamps and chat history
3. Auto-populate contacts when peer connections succeed (in `NearbyP2PController.onConnectionResult`)

---

## 6. Security & Cryptography Deep Review

### Current Strengths ✅
1. **Android Keystore hardware backing** — Private keys never leave the TEE/SE
2. **ECDH + SecP256r1** — Industry-standard key exchange curve
3. **AES-256-GCM** — Authenticated encryption mode (correct choice; CBC would be insecure)
4. **Per-peer session keys** — Each connection derives its own AES key via ECDH
5. **No external data transmission** — No HTTP clients, no analytics SDKs, no telemetry

### Current Vulnerabilities 🔴

| Vuln ID | Severity | Description | Fix |
|---------|----------|-------------|-----|
| S-01 | HIGH | GCM auth tag not properly validated during decryption. Raw `cipher.doFinal()` without proper tag extraction means corrupted payloads pass silently. | Add explicit auth tag prefix/suffix in encrypt, validate in decrypt (see Implementation Block E) |
| S-02 | MEDIUM | No nonce/IV reuse protection for repeated encryptions with same key. If two messages use the same IV with the same AES key, GCM security breaks. | Generate fresh random 12-byte IV per encryption operation; store IV at start of payload. Current code does this correctly (cipher generates IV) but the decrypt side needs to read it back from payload. |
| S-03 | MEDIUM | Ephemeral secret from ECDH is directly used as AES key via SHA-256. No KDF (Key Derivation Function). Should use HKDF instead of raw SHA-256 for key stretching. | Replace `MessageDigest("SHA-256")` with `HKDF` + salt derivation |
| S-04 | LOW | Shared public keys are exchanged plaintext during handshake. While ECDH prevents eavesdropping on the session key, it doesn't prevent MITM without certificate verification. | For P2P mesh in venues, this is an acceptable risk (trust-on-first-use model). Document as design decision. Consider adding a "verified fingerprint" option for high-security use cases. |
| S-05 | LOW | `fallbackToDestructiveMigration()` in Room means schema changes wipe all data silently. During alpha this is fine; for production, implement proper migration paths. | Remove `fallbackToDestructiveMigration()` and add proper Migration objects to ChatDatabase |

### Recommendations for Production Security Hardening

1. **Add message deduplication** — Use `messageId` from the payload to skip duplicate messages received via mesh (same message can arrive through multiple peers in a mesh topology).
2. **Add replay protection** — Include a monotonically increasing counter per session key; reject messages with counters older than the last seen one.
3. **Rate limit incoming messages** — In `handleMessage()`, add a token bucket rate limiter to prevent flood attacks from malicious peers.
4. **Hash peer public keys for display** — Show SHA-256 fingerprint of peer's public key in chat header so users can verify they're communicating with the intended person (out-of-band verification).

---

## 7. Play Store Deployment Checklist (Verified)

### Account & Verification ✅
- [x] Developer account ID: `6848893333714998276` documented
- [ ] Verify identity in Play Console (government ID or business docs)
- [ ] Complete developer profile (name, email, phone, address)

### Build Configuration ✅
- [x] Package name: `cc.thevar.blukit` (in AndroidManifest + build.gradle.kts)
- [x] Version Code: 10 / Version Name: "1.0.4"
- [x] Min SDK: 26 / Target SDK: 35
- [x] Signing config wired to `keystore.properties` → `blukit-release-key.jks`
- [x] NDK debug symbol level: FULL
- [x] R8 optimization enabled, proguard-rules.pro included
- [ ] Generate signed `.aab` bundle

### Permissions (AndroidManifest.xml) ✅ Verified
- [x] `BLUETOOTH_SCAN` with `neverForLocation` flag
- [x] `BLUETOOTH_ADVERTISE`
- [x] `BLUETOOTH_CONNECT`
- [x] `NEARBY_WIFI_DEVICES` with `neverForLocation` flag
- [x] `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` (for Nearby API compatibility)
- [x] `VIBRATE`
- [ ] Remove/review `INTERNET` permission — currently declared but not used for P2P; may be flagged by Play review

### Store Listing ✅ (from STORE_PRESENCE.md)
- [x] App name: "Blukit - Offline Mesh Chat"
- [x] Short description prepared
- [x] Full description prepared
- [x] Icons and assets in `doc/assets/store/`
  - `blukit_icon_512x512.png` (512×512)
  - `feature_graphic_1024x500.png` (1024×500)
  - `screenshot_chat_phone.png`
  - `screenshot_profile_stealth.png`
  - `screenshot_radar_phone.png`
  - `screenshot_radar_tablet.png`

### Data Safety Declaration ✅ (from DATA_SAFETY.md)
- [x] "No data collected" — all local
- [x] "No data shared with third parties" — no SDKs, no servers
- [x] "Data encrypted in transit" — AES-256-GCM + ECDH
- [ ] Submit questionnaire in Play Console

### Content Rating & Moderation ✅
- [x] Target rating: Everyone (3+)
- [x] UGC moderation via Block/Mute on chat bubbles
- [x] "No violence/sexual content" declarations ready

### Testing Requirements ⏳
- [ ] **Closed Testing:** Minimum 20 opted-in testers, minimum 14 days continuous testing (Google's mandatory requirement for first-time publishers)
- [ ] Upload `.aab` to Closed Testing track
- [ ] Monitor pre-launch report for ANR rate (< 0.47%)
- [ ] Request Production access after 14-day period

---

## 8. Implementation Priority Roadmap

### Phase P1: Critical Fixes (Do First) ⚡

| # | Task | Effort | Impact |
|---|------|--------|--------|
| P1-1 | Fix `RadioStateManager` — use modern Bluetooth API (API 31+ compatible) | 1-2 hours | Fixes crash on Android 12+ |
| P1-2 | Wire `DiscoveryScreen` into navigation instead of raw `RadarScreen` | 30 min | Permissions properly requested |
| P1-3 | Fix `CryptoManager` — add GCM auth tag validation (prevent spoofed payloads) | 1 hour | Security vulnerability closed |
| P1-4 | Add `connectedPeers: StateFlow<Set<String>>` to P2PController interface | 2 hours | Mesh networking correctly represented |

### Phase P2: Stadium Lobby (Core Missing Feature) 📢

| # | Task | Effort | Impact |
|---|------|--------|--------|
| P2-1 | Create `LobbyScreen.kt` with multi-peer message display | 3-4 hours | Core feature now exists |
| P2-2 | Add `Route.Lobby` navigation and FAB trigger from Radar screen | 1 hour | Users can enter lobby mode |
| P2-3 | Update `sendMessage(null)` → `broadcastToAllPeers()` in NearbyP2PController | 1-2 hours | Public broadcast actually works |
| P2-4 | Wire Lobby messages into Room DB as "lobby" messages with sender metadata | 1 hour | Messages persist and display correctly |

### Phase P3: UX Polish & ChatScreen Fixes 💬

| # | Task | Effort | Impact |
|---|------|--------|--------|
| P3-1 | Add peer identity display in ChatScreen TopAppBar (name, emoji, signal) | 1 hour | Users know who they're talking to |
| P3-2 | Add delivery status badges (sent ✓✓ / delivered ✓ / pending ⏳) | 1-2 hours | Message feedback loop |
| P3-3 | RSSI-based radar positioning instead of circular index layout | 2 hours | Radar accurately shows proximity |
| P3-4 | Chat input: max lines = 6 with vertical resize, IME handling | 30 min | Prevents overflow UX issues |

### Phase P4: Contact Management & History Control 📋

| # | Task | Effort | Impact |
|---|------|--------|--------|
| P4-1 | Create `ContactRepository` and wire contacts table in NearbyP2PController | 2 hours | Persistent contact list |
| P4-2 | Create `ContactsScreen.kt` showing known peers with last-seen + quick-chat | 3 hours | Discover past connections |
| P4-3 | Add "Clear Chat History" button in Profile/Settings | 1 hour | User privacy control |
| P4-4 | Add emoji picker (scrollable grid, 20+ emojis) | 1-2 hours | Better identity expression |

### Phase P5: Security Hardening & Production Prep 🔒

| # | Task | Effort | Impact |
|---|------|--------|--------|
| P5-1 | Replace SHA-256 ECDH derivation with HKDF | 1 hour | Stronger key derivation |
| P5-2 | Add message deduplication via messageId Set in ViewModel | 30 min | Prevents duplicate messages on mesh |
| P5-3 | Remove unused Camera/Retrofit/OkHttp/Hilt dependencies from build.gradle.kts | 30 min | Reduce APK size, clean build |
| P5-4 | Review and remove `INTERNET` permission if not needed | 30 min | Cleaner Play Store Data Safety |
| P5-5 | Replace `fallbackToDestructiveMigration()` with proper Room Migrations | 2 hours | Data preservation across updates |
| P5-6 | Sync i18n strings across all translation files | 2 hours | All languages complete |

---

## Appendix A: Missing Code Snippets (Ready to Paste)

### A.1 New Route Entry (Routes.kt addition)

```kotlin
@Serializable
data object Lobby : Route
```

### A.2 P2PController Interface Extension

Add these to the `P2PController` interface:
```kotlin
val connectedPeers: StateFlow<Set<String>>
fun broadcastLobbyMessage(content: String, nickname: String): List<MessagePayload>
fun getPeerInfo(endpointId: String): PeerInfo?
data class PeerInfo(val id: String, val name: String?, val signalStrength: Int)
```

### A.3 BluetoothViewModel Lobby Methods

Add to `BluetoothViewModel`:
```kotlin
fun startLobbyBroadcast() {
    p2pController.startAdvertising()  // already exists
}

fun broadcastToLobby(message: String) {
    viewModelScope.launch {
        val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
        p2pController.broadcastLobbyMessage(message, nickname)
    }
}
```

### A.4 ChatScreen Signature Update

Change from:
```kotlin
@Composable fun ChatScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onEnterPip: () -> Unit
)
```

To:
```kotlin
@Composable fun ChatScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    peerId: String,       // NEW
    peerName: String?,   // NEW  
    peerEmoji: String?,  // NEW
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onEnterPip: () -> Unit
)
```

### A.5 ProfileScreen "Clear History" Button Addition

Add after the Stealth Mode toggle in `ProfileScreen.kt`:
```kotlin
// Clear Chat History
TextButton(
    onClick = { 
        /* call viewModel.clearHistory() */
    },
    modifier = Modifier.fillMaxWidth()
) {
    Text(stringResource(R.string.profile_clear_history))
}

// Logout / Reset Profile
TextButton(
    onClick = { 
        repository.clearNickname()  // triggers navigation to onboarding
    },
    modifier = Modifier.fillMaxWidth()
) {
    Text(stringResource(R.string.profile_logout))
}
```

---

*This document merges the original Blukit Master Architecture Blueprint, Play Store Publishing Blueprint, all .agent system directives, plan.md, and every source file in the codebase. All "verified" items have been confirmed against actual code. All gaps are documented with specific fixes.*
