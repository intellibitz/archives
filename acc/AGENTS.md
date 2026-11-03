# AI Agent Interaction Guide

Welcome to the **acc** codebase. This project uses a modern, modular architecture designed for high performance and testability.

## 🏗️ Architecture Overview
- **Dependency Injection**: We use **Koin** for both the Gateway and Frontend.
  - Gateway: Registered in `GatewayModule.kt`.
  - Frontend: Shared ViewModels in `CommonModule.kt`.
- **Control Plane (Gateway)**: A modular Ktor server.
  - `Routing.kt`: Modular route definitions.
  - `MonitoringService.kt`: Lifecycle-aware metrics and WebSocket management.
  - `ProvisioningService.kt`: Resource-safe model downloads (Ktor-native) and registration.
  - `AgentService.kt`: Kotlin-native integration with local and cloud LLMs.
- **Frontend (Deck)**: Compose Multiplatform.
  - Responsive layouts in `App.kt` with breakpoints for Desktop/Mobile.
  - **Platform-Aware Host Discovery**: The `Platform` interface provides default gateway hosts (e.g., `10.0.2.2` for Android emulators, `window.location.hostname` for Web).
  - Type-safe communication via shared `:common` protocols.

## 🧪 Testing Strategy
We follow a multi-layered testing approach. Detailed documentation can be found in [docs/testing.md](docs/testing.md).

### Key Test Suites:
- **Protocol Tests**: `:common:jvmTest`
- **Gateway Service Tests**: `:gateway:test` (uses MockK and Ktor MockEngine)
- **Frontend Build Verification**: `./gradlew :frontend:desktop:assemble`

## 🚀 Release Management
We use a semi-automated release system triggered by Gradle and completed by GitHub Actions.

### Release Tasks:
- **Test Release**: `./gradlew releaseTest`
  - Increments version (e.g., `1.0.0` -> `1.0.1-test.1` or `1.0.1-test.1` -> `1.0.1-test.2`).
  - Tags and pushes to GitHub.
  - Triggers a **Pre-release** on GitHub with a Fat JAR.
- **Production Release**: `./gradlew releaseProduction`
  - Promotes test to production or increments version (e.g., `1.0.1-test.2` -> `1.0.1` or `1.0.1` -> `1.0.2`).
  - Tags and pushes to GitHub.
  - Triggers a **Full Release** on GitHub.

### ⚠️ Mandatory Creator Workflow Rule
> **STRICT RULE FOR ALL ACC CREATORS (AI Agents, Human Developers, and Contributors):**
> **All Git and GitHub collaboration operations MUST be executed via Gradle automation tasks ONLY.**
> Direct execution of manual `git` or `gh` CLI commands for branch management, PR creation, syncing, merging, or branch cleanup is prohibited. Every creator (you, me, everyone) must follow the standardized Gradle workflow so that GitHub collaboration and branch lifecycles are 100% managed by our Kotlin-native automation tasks.

### GitHub Creator Tasks (Modular Automation)
We use a modular automation engine in [github-automation.gradle.kts](github-automation.gradle.kts) to manage the repository lifecycle. These tasks ensure a clean, linear history and "Zero Effort" maintenance.

- **Smart Sync**: `./gradlew githubSync`
  - Keeps your current branch updated with `remote` and `main`.
  - Automatically captures local work and rebases on the latest `origin/main`.
  - Handles accidental work on `main` by moving it to a `sync/` branch.
- **Merge Current**: `./gradlew githubMerge`
  - Syncs, updates from `main`, ensures a PR exists, and enables **Auto-merge**.
- **Merge All**: `./gradlew githubMergeAll`
  - The "Elite Maintenance" task. Bulk-syncs and enables auto-merge for all open PRs.
- **Return to Main**: `./gradlew githubMain`
  - Safely saves current work to GitHub and returns your local environment to a fresh, hard-reset `main` branch.
- **New Feature**: `./gradlew githubFeature -Pname=my-feature`
  - Creates a timestamped feature branch and opens a PR immediately.
- **Repository Utilities**:
  - `githubSetup`: Configures the repo for optimal workflow (auto-merge, branch deletion).
  - `githubOpen`: Opens the repository in your browser.
  - `githubChecks`: Watches CI progress for the current branch.
  - `githubFixAll`: Attempts to fix failed PRs by updating branches and rerunning CI.

### 📌 ACC Dedicated Port Rule & Policy
> **MANDATORY RULE:** All ACC components MUST use dedicated, unique non-conflicting ports:
> - **`8333`** — ACC Gateway HTTP Server & Web UI (`http://localhost:8333`)
> - **`8334`** — ACC Gateway HTTPS Secure Server (`https://localhost:8334`)
> - **`8335`** — ACC Standalone Web Development Server (Wasm/JS Dev) (`http://localhost:8335`)
> Components, ViewModels, and documentation MUST adhere to these exact ports and MUST NOT fall back to generic ports (`8080`, `8081`, `8443`).

### Deployment for Pilots:
Users can simply download the `acc-gateway-x.y.z.jar` from the GitHub Release and run:
`java -jar acc-gateway-x.y.z.jar`
The server and web frontend will be served at `http://localhost:8333`.

## 🛠️ Development Guidelines
- **Koin first**: Avoid global singletons. Inject dependencies via constructors or Koin DSL.
- **Lifecycle Management**: Core services (Supervisor, Provisioning, Monitoring) implement `AutoCloseable`. Always ensure background coroutines and processes are canceled on `close()`.
- **Stay Modular**: When adding new features to the Gateway, create a new `Routes` file or Service instead of bloating `Application.kt`.
- **Structured Logging**: Use **SLF4J** (`LoggerFactory.getLogger(javaClass)`) instead of `println`.
- **Zero Silent Failures**: Never use empty `catch` blocks. Log errors at the appropriate level (WARN/ERROR).
- **Test-Driven**: Always add unit tests for new business logic.

---
*Maintained by the AI Command Center Architects.*
