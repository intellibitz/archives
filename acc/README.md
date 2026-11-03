# 🚀 AI Command Center (acc)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform: Universal](https://img.shields.io/badge/Platform-Linux%20%7C%20macOS%20%7C%20Windows-blue.svg)](https://github.com/intellibitz/acc)
[![Engine: Backend-Agnostic](https://img.shields.io/badge/Engine-Ollama%20%7C%20LocalAI%20%7C%20vLLM-blueviolet.svg)](https://litellm.ai/)
[![Frontend: Kotlin Multiplatform](https://img.shields.io/badge/Frontend-Compose%20Wasm%20%7C%20Android%20%7C%20Desktop-green.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Stack: Kotlin 2.3.20 | Ktor 3.0.3 | Compose 1.10.1](https://img.shields.io/badge/Stack-Kotlin%202.3.20%20%7C%20Ktor%203.0.3%20%7C%20Compose%201.10.1-blue.svg)](https://github.com/intellibitz/acc)

**AI Command Center (acc)** is the elite orchestration environment for AI Agents and LLMs. It
transforms your workstation into a professional-grade AI cockpit with **zero-effort** provisioning,
real-time visual monitoring, and a high-performance **Kotlin Multiplatform** frontend.

Run any open-source model—from Phi-3 to Llama 3.1 70B—with a single command and watch their "
thoughts" stream in real-time.

---

## 🌟 Vision: Zero Effort, Infinite Intelligence

acc is designed for the developer who wants to *use* AI, not fight with dependencies.

- **Unified Visual Interface**: A high-performance Compose Multiplatform app (Wasm, Android,
  Desktop) for real-time agent observability.
- **Thought Stream**: Visualize the agent's internal reasoning process (the "hidden thoughts") as they happen.
- **Tool Visualization**: Transparently monitor tool calls (shell commands, file edits) in a terminal-style interface.
- **On-Demand Spawning**: Instantly spawn specialized agent services for any model in your fleet.
- **Visual Dashboard**: Real-time TUI tracking CPU, RAM, and hardware metrics (NVIDIA CUDA / Apple
  Silicon) via the **HWT** (Hardware Tuner).
- **Backend Agnostic**: Native support for **Ollama**, **LocalAI**, and **vLLM**, plus integration
  with cloud providers like **OpenAI**, **Anthropic**, and **Gemini**.
- **Multi-Engine Fleet**: Mix and match local and cloud models in a unified interface.
- **Private Fleet**: Separate your paid/private models using the git-ignored
  `config/private_fleet.conf`.
- **Smart Provisioning**: Resilient multi-connection downloads via **HF-CLI** with automatic
  registration via **Ollama HTTP API**.
- **Sandboxed Security**: Hardened console with strict API-level command white-listing, preventing
  host-level Remote Code Execution (RCE).

---

## 🏗️ Project Structure (Standard Format)

acc is organized into a clean, professional architecture:

- **`gateway/`**: The **Manager** (Control Plane). A modular Ktor hub powered by **Koin DI**. It supervises all workers, handles provisioning (Kotlin-native), and serves the UI. Features modular routing, a dedicated `MonitoringService` for real-time telemetry, and a hardened `CommandHandler`.
- **`frontend/`**: The **Deck** (UI). A Compose Multiplatform app providing real-time observability. Powered by **Koin DI** for cross-platform ViewModel management. Features an **Adaptive UI** that automatically scales from mobile to desktop.
- **`brain/`**: The **Intelligence** layer. Native Kotlin agent services for high-performance reasoning.
- **`common/`**: The **Registry**. Shared type-safe protocols and models ensuring
  backend-to-frontend integrity, verified by automated JVM tests. Includes **HWT** and **Provisioner
  ** logic.
- **`registry/`**: The **Vault**. Central storage for engine-neutral manifests, model parameters,
  and provider configurations (e.g., Ollama Modelfiles, LocalAI YAMLs).
- **`tooling/`**: The **Workshop**. Low-level scripts for project maintenance, verification, and benchmarking.
- **`docs/`**: The **Library**. Comprehensive documentation, including architectural deep-dives (`/architecture`) and testing guides.

---

## 🚀 Quick Start (Zero-Effort AI Cockpit)

`acc` is a single, self-contained Kotlin application. No Python, no Docker, no complicated setup.

1. **Download**: Get the latest `acc-gateway-*.jar` from [GitHub Releases](https://github.com/intellibitz/acc/releases).
2. **Launch**: Run with Java 21:
   ```bash
   java -jar acc-gateway-1.0.0.jar
   ```
3. **Explore**: Open **`http://localhost:8333`** in your browser to access your AI Command Center.

### Prerequisites
- **JDK 21+**: The only requirement to run the cockpit.
- **Ollama**: (Optional) For high-performance local LLM execution.

---

## 🏗️ Pure Kotlin Architecture

`acc` has evolved into a unified, high-performance environment:

1. **Manager (Gateway)**: A robust Ktor server that bootstraps the environment, manages system metrics (via OSHI), and serves the visual dashboard.
2. **Deck (UI)**: A high-performance Compose Multiplatform interface compiled to WebAssembly (Wasm) for the browser.
3. **Intelligence (Agent Service)**: Native Kotlin integration with local and cloud LLMs.
4. **Zero Footprint**: Everything is contained within the `acc` directory. Deleting the folder removes everything.

## 🛠️ Orchestrator Commands

In Kotlin-native mode, all orchestration is handled via the **Visual Dashboard** or the **Built-in Console**.

### 🎮 User (The Pilot)
Most operations like `sync`, `prune`, `backup`, and `auto-scale` are available directly in the Dashboard.

---

## 🛡️ Self-Healing & Secure Architecture

acc is built like a high-performance process orchestrator:

- **Modular Control Plane**: The Gateway uses **Koin Dependency Injection** and **AutoCloseable** service lifecycles to ensure high testability, clean resource management, and zero "zombie" processes.
- **API Orchestration**: All model management is handled via secure HTTP APIs to engines (Ollama/vLLM), rather than brittle CLI calls.
- **Resilient Telemetry**: A dedicated `MonitoringService` manages WebSocket streams with structured error handling and SLF4J logging, ensuring metrics reach the UI reliably.
- **Auto-Heal & Forced Kill**: If a worker crashes, the Manager restarts it. Unresponsive workers are forcibly terminated to reclaim system resources.
- **Console Sandbox**: The terminal interface is strictly restricted to non-privileged orchestration
  tasks. Sensitive operations like `setup` or `tune-hw` are host-only to prevent unauthorized system
  modification.
- **Secret Management**: Sensitive data, such as keystore passwords, are managed via environment
  variables (`ACC_KEYSTORE_PASSWORD`).
- **Bootstrap UI**: In fresh environments, the Manager serves a live "Bootstrap Page" showing
  real-time build and setup progress until the cockpit is ready.

## 🧪 Reliability & Testing

acc maintains high stability through a multi-layered testing strategy:

- **Protocol Integrity**: Automated tests in `:common` verify that the JSON communication between the Kotlin Gateway and the Compose UI never breaks.
- **Environment Audit**: The Manager performs a pre-flight health check of your toolchain on startup.
- **Visual Parity**: UI components are developed with Material 3 tokens to ensure consistent
  rendering across Wasm, Android, and Desktop.

---

## 🛠️ Prerequisites

- **OS**: Linux, macOS, or Windows.
- **JDK 21+**: To run the universal JAR.
- **Ollama**: For local model support.

---

## 🤝 Contributing

acc is built for the community. If you have ideas for better orchestration or new model superpowers,
check out our [CONTRIBUTING.md](CONTRIBUTING.md).

### 🛠️ Creator Workflow

> **⚠️ Mandatory Rule for All ACC Creators (you, me, everyone):**
> **All Git and GitHub tasks MUST be executed using Gradle automation tasks ONLY.**
> All creators (AI agents, human developers, and contributors) follow the automated Gradle workflow so GitHub collaboration, branch hygiene, and merge lifecycles are consistently managed by Kotlin-native Gradle tasks.

1. **Run Locally**: `./gradlew :gateway:run`
2. **Start Feature**: `./gradlew githubFeature -Pname=MyFeature`
3. **Smart Sync**: `./gradlew githubSync` (Captures local work and rebases on latest `main`).
4. **Automated Merge**: `./gradlew githubMerge` (Creates PR, enables auto-merge, and prunes merged branches).
5. **Test Release**: `./gradlew releaseTest` (Increments version and triggers pre-release).
6. **Production Release**: `./gradlew releaseProduction` (Promotes to stable release).

For deep-dives into the architecture or modular automation system, see [AGENTS.md](AGENTS.md).

## 🛡️ License

Distributed under the MIT License. See `LICENSE` for more information.

---
*Built with ❤️ for the AI Community. Unlock your hardware's true potential.*
