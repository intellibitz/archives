# AI Agent Handover

Welcome to the Acc cockpit. Here is the current architectural status as of late 2026.

## 🗝️ Core Security Rule
**DO NOT allow arbitrary command execution via WebSockets.**
Command execution is handled by `cc.thevar.acc.service.CommandHandler`, which enforces strict whitelisting. Privileged operations are managed by the `SystemBootstrapper` or restricted to local-only access to maintain security boundaries.

## 🏗️ The Kotlin-Native Pattern
Acc is a **Pure Kotlin-Native Intelligence Layer**.
- **Tech Stack (Late 2026)**: Kotlin 2.3.20, Ktor 3.0.3, Compose 1.10.1, Koin 4.0.1.
- **Self-Contained Gateway**: The cockpit is a single project directory containing the Manager (Kotlin) and the UI (Wasm).
- **Zero Footprint**: The user's system remains untouched outside the project folder. All state is maintained within `data/`, `registry/`, and `logs/`.

## 🛠️ The Creator Workflow (Zero-Effort CI/CD)
1.  **Develop**: Make changes in a feature branch.
2.  **Verify**: Run `./gradlew :gateway:run`.
3.  **Release**: Push tags to trigger the distribution build.

## 🧪 Ongoing Missions
- [x] Pivot to **"Folder-Sandbox" Architecture**.
- [x] Implement integrated `acc uninstall` (Safe data purge).
- [x] distribution artifacts support.
- [x] Purge all legacy Docker, Python, Bash and PowerShell technical debt.
- [x] 0 Shell Dependency Architecture (Migrated all scripts to Kotlin).
- [x] Engine-on-Demand: Control local engines (Ollama) from the UI via APIs.
- [x] API-driven Zero-Footprint Provisioning.
- [ ] Implement integrated E2E journey tests for the Provisioning flow.

See [../testing.md](../testing.md) for verification commands.
