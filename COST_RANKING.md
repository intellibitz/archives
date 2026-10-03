# AI Provider & Model Cost Ranking (Lowest to Highest)

This document provides a comprehensive cost ranking of models across all 10 tracked AI providers, ordered from **lowest cost** to **highest cost** per 1 million tokens (Input / Output). All providers and models include direct clickable links to their official websites or GitHub repositories.

---

## 🏆 Master Cost Ranking Table

| Rank | Provider / Model Tier | Input Cost (per 1M tokens) | Output Cost (per 1M tokens) | Value & Efficiency Category |
| :---: | :--- | :---: | :---: | :--- |
| **1** | **[Zhipu GLM](https://open.bigmodel.cn)** (GLM-4-Flash) | $0.00 | $0.00 | Free / Prototyping |
| **2** | **[DeepSeek](https://www.deepseek.com)** ([DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3) - Cache Hit) | **$0.003 - $0.014** | $0.28 - $0.60 | Ultra-Low (Cached Agentic) |
| **3** | **[Ministral / Mistral AI](https://mistral.ai)** (Ministral 8B) | $0.10 | $0.10 | Edge Micro-Model |
| **4** | **[Qwen](https://github.com/QwenLM/Qwen)** ([Qwen2.5-7B-Instruct](https://github.com/QwenLM/Qwen2.5)) | $0.10 | $0.20 | Lightweight Multilingual |
| **5** | **[Google AI](https://ai.google.dev)** ([Gemini 2.0 Flash](https://ai.google.dev)) | $0.10 | $0.40 | High-Speed Multimodal |
| **6** | **[Codex Luna](https://openai.com)** / **[OpenAI](https://openai.com)** ([GPT-4o-mini](https://openai.com)) | $0.15 | $0.60 | Standard Micro / Agent Model |
| **7** | **Xiaomi MiMo** (MiMo-Instruct) | $0.20 | $0.50 | Edge-Cloud Hybrid |
| **8** | **Inclusion AI** (Inclusion-Core) | $0.25 | $0.75 | Accessible Multi-Region |
| **9** | **[Qwen](https://github.com/QwenLM/Qwen)** ([Qwen2.5-72B-Instruct](https://github.com/QwenLM/Qwen2.5)) | $0.35 | $0.70 | Open-Weights High Performance |
| **10** | **[DeepSeek](https://www.deepseek.com)** ([DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3) - Standard) | $0.14 | $0.28 | Frontier General Intelligence |
| **11** | **Agnes AI** (Agnes-Agent-Base) | $0.50 | $1.50 | Domain-Specific Agent |
| **12** | **[DeepSeek](https://www.deepseek.com)** ([DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1)) | $0.55 | $2.19 | Frontier Reasoning Model |
| **13** | **[Claude](https://www.anthropic.com)** ([Claude 3.5 Haiku](https://www.anthropic.com)) | $0.80 | $4.00 | Fast Enterprise Model (200K Context) |
| **14** | **[Zhipu GLM](https://open.bigmodel.cn)** (GLM-4 Standard) | $1.00 | $2.00 | Bilingual Enterprise |
| **15** | **[OpenAI](https://openai.com)** ([o3-mini](https://openai.com)) | $1.10 | $4.40 | Optimized Reasoning Model |
| **16** | **[Google AI](https://ai.google.dev)** ([Gemini 1.5 Pro](https://ai.google.dev)) | $1.25 | $5.00 | Massive Long-Context (2M) |
| **17** | **[Qwen](https://github.com/QwenLM/Qwen)** (Qwen-Max) | $1.60 | $6.40 | Flagship Multilingual |
| **18** | **[Mistral AI](https://mistral.ai)** ([Mistral Large 2](https://mistral.ai)) | $2.00 | $6.00 | Enterprise European Flagship |
| **19** | **[OpenAI](https://openai.com)** ([GPT-4o](https://openai.com)) | $2.50 | $10.00 | Flagship General Purpose |
| **20** | **[Claude](https://www.anthropic.com)** ([Claude 3.5 Sonnet](https://www.anthropic.com)) | $3.00 | $15.00 | Premium Coding & Reasoning |
| **21** | **[OpenAI](https://openai.com)** ([o1](https://openai.com)) | $15.00 | $60.00 | Advanced Reasoning Frontier |
| **22** | **[Claude](https://www.anthropic.com)** ([Claude 3 Opus](https://www.anthropic.com)) | $15.00 | $75.00 | Premium Long-Form Writing |

---

## 💡 Key Economic & Comparative Insights

1. **DeepSeek vs Claude 3.5 Haiku & Codex Luna:**
   * **DeepSeek-Flash / V3:** $0.003 - $0.014/1M cached input. Processing a 626M token agentic session costs **$3.25**.
   * **Codex Luna (GPT-4o-mini tier):** $0.075/1M cached input. Processing the same workload costs **$48.11** (14.8x higher).
   * **Claude 3.5 Haiku:** $0.080/1M cached input, $4.00/1M output. Processing the same workload costs **$56.41** (17.4x higher).
2. **The Reasoning Disruption:** [DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1) (`$0.55 / $2.19`) delivers frontier reasoning capabilities at ~1/25th the cost of OpenAI's `o1` (`$15.00 / $60.00`), fundamentally changing high-compute task economics.
3. **Caching Power:** [DeepSeek's](https://www.deepseek.com) prompt caching (`$0.003` per 1M cached input tokens) makes multi-turn agent loops cheaper than almost any other model on the market.
