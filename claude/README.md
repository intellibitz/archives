# 🧠 [Anthropic Claude](https://www.anthropic.com) Knowledge Base & Agent Ecosystem

This directory houses technical documentation, evaluation benchmarks, architectural guides, and integration recipes for Anthropic's **Claude** family of foundation models, with emphasis on **Claude 3.5 Sonnet**, **Claude 3.7 Sonnet (Hybrid Reasoning)**, and **Model Context Protocol (MCP)** execution.

---

## 🚀 Tracked Anthropic Models

| Model | Context Window | Input Cost (per 1M) | Output Cost (per 1M) | Core Strength & Role |
| :--- | :---: | :---: | :---: | :--- |
| **[Claude 3.7 Sonnet](https://www.anthropic.com)** | 200,000 | **$3.00** ($0.30 cached) | **$15.00** | **Hybrid Reasoning Frontier.** Dynamically toggles between instant generation and extended chain-of-thought reflection. SWE-bench Verified champion. |
| **[Claude 3.5 Sonnet](https://www.anthropic.com)** | 200,000 | **$3.00** ($0.30 cached) | **$15.00** | Gold standard for software engineering, tool calling, and Computer Use DOM automation. |
| **[Claude 3.5 Haiku](https://www.anthropic.com)** | 200,000 | **$0.80** ($0.08 cached) | **$4.00** | Fast, economical coding model for sub-agent loops, git commit generation, and syntax linting. |
| **Claude 3 Opus** | 200,000 | $15.00 | $75.00 | Deep humanistic analysis, long-form prose, and complex enterprise legal synthesis. |

---

## 🔌 Anthropic & The Model Context Protocol (MCP)

Anthropic created the open standard **Model Context Protocol (MCP)** to replace fragmented proprietary plugins with a universal JSON-RPC protocol over stdio and SSE.

* **Claude Desktop:** Native MCP host connecting desktop Claude to local databases, git repositories, and filesystems.
* **Claude Code CLI:** Autonomous terminal agent orchestrating MCP servers directly within your repository.

---

## 📚 Documentation & Integration Guides

* 🧠 **[Claude 3.7 Sonnet & Hybrid Reasoning Architecture](./hybrid_reasoning_guide.md)** — Guide on extended thinking budgets, token economics, and SWE-bench Verified coding patterns.
* 💰 **[Token Pricing & Cache Analysis](./cost_analysis.md)** — Detailed analysis of Anthropic prompt caching (90% discount on cache hits) and ROI comparisons against OpenAI and DeepSeek.
* 💻 **[Claude Code CLI Hub](../claude-code/README.md)** — Operational guide for the native Claude Code terminal coding assistant.
