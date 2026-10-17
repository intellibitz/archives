# 🌐 [Google AI](https://ai.google.dev) & Gemini Foundation Models

This directory provides technical documentation, evaluation harnesses, pricing telemetry, and integration recipes for Google's **Gemini** foundation models, specifically focusing on high-speed multimodal reasoning and massive **1 Million to 2 Million token context windows**.

---

## 🚀 Tracked Google AI Models

| Model | Context Window | Input Cost (per 1M) | Output Cost (per 1M) | Key Architecture & Strength |
| :--- | :---: | :---: | :---: | :--- |
| **[Gemini 2.0 Flash](https://ai.google.dev)** | **1,048,576 tokens (1M)** | **$0.10** ($0.025 cached) | **$0.40** | Breakthrough high-speed multimodal reasoning, low-latency function calling, free tier (15 RPM). |
| **[Gemini 1.5 Pro](https://ai.google.dev)** | **2,097,152 tokens (2M)** | **$1.25** ($0.3125 cached) | **$5.00** | Massive context window for ingesting entire software repositories, long video streams, and audio archives. |
| **Gemini 2.0 Flash Thinking** | **1,048,576 tokens (1M)** | Experimental / Free | Experimental | Native chain-of-thought reasoning for complex mathematical, algorithmic, and debugging tasks. |

---

## 💡 Architectural Role in the IntelliBitz Platform

In the multi-agent orchestrator architecture, Google Gemini serves as the **Autonomous Repo Librarian**:

1. **Whole-Repository Ingestion:** Ingests tens of thousands of lines of code across hundreds of files in a single turn, eliminating the vector database chunking problem (RAG retrieval failures).
2. **Multimodal Diagnostics:** Natively inspects UI screenshots, wireframes, PDF architectural specifications, and CI/CD video traces.
3. **High-Speed Tool Calling:** Gemini 2.0 Flash executes sub-second structured function calling, ideal for driving fast Model Context Protocol (MCP) tool loops.

---

## 📚 Documentation & Integration Guides

* 📖 **[Codebase Ingestion & 2M Context Guide](./codebase_ingestion_guide.md)** — Architectural guide on using Gemini 1.5 Pro and 2.0 Flash to analyze entire codebases in a single prompt.
* 💰 **[Token Pricing & Cost Analysis](./cost_analysis.md)** — Comprehensive breakdown of per-million token costs, prompt caching economics, and long-context pricing tiers.
