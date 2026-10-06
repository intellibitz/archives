# Prompt Caching Token Economics & Provider Comparison (2026)

> **Security & Privacy Guarantee**: This archive entry contains public pricing models, architectural comparison benchmarks, and token math analysis. No private API keys, secrets, or internal credentials are contained herein.

---

## 🚀 Executive Summary & Realized Token Math

Prompt caching fundamentally alters the cost structure of long-context LLM applications, agentic loops, and repository-wide code analysis. By reusing KV-caches for repeated system prompts or context windows, providers offer up to **90% discounts** on prompt tokens.

### The "100 Million Token" Realized Volume Question
When purchasing a budget equivalent to **100 Million standard (uncached) tokens**, prompt caching expands your effective token volume as follows:

| Provider & Model | Uncached Rate / 1M | Cached Rate / 1M | Cache Multiplier | **Realized Tokens for 100M Uncached Budget** |
| :--- | :---: | :---: | :---: | :---: |
| **DeepSeek-V3** (`deepseek-chat`) | $0.14 | **$0.014** | **10.0x** | **1,000,000,000 (1.0 Billion)** |
| **ZAI** (Zhipu GLM-4-Air / Plus) | $0.138 | **$0.014** | **10.0x** | **1,000,000,000 (1.0 Billion)** |
| **Fireworks AI** (DeepSeek 671B Hosted) | $0.90 | **$0.160** | **5.6x** | **562,500,000 (562.5 Million)** |
| **Fireworks AI** (Llama 3.3 70B) | $0.20 | **$0.050** | **4.0x** | **400,000,000 (400 Million)** |
| **DeepSeek-R1** (`deepseek-reasoner`) | $0.55 | **$0.140** | **3.9x** | **392,800,000 (392.8 Million)** |
| **Groq** (DeepSeek-R1 Distill 70B / Llama 70B) | $0.59–$0.75 | **$0.295–$0.375** | **2.0x** | **200,000,000 (200 Million)** |

---

## 📊 Master 12-Provider Pricing Comparison Table

*(All rates normalized to USD per 1 Million tokens; RMB conversion rate ~¥7.25 RMB = $1.00 USD)*

| Provider | Model | Uncached Input / 1M | **Cached Input / 1M** | Output / 1M | Discount % | Data Residency |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **DeepSeek Direct** | **`deepseek-chat` (V3)**<br>**`deepseek-reasoner` (R1)** | **$0.14**<br>**$0.55** | **$0.014**<br>**$0.140** | **$0.28**<br>**$2.19** | **90% off**<br>**75% off** | China Direct |
| **ZAI (Zhipu GLM)** | GLM-4-Flash<br>GLM-4-Air<br>GLM-4-Plus | $0.008<br>$0.138<br>$6.90 | **$0.0014**<br>**$0.014**<br>**$0.69** | $0.014<br>$0.138<br>$6.90 | **85% off**<br>**90% off**<br>**90% off** | China Direct |
| **Qwen (DashScope)** | Qwen-Turbo<br>Qwen-Plus<br>Qwen-Max | $0.041<br>$0.110<br>$2.76 | **$0.007**<br>**$0.014**<br>**$0.345** | $0.041<br>$0.280<br>$8.28 | **83% off**<br>**87.5% off**<br>**87.5% off** | Global / China |
| **MiniMax** | MiniMax-Text-01 | $0.138 | **$0.021** | $0.276 | **85% off** | Global / China |
| **Xiaomi (MiLM)** | MiLM-13B<br>MiLM-70B | $0.069<br>$0.345 | **$0.014**<br>**$0.048** | $0.138<br>$0.690 | **80% off**<br>**86% off** | Global / China |
| **Moonshot / Kimi** | Moonshot-v1-32k<br>Kimi K1.5 | $1.65<br>$1.65 | **$0.210**<br>**$0.210** | $1.65<br>$1.65 | **87.5% off**<br>**87.5% off** | Global / China |
| **Fireworks AI** | DeepSeek 671B Hosted<br>Llama 3.3 70B | $0.90<br>$0.20 | **$0.160**<br>**$0.050** | $0.90<br>$0.20 | **82% off**<br>**75% off** | **US / EU** |
| **Groq** | DeepSeek-R1 Distill 70B<br>Llama 3.3 70B | $0.75<br>$0.59 | **$0.375**<br>**$0.295** | $0.99<br>$0.79 | **50% off**<br>**50% off** | **US / EU** |
| **Mistral AI** | Codestral<br>Mistral Large 2 | $0.30<br>$2.00 | **$0.075**<br>**$0.500** | $0.90<br>$6.00 | **75% off**<br>**75% off** | **EU / US** |
| **Together AI** | Llama 3.3 70B | $0.88 | **$0.440** | $0.88 | **50% off** | **US / EU** |
| **xAI** | Grok-2 | $2.00 | **$0.500** | $10.00 | **75% off** | **US** |
| **NVIDIA NIM** | Llama 3.3 70B | $0.35 | **$0.090** | $0.40 | **75% off** | Hybrid / Global |

---

## 🔬 Head-to-Head Provider Tradeoff Analysis

### 1. DeepSeek Direct vs. Groq
* **Cost Factor:** DeepSeek-V3 cached input ($0.014/1M) is **26.8x cheaper** than Groq ($0.375/1M).
* **Speed Factor:** Groq LPUs deliver **300–500+ tokens/sec** with sub-100ms TTFT, whereas DeepSeek Direct delivers ~30–60 tokens/sec.
* **Verdict:** Use **Groq** for real-time interactive UX (voice, live autocomplete) and **DeepSeek Direct** for high-volume background/agent processing.

### 2. DeepSeek Direct vs. Fireworks AI
* **Cost Factor:** Fireworks AI hosts the full 671B DeepSeek-V3/R1 model at $0.16 cached input / $0.90 output (~3.2x–11x markup over DeepSeek Direct).
* **Residency Factor:** Fireworks AI hosts on US infrastructure with US data center compliance (SOC 2 / HIPAA ready).
* **Verdict:** Use **Fireworks AI** when US/EU data compliance or guaranteed Western SLA stability is strictly required.

### 3. DeepSeek Direct vs. ZAI (Zhipu GLM) & Chinese Ecosystem
* **Cost Parity:** ZAI (GLM-4-Air), Qwen-Plus, Xiaomi, and DeepSeek all share the **$0.014/1M cached input** price floor (~¥0.10 RMB).
* **Intelligence Advantage:** DeepSeek-V3 is a **671B parameter Mixture-of-Experts** frontier model, giving it significantly higher reasoning and coding capabilities than mid-tier 13B–32B alternatives at the exact same price point.

---

## 🎯 Architectural Recommendation

```
                               ┌──────────────────────────────────────────┐
                               │       What is your primary constraint?   │
                               └────────────────────┬─────────────────────┘
                                                    │
             ┌──────────────────────────────────────┼──────────────────────────────────────┐
             ▼                                      ▼                                      ▼
  【 Lowest Cost & Intelligence 】        【 Sub-Second Real-Time Latency 】        【 Western Data Compliance 】
             │                                      │                                      │
             ▼                                      ▼                                      ▼
   DeepSeek Direct / ZAI                          Groq AI                             Fireworks AI
    ($0.014/1M Cached Input)               (300–500+ tokens/sec)                  (US Data Center Hosting)
```
