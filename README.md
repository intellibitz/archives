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

> [!IMPORTANT]
> ### 🏆 Undisputed AI Economics & Performance Winner: DeepSeek V3 / R1
> **Unbeatable Agentic Economics: ~$3.00 – $6.00 USD per 1 Billion Cached Tokens**
>
> Across empirical cost benchmarks and real-world telemetry in the **IntelliBitz Archives** platform, **[DeepSeek](https://www.deepseek.com)** has clearly emerged as the **#1 overall winner** for building autonomous agents, coding assistants, and high-throughput LLM pipelines:
>
> * 💰 **Breakthrough Pricing:** **~$3.00 to $6.00 USD per 1 Billion cached input tokens** ($0.003–$0.014 / 1M tokens), thanks to 90%+ prompt cache hit ratios.
> * 📉 **15x–50x Cheaper than Competition:** Processing 1 Billion tokens on DeepSeek costs **$3.25–$6.00**, compared to **$48.11** on GPT-4o-mini, **$56.41** on Claude 3.5 Haiku, and **$150+** on Claude 3.5 Sonnet / o1.
> * 🧠 **Frontier Reasoning & Coding Capabilities:** DeepSeek-R1 rivals OpenAI o1 on reasoning benchmarks (AIME, MATH-500) at ~1/25th the cost; DeepSeek-V3 matches top proprietary models on SWE-bench and LiveCodeBench.
> * 🎯 **Clear Choice / Zero-Confusion Default:** For all agent harnesses, IDE plugins, desktop tools, and serverless workflows in this repository, **DeepSeek is the default recommended provider**.
>
> 📈 **[Read the 626M Token Real-World Cost Telemetry Study](./deepseek/real_world_cost_study.md)** | 🛠️ **[Explore DeepSeek Integration Harness](./deepseek/README.md)** | 📊 **[Master Cost Rankings](./COST_RANKING.md)**

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

* 🦙 **[Local Llama 3.3 70B Architectural Review](./LOCAL_LLAMA70B_REVIEW.md)** — Project review generated 100% offline by the local Llama 3.3 70B model.
* 🤖 **[Uncapped Agent Architecture](./UNCAPPED_AGENT_ARCHITECTURE.md)** — Guide on building 24/7 high-throughput autonomous agent pipelines without weekly token caps or seat lockouts.
* 🟢 **[NVIDIA 128GB Hardware Guide](./NVIDIA_128GB_HARDWARE_GUIDE.md)** — Hardware guide & benchmark specs for running 70B+ models locally on NVIDIA Jetson AGX Orin 128GB & GH200 workstations.
* ⚡ **[CachyOS Local AI Setup](./CACHYOS_LOCAL_AI_SETUP.md)** — Hardware setup, systemd optimizations, and vLLM / llama.cpp benchmarks.
* 📈 **[DeepSeek Real-World Cost Telemetry](./deepseek/real_world_cost_study.md)** — Empirical case study analyzing 626M tokens processed for **$3.25 USD** with a 99.49% prompt cache hit ratio.
* 💰 **[Prompt Caching Economics & 12-Provider Pricing](./PROMPT_CACHING_LLM_PRICING_2026.md)** — DeepSeek, ZAI, Groq, Fireworks AI, Qwen, Moonshot, Xiaomi & 100M token prompt cache analysis.
* 📊 **[Master Cost Ranking](./COST_RANKING.md)** — Comprehensive cost ranking across all 10 tracked AI providers.
* 🏆 **[Global AI Leaderboard](./TOP_100_LEADERBOARD.md)** — Top 100 frontier AI models ranked by capability, reasoning, and coding benchmarks.
* 🛠️ **[IDEs & Desktop AI Tools](./IDE_PLUGINS_DESKTOP.md)** — Complete setup guide for Cursor, Claude Desktop, Codex Desktop, DeepSeek Harness (`dsh`), OpenHands, and Roo Code.
* 💻 **[Local Execution Guide](./LOCAL_EXECUTION.md)** — Guide to running open-weights models (DeepSeek-R1, Qwen 2.5 Coder) locally using Ollama and vLLM.
* 🔑 **[BYOK & API Key Management](./BYOK_API_KEYS.md)** — Security best practices for configuring direct API keys.
* 🌟 **[Top 100 GitHub AI Projects](./TOP_100_GITHUB_PROJECTS.md)** — Curated directory of top open-source AI repositories.

### ⚡ Live Local Inference Engines
<!-- DYNAMIC_LOCAL_ENGINES_START -->
> *Last Status Check: `2026-10-07 02:58 UTC`*

| Inference Service / Local Server | Endpoint URL | Status |
| :--- | :--- | :--- |
| **Ollama Service** | `http://localhost:11434` | ⚪ Offline / Standby |
| **Open WebUI** | `http://localhost:8080` | ⚪ Offline / Standby |
| **vLLM Engine** | `http://localhost:8000` | ⚪ Offline / Standby |
| **Native llama-server** | `http://localhost:8081` | ⚪ Offline / Standby |
<!-- DYNAMIC_LOCAL_ENGINES_END -->

### 🔗 Free & Low-Cost AI Model Connectors
<!-- DYNAMIC_AI_CONNECTORS_START -->
> *Updated: `2026-10-07 02:58 UTC`*

| AI Provider & Connector | API Base Endpoint | Free / Low-Cost Models | Pricing Tier | Environment Variable |
| :--- | :--- | :--- | :--- | :--- |
| **[OpenRouter Free Tier](https://openrouter.ai/api/v1)** | `https://openrouter.ai/api/v1` | `deepseek/deepseek-r1:free`, `qwen/qwen-2.5-coder-32b:free`, `meta-llama/llama-3.3-70b-instruct:free` | **$0.00 / 1M** (Free tier) | `OPENROUTER_API_KEY` |
| **[Zhipu GLM](https://open.bigmodel.cn/api/paas/v4)** | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash` | **$0.00 / 1M** (100% Free API) | `ZHIPU_API_KEY` |
| **[Google Gemini Free Tier](https://generativelanguage.googleapis.com/v1beta/openai/)** | `https://generativelanguage.googleapis.com/v1beta/openai/` | `gemini-2.0-flash-exp`, `gemini-1.5-flash` | **$0.00** (15 Req/Min free in AI Studio) | `GEMINI_API_KEY` |
| **[Groq Cloud](https://api.groq.com/openai/v1)** | `https://api.groq.com/openai/v1` | `llama-3.3-70b-versatile`, `llama-3.1-8b-instant` | Free Developer Tier (Ultra-Fast LPUs) | `GROQ_API_KEY` |
| **[DeepSeek API Direct](https://api.deepseek.com/v1)** | `https://api.deepseek.com/v1` | `deepseek-chat` (V3), `deepseek-reasoner` (R1) | **$0.003 / 1M** (Cache Hit) / **$0.14** (Miss) | `DEEPSEEK_API_KEY` |
| **[Chutes AI](https://chutes.ai/v1)** | `https://chutes.ai/v1` | `deepseek-ai/DeepSeek-R1`, `Qwen/Qwen2.5-Coder-32B` | Serverless GPU Pay-Per-Token | `CHUTES_API_KEY` |
<!-- DYNAMIC_AI_CONNECTORS_END -->

### 🌟 Trending Open-Source AI Integrations
<!-- DYNAMIC_GITHUB_INTEGRATIONS_START -->
> *Telemetry Sync: `2026-10-07 02:58 UTC`*

| Open-Source AI Project | GitHub Stars | Description & Purpose |
| :--- | :--- | :--- |
| **[deepseek-ai/DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1)** | ⭐ Tracked | deepseek-ai/DeepSeek-R1 repository |
| **[deepseek-ai/DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3)** | ⭐ Tracked | deepseek-ai/DeepSeek-V3 repository |
| **[Qwen3-Coder](https://github.com/QwenLM/Qwen3-Coder)** (`QwenLM/Qwen2.5-Coder`) | **⭐ 16,837** | Qwen3-Coder is the code version of Qwen3, the large language model series dev... |
| **[vllm](https://github.com/vllm-project/vllm)** (`vllm-project/vllm`) | **⭐ 93,295** | A high-throughput and memory-efficient inference and serving engine for LLMs |
| **[llama.cpp](https://github.com/ggml-org/llama.cpp)** (`ggml-org/llama.cpp`) | **⭐ 130,523** | LLM inference in C/C++ |
| **[ollama](https://github.com/ollama/ollama)** (`ollama/ollama`) | **⭐ 182,410** | Get up and running with Kimi, GLM, MiniMax, DeepSeek, gpt-oss, Qwen, Gemma an... |
| **[open-webui](https://github.com/open-webui/open-webui)** (`open-webui/open-webui`) | **⭐ 154,107** | User-friendly AI Interface (Supports Ollama, OpenAI API, ...) |
| **[browser-use](https://github.com/browser-use/browser-use)** (`browser-use/browser-use`) | **⭐ 117,298** | Agents that use the browser. |
| **[Roo-Code](https://github.com/RooCodeInc/Roo-Code)** (`RooVetGit/Roo-Cline`) | **⭐ 24,283** | Roo Code gives you a whole dev team of AI agents in your code editor. |
| **[crewAI](https://github.com/crewAIInc/crewAI)** (`crewAIInc/crewAI`) | **⭐ 59,401** | Framework for orchestrating role-playing, autonomous AI agents. By fostering ... |
<!-- DYNAMIC_GITHUB_INTEGRATIONS_END -->

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
