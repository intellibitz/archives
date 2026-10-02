# Real-World DeepSeek API Cost & Caching Telemetry Study

This empirical case study analyzes real-world API telemetry data over a 2-day high-throughput agentic workflow execution (~626 Million tokens across 1,672 requests) to demonstrate the cost benefits of [DeepSeek's](https://www.deepseek.com) prompt caching architecture compared to alternative proprietary and open-weights LLM providers.

---

## 📊 1. Empirical Telemetry Data Summary

The following anonymized telemetry dataset reflects active production usage for multi-turn agentic coding sessions:

* **Evaluation Period:** 2 Days (48 Hours)
* **Total API Requests:** **1,672 requests**
* **Total Token Volume Processed:** **626,329,236 tokens** (~626.33 Million Tokens)
* **Actual Total Cost Incurred:** **$3.2479 USD** (~**$3.25 USD**)
* **Prompt Cache Hit Ratio:** **99.49%**

### Token Volume & Cost Breakdown

| Metric / Category | Day 1 | Day 2 | Total Token Volume | Unit Rate (per 1M Tokens) | Subtotal Cost |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Input Tokens (Cache Miss)** | 1,644,930 | 1,564,690 | **3,209,620** (~3.21 M) | $0.15 / 1M ($0.00000015) | **$0.481** |
| **Input Tokens (Cache Hit)** | 375,830,656 | 247,288,960 | **623,119,616** (~623.12 M) | **$0.003 / 1M** ($0.000000003) | **$1.869** |
| **Output Generation Tokens** | 990,075 | 505,084 | **1,495,159** (~1.50 M) | $0.60 / 1M ($0.00000060) | **$0.897** |
| **Total Requests** | 1,005 | 667 | **1,672** | — | — |
| **Daily Expenditure** | **$1.968** | **$1.280** | — | — | **$3.248 USD** |

---

## 💡 2. Why DeepSeek Prompt Caching Disrupts LLM Economics

In multi-turn agentic workflows (e.g., repository auditing, continuous refactoring, or iterative chat), the system prompt, codebase context, and conversation history are repeatedly re-sent with every message.

1. **99.49% Context Reuse:** Out of 626.33 Million total input tokens sent, 623.12 Million tokens were served directly from DeepSeek's prompt cache.
2. **The $0.003 / 1M Benchmark:** Serving cached prompt tokens at $0.003 per 1 Million tokens ($3 per 1 Billion tokens) reduced input cost by **98%** compared to un-cached input pricing ($0.15 / 1M).
3. **Net Financial Savings:** Without prompt caching, processing 626.33 M input tokens at standard rates would have cost **~$93.95 USD**. DeepSeek's prompt caching reduced the total bill to **$3.25 USD**—delivering a net savings of **$90.70 USD** on a single 2-day session.

---

## 💰 3. Comparative Cost Analysis Across Providers

Applying this exact **626.33M Input (99.49% Cached) / 1.50M Output** workload across major commercial LLM API providers highlights the massive economic disparity:

| Provider & Model | Input Rate (Miss / Hit per 1M) | Output Rate (per 1M) | Projected Total Cost for Workload | Cost Ratio vs DeepSeek |
| :--- | :---: | :---: | :---: | :---: |
| **[Zhipu GLM-4-Flash](https://open.bigmodel.cn)** | **$0.00 / $0.00** | **$0.00** | **$0.00** *(Free Tier)* | **0.0x** |
| **Local Ollama / vLLM** *(Self-Hosted)* | **$0.00 / $0.00** | **$0.00** | **$0.00** *(Hardware Only)* | **0.0x** |
| **[DeepSeek-Flash](https://www.deepseek.com)** *(Actual)* | **$0.15 / $0.003** | **$0.60** | **$3.25** | **1.0x** *(Baseline)* |
| **[DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3)** | $0.14 / $0.014 | $0.28 | **$9.59** | **2.95x** |
| **[Google Gemini 2.0 Flash](https://ai.google.dev)** | $0.10 / $0.025 | $0.40 | **$16.50** | **5.08x** |
| **[OpenAI GPT-4o-mini](https://openai.com)** | $0.15 / $0.075 | $0.60 | **$48.11** | **14.80x** |
| **[Claude 3.5 Haiku](https://www.anthropic.com)** | $0.80 / $0.08 | $4.00 | **$56.41** | **17.36x** |
| **[OpenAI GPT-4o](https://openai.com)** | $2.50 / $1.25 | $10.00 | **$798.11** | **245.57x** |

---

## 🔑 Key Takeaways

1. **Agentic Workflows Demand High Cache Hits:** When selecting an LLM provider for coding agents or IDE extensions, the **cached input token price** is the single most critical factor determining operating cost.
2. **OpenAI GPT-4o-mini vs DeepSeek:** While GPT-4o-mini offers similar base input rates ($0.15/1M), its cached input rate ($0.075/1M) is **25x higher** than DeepSeek's ($0.003/1M), making GPT-4o-mini nearly 15 times more expensive for high-context agentic workloads.
3. **Free Alternatives:** For budget-constrained applications, **Zhipu GLM-4-Flash** provides a free cloud API, and self-hosting **Qwen2.5-72B** or **DeepSeek-R1-32B** on local GPUs via **Ollama / vLLM** eliminates token fees entirely.
