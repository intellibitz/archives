# The Uncapped Autonomous Agent Architecture Guide (24/7 Production Workflows)

This guide details how to architect, configure, and execute **high-throughput, 24/7 autonomous AI software engineering workflows** without getting locked out, throttled, or hitting weekly token caps.

---

## 🚫 1. The Seat-Based Subscription Trap

Proprietary seat-based tools (Cursor Pro, Devin, Claude Pro, ChatGPT Plus) sell **fixed-price monthly subscriptions** ($20 – $500+/month). To protect their profit margins from heavy multi-agent loops and broad codebase prompts, they enforce **hard rolling weekly caps, daily token throttles, and message queues**.

Running all-day agentic workflows that process hundreds of millions of tokens will exhaust weekly seat quotas in 1 to 2 days.

### The Solution: Decouple the Agent Client from the API Provider
To build a **100% lock-proof environment**, combine **open-source, local agent clients** (zero software limits) with **uncapped Pay-As-You-Go API providers**.

---

## ⚡ 2. The Production-Grade Uncapped Cloud API Stack

```
                          ┌──────────────────────────────────────────────────┐
                          │ Standalone Open-Source Agent Client              │
                          │ (Aider / DeepSeek Harness / OpenHands / Roo Code)│
                          └────────────────────────┬─────────────────────────┘
                                                   │
            ┌──────────────────────────────────────┼──────────────────────────────────────┐
            ▼                                      ▼                                      ▼
┌─────────────────────────┐            ┌─────────────────────────┐            ┌─────────────────────────┐
│ Primary Workhorse       │            │ Broad Context & Fast    │            │ Universal Failover      │
│ DeepSeek Direct API     │            │ Google Gemini 2.0 Flash │            │ OpenRouter API Router   │
│ • 99.5% Cache Hit Ratio │            │ • 1M+ Token Context     │            │ • 200+ Cloud Models     │
│ • $0.003 / 1M Cached    │            │ • 100+ tokens/sec       │            │ • DeepInfra / Novita    │
│ • Uncapped Throughput   │            │ • $0.10 / 1M Input      │            │ • Auto Failover         │
└─────────────────────────┘            └─────────────────────────┘            └─────────────────────────┘
```

### Provider Matrix

| Provider & Model | Role in Stack | Context Window | Rate & Usage Limits | Cost Structure |
| :--- | :--- | :---: | :--- | :--- |
| **[DeepSeek API](https://platform.deepseek.com)** (`deepseek-chat` / `deepseek-reasoner`) | **Primary Workhorse** | 128K | **Uncapped** (Prepaid) | $0.15 / $0.003 Cached Input, $0.60 Output ($3.25 / 626M tokens) |
| **[Google Gemini API](https://ai.google.dev)** (`gemini-2.0-flash`) | **Broad Context & Vision** | **1M – 2M** | **4,000 RPM** / 4M TPM | $0.10 / $0.025 Cached Input, $0.40 Output (Free tier available) |
| **[OpenRouter API](https://openrouter.ai)** (`openrouter/auto`) | **Failover Router** | Model Dependent | **Uncapped** (Prepaid) | Access to Claude 3.5 Sonnet, DeepSeek V3/R1, Qwen 2.5 Coder 72B |

---

## 🤖 3. Standalone Non-Docker Agent Tools

These agent clients run **natively on Linux** without requiring Docker, container virtualization, or monthly seat accounts:

### 1. **Aider (`aider`) — Standalone Terminal Pair-Programmer**
* **Type:** Native Python CLI (`pip install aider-chat`).
* **Architect / Editor Dual-Agent Mode:** Runs an **Architect Agent** (DeepSeek-R1) for high-level reasoning and an **Editor Agent** (DeepSeek-V3) for multi-file edits.
* **Command:**
  ```bash
  export DEEPSEEK_API_KEY="sk-..."
  aider --model deepseek/deepseek-chat --architect
  ```

### 2. **DeepSeek Harness (`dsh` / `dsh web`) — Browser & Terminal Agent**
* **Type:** Native Node.js CLI & Web UI (`npm install -g @deepseek-ai/dsh`).
* **OpenRouter & Gemini Integration:** Connects seamlessly to OpenRouter or Google Gemini via OpenAI-compatible environment flags:
  ```bash
  # OpenRouter
  OPENAI_BASE_URL="https://openrouter.ai/api/v1" OPENAI_API_KEY="sk-or-v1-..." dsh web

  # Google Gemini API
  OPENAI_BASE_URL="https://generativelanguage.googleapis.com/v1beta/openai/" OPENAI_API_KEY="AIzaSy..." dsh web
  ```

### 3. **OpenHands Non-Docker Web Engine (`openhands-web`)**
* **Type:** Native Python standalone web server (`pipx install openhands-ai`).
* **Workspace Attachment:** Executes directly on local host workspace directories without Docker containers:
  ```bash
  export RUNTIME="local"
  export SANDBOX_TYPE="local"
  export WORKSPACE_BASE="/path/to/your/project"
  openhands-web
  ```

### 4. **Roo Code Desktop & CLI (`roo`)**
* **Type:** VS Code Extension (`rooveterinaryinc.roo-cline`) + Standalone CLI (`roo`).
* **Features:** Autonomous agentic workspace indexing, multi-file editing, custom prompt modes.

---

## 🎯 4. Production Recommendations

1. **Never use fixed-price seat subscriptions for multi-agent loops.** Use BYOK Pay-As-You-Go endpoints.
2. **Combine DeepSeek + Gemini 2.0 Flash:** DeepSeek provides the lowest prompt-caching cost for daily generation ($0.003/1M), while Gemini 2.0 Flash provides 1M+ token context for massive codebase ingestion.
3. **Use OpenRouter as a Failover Endpoint:** Deposit $5 on OpenRouter as a backup if official provider APIs experience transient latency spikes during peak hours.
