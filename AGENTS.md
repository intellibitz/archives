# Agent Workspace Notes 🤖

This file serves as a hand-off document for AI agents working on Blukit.

## 🏁 Current State (2026-09-01)
We have completed Phase 3: Terminology Normalization. The app is now jargon-free and uses natural language for all user segments.

### **Key Improvements**
- **Navigation Unified**: `NavigationViewModel` is the single source of truth for the `backStack`.
- **Onboarding Route**: Identity creation is a first-class route (`Route.Onboarding`).
- **Multi-Pane Detail Placeholders**: Tablets/Foldables now show a "Select a person" prompt.
- **Natural Terminology**: Removed all "Ghost", "Ritual", "Source", and "Emit/Whisper" terminology. Replaced with "Assistant", "Setup", "Person", and "Message/Send".
- **Resource Localization**: Migrated 95% of hardcoded strings to `strings.xml`.

## ⚠️ Known Issues / Technical Debt
- **Re-composition Optimization**: `LocalPersonaCoordinates` still provides a global map; consider narrowing its scope.
- **Theme Consistency**: "Stealth Mode" remains in the code as a branding element, but UI strings use "Power Saving".


## 🛠️ Testing Strategy
Refer to `ARCHITECTURE.md` Section 6 for commands.
- **Unit Tests**: `app/src/test/java/cc/thevar/blukit/ui/viewmodels/NavigationViewModelTest.kt`
- **Compose Previews**: Use `MarketPreviews.kt` for visual regression checks.

## 🎯 Next Tasks for Agents
1. **Increase Test Coverage**: Write unit tests for `MainViewModel` and `ConnectionViewModel`.
2. **Resource Localization**: Move all hardcoded strings to `strings.xml`.
3. **Refine Adaptive UI**: Use `MediaQuery` to adjust padding and font sizes on extremely small or large displays.
