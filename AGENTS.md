# Agent Workspace Notes 🤖

This file serves as a hand-off document for AI agents working on Blukit.

## 🏁 Current State (2026-09-01)
We have just completed a "Full Architecture Surgery" to modularize the main app entry and fix performance bottlenecks.

### **Key Improvements**
- **Monolith Broken**: `BlukitApp.kt` is no longer a God Composable. It's split into `BlukitScaffold` and `BlukitNavGraph`.
- **Adaptive Fix**: `ListDetailSceneStrategy` is implemented. The app now supports side-by-side panes on tablets/foldables for the Nearby -> Chat flow.
- **Paging Optimized**: `collectAsLazyPagingItems()` was moved from the root to individual screen level (`TimelineField`, `GroupField`, `LiveFeedField`).
- **Navigation 3 Unified**: Backstack is managed via `NavigationViewModel.backStack` (a `mutableStateListOf`).

## ⚠️ Known Issues / Technical Debt
- **Shared State**: `isSearchActive` is currently passed down manually; consider moving to a UI state holder or `CompositionLocal` if it grows.
- **Test Coverage**: Initial unit tests added for `NavigationViewModel`. More coverage is needed for `ConnectionViewModel` and `HarmonyViewModel`.
- **Hardcoded Strings**: Many UI strings are still hardcoded in Composables.

## 🛠️ Testing Strategy
Refer to `ARCHITECTURE.md` Section 6 for commands.
- **Unit Tests**: `app/src/test/java/cc/thevar/blukit/ui/viewmodels/NavigationViewModelTest.kt`
- **Compose Previews**: Use `MarketPreviews.kt` for visual regression checks.

## 🎯 Next Tasks for Agents
1. **Increase Test Coverage**: Write unit tests for `MainViewModel` and `ConnectionViewModel`.
2. **Resource Localization**: Move all hardcoded strings to `strings.xml`.
3. **Refine Adaptive UI**: Use `MediaQuery` to adjust padding and font sizes on extremely small or large displays.
