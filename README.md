# IntelliBitz Archives

<p align="center">
  <strong>AI-Centric Knowledge Base, Uncapped Agent Platform & Engineering Ecosystem</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Focus-AI%20%26%20Agents-7F52FF.svg" alt="Focus: AI & Agents" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT" />
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF.svg" alt="GitHub CI" />
  <img src="https://img.shields.io/badge/Docker-Compose%20v2%20%7C%20Alpine-2496ED.svg" alt="Docker" />
  <img src="https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Python-3.14%20%7C%20uv-3776AB.svg" alt="Python" />
</p>

---

## 🤖 Overview

**IntelliBitz Archives** is an **AI-centric monorepo, agent platform, and engineering knowledge base**. The primary domain of the repository is focused on **AI foundation models, autonomous agents, evaluation harnesses, cost benchmarking, and multi-provider AI architectures**.

The repository is structured around **AI as the primary top-level directory**, with classic infrastructure, runtimes, and services organized under `engineering/`:

```text
archives/
├── ai/            # 🧠 PRIMARY AI KNOWLEDGE BASE & GUIDES (Agent Architecture, Benchmarks, Local Execution)
├── router-ledger/ # 📊 Router Ledger AI model pricing telemetry & fallback architecture
├── deepseek/      # DeepSeek-V3/R1 evaluation harnesses, API setup & real-world cost telemetry
├── openrouter/    # OpenRouter unified model router & multi-provider fallback
├── chutes/        # Chutes AI decentralized serverless GPU compute platform
├── qwen/          # Alibaba Qwen 2.5 Coder & instruct models
├── openai/        # GPT-4o, o1, o3-mini & OpenAI Codex
├── claude/        # Anthropic Claude 3.5 Sonnet & Opus
├── google-ai/     # Gemini 1.5 Pro & Gemini 2.0 Flash
├── mistral/       # Mistral Large, Codestral & Ministral
├── zhipu-glm/     # Zhipu GLM-4 series
├── agnes-ai/      # Agnes AI agent configurations
├── inclusion-ai/  # Inclusion AI core models
├── xiaomi-mimo/   # Xiaomi MiMo models
└── engineering/   # ⚙️ CLASSIC ENGINEERING & INFRASTRUCTURE
    ├── clients/   # User interfaces & mobile clients (Android, Multiplatform, Java SE)
    ├── data/      # Databases & SQL schemas (PostgreSQL, MySQL, SQLite, rqlite)
    ├── docs/      # Technical documentation, Linux maintenance & system fixes
    ├── infra/     # DevOps, Docker, K8s, Git, Gradle & cloud automation
    ├── runtimes/  # OS runtimes & language environments (Go, Python, Shell, Clojure)
    └── services/  # Enterprise backends & web servers (Java EE, Python Web, Nginx)
```

---

## 🧠 Top-Level AI Ecosystem & Knowledge Base

All primary AI model provider hubs, benchmark harnesses, agent launchers, and cost optimizations live at top level:

* 🤖 **[Uncapped Agent Architecture](./ai/UNCAPPED_AGENT_ARCHITECTURE.md)** — Guide on building 24/7 high-throughput autonomous agent pipelines without weekly token caps or seat lockouts.
* 🟢 **[NVIDIA 128GB Hardware Guide](./ai/NVIDIA_128GB_HARDWARE_GUIDE.md)** — Hardware guide & benchmark specs for running 70B+ models locally on NVIDIA Jetson AGX Orin 128GB & GH200 workstations.
* ⚡ **[CachyOS Local AI Setup](./ai/CACHYOS_LOCAL_AI_SETUP.md)** — Hardware setup, systemd optimizations, and vLLM / llama.cpp benchmarks.
* 📈 **[DeepSeek Real-World Cost Telemetry](./deepseek/real_world_cost_study.md)** — Empirical case study analyzing 626M tokens processed for **$3.25 USD** with a 99.49% prompt cache hit ratio.
* 📊 **[Master Cost Ranking](./ai/COST_RANKING.md)** — Comprehensive cost ranking across all 10 tracked AI providers.
* 🏆 **[Global AI Leaderboard](./ai/TOP_100_LEADERBOARD.md)** — Top 100 frontier AI models ranked by capability, reasoning, and coding benchmarks.
* 🛠️ **[IDEs & Desktop AI Tools](./ai/IDE_PLUGINS_DESKTOP.md)** — Complete setup guide for Cursor, Claude Desktop, Codex Desktop, DeepSeek Harness (`dsh`), OpenHands, and Roo Code.
* 💻 **[Local Execution Guide](./ai/LOCAL_EXECUTION.md)** — Guide to running open-weights models (DeepSeek-R1, Qwen 2.5 Coder) locally using Ollama and vLLM.
* 🔑 **[BYOK & API Key Management](./ai/BYOK_API_KEYS.md)** — Security best practices for configuring direct API keys.
* 🌟 **[Top 100 GitHub AI Projects](./ai/TOP_100_GITHUB_PROJECTS.md)** — Curated directory of top open-source AI repositories.

---

## ⚙️ Engineering & Infrastructure (`engineering/`)

Classic systems, cloud automation, and client application modules are organized under [`engineering/`](./engineering/):

* 📱 **[Clients (`engineering/clients/`)](./engineering/clients/)**: Flagship Android [IntelliDroid](./engineering/clients/android/IntelliDroid/README.md), Kotlin modules ([MEvents](./engineering/clients/android/MEvents/README.md), [TwRends](./engineering/clients/android/TwRends/README.md), [UDigg](./engineering/clients/android/UDigg/README.md)), and web frontends.
* 🗄️ **[Data & SQL (`engineering/data/`)](./engineering/data/)**: Containerized PostgreSQL, MySQL, SQLite, and rqlite database stacks.
* 📖 **[Docs & System Fixes (`engineering/docs/`)](./engineering/docs/)**: CachyOS / Arch Linux maintenance guide ([SYSTEM_FIXES_AND_OPTIMIZATIONS.md](./engineering/docs/cachyos/SYSTEM_FIXES_AND_OPTIMIZATIONS.md)).
* 🚀 **[Infrastructure (`engineering/infra/`)](./engineering/infra/)**: Docker Compose, Kubernetes, Google Cloud, and Gradle automation.
* 💻 **[Runtimes (`engineering/runtimes/`)](./engineering/runtimes/)**: Python, Shell Linux, Go, and Clojure toolchains.
* ☁️ **[Services (`engineering/services/`)](./engineering/services/)**: Java EE enterprise backends and reverse proxy web servers.

---

## Technology Roadmap

See [`roadmap.md`](./roadmap.md) for active component milestones and future development plans.

---

## Contributing & Community

Contributions are warmly welcomed! Please review:

- [Contributing Guidelines](./CONTRIBUTING.md)
- [Contributor Code of Conduct](./CODE_OF_CONDUCT.md)
- [Security Policy](./SECURITY.md)

---

## License

This repository is licensed under the [MIT License](./LICENSE).
