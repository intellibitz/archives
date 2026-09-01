# Agent Workspace Notes 🤖

This file serves as a hand-off document for AI agents working on Blukit.

## 🏁 Current State (2026-09-01)
We have completed Phase 2 of the architecture refactor, focusing on navigation authority and adaptive UX.

### **Key Improvements**
- **Navigation Unified**: `NavigationViewModel` is now the single source of truth for the `backStack`. `BlukitNavGraph` consumes the VM's `SnapshotStateList`, eliminating "dual source of truth" sync issues.
- **Onboarding Route**: Identity creation is now a first-class route (`Route.Onboarding`). This removes layout jumping and "scaffold hijacking" that occurred when checking for nickname at the root level.
- **Multi-Pane Detail Placeholders**: Added `DetailPlaceholder` for expanded layouts (Tablets/Foldables), ensuring a polished UX when no conversation is selected.
- **Permission Guard Refined**: Permission checks are now bypassed for the Onboarding route to ensure users can set up their identity even without system permissions.

## ⚠️ Known Issues / Technical Debt
- **Re-composition Optimization**: `LocalPersonaCoordinates` still provides a global map; consider narrowing its scope.
- **Hardcoded Strings**: Breadcrumbs and placeholders still use hardcoded strings; localization is needed.


## 🛠️ Testing Strategy
Refer to `ARCHITECTURE.md` Section 6 for commands.
- **Unit Tests**: `app/src/test/java/cc/thevar/blukit/ui/viewmodels/NavigationViewModelTest.kt`
- **Compose Previews**: Use `MarketPreviews.kt` for visual regression checks.

## 🎯 Next Tasks for Agents
1. **Increase Test Coverage**: Write unit tests for `MainViewModel` and `ConnectionViewModel`.
2. **Resource Localization**: Move all hardcoded strings to `strings.xml`.
3. **Refine Adaptive UI**: Use `MediaQuery` to adjust padding and font sizes on extremely small or large displays.
