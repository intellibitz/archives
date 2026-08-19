# Unified Persona Hub & Noise Filter Implementation Plan

The goal is to refine the Persona Hub experience by making personas more identifiable, improving the Direct Message (1-1/Whisper) UX, and merging the "Focus" logic into a "Noise Filter" within the "All" view.

## User Review Required

> [!IMPORTANT]
> The "Noise Filter" will replace the current binary Focus toggle. It will be more prominently labeled to help users understand they are filtering "noise" (distant or unknown vibes).

> [!NOTE]
> All personas (active and dots) will now display a truncated name and the blukit icon for universal identification.

## Proposed Changes

### [Component] Core Models & Logic
#### [MODIFY] [P2PDevice.kt](file:///home/ramadoss/Projects/AI/Blukit/core/src/main/java/cc/thevar/blukit/domain/model/P2PDevice.kt)
- Add any necessary fields if required for filtering (though signal strength exists).

### [Component] UI Components
#### [MODIFY] [SharedComponents.kt](file:///home/ramadoss/Projects/AI/Blukit/app/src/main/java/cc/thevar/blukit/ui/screens/SharedComponents.kt)
- **VibeNode**: Refine layout to ensure name and blukit icon are always visible and balanced.
- **VibeDot**: Update to include a tiny name label below and the blukit icon on the corner.
- **UnifiedPersonaCloud**: Adjust layout to accommodate names for all personas.
- **PersonaOptionsMenu**:
    - Update "1-1" button icon and label to convey "Whisper/Side-Vibe".
    - Use `Icons.Rounded.Hearing` or a similar icon for 1-1.

#### [MODIFY] [BlukitApp.kt](file:///home/ramadoss/Projects/AI/Blukit/app/src/main/java/cc/thevar/blukit/ui/BlukitApp.kt)
- **VisualEnergyPicker**: Change 1-1 tab icon to `Icons.Rounded.Hearing`.
- **BlukitHub**:
    - Rename `isFocusFilterActive` to `isNoiseFilterActive`.
    - Update the filter button UI to be more explicit about "Noise Filtering".
- **UnifiedBlukitBadge**: Add a "DENY" action next to "JOIN" for incoming link requests.

### [Component] Handshake & Logic
#### [MODIFY] [BluetoothViewModel.kt](file:///home/ramadoss/Projects/AI/Blukit/app/src/main/java/cc/thevar/blukit/ui/viewmodels/BluetoothViewModel.kt)
- Ensure `denyLink` is correctly wired and handles the UI state.

---

## Verification Plan

### Automated Tests
- Run `UnifiedPersonaCloudTest.kt` to ensure personas still render correctly.
- Run `NavigationTest.kt` to verify the "All" tab still works with the new filtering logic.

### Manual Verification
- Deploy to device and verify:
    - All personas in the cloud show a name and the blukit icon.
    - Tapping the "Noise Filter" correctly hides/shows non-vibed/distant peers.
    - Direct Message (1-1) icon is updated.
    - Incoming vibe requests show both "JOIN" and "DENY".
