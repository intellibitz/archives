# Real-World DeepSeek API Cost & Caching Telemetry Study

This empirical case study analyzes real-world API telemetry data across a 5-day high-throughput agentic workflow execution (**1.15 Billion tokens across 3,001 requests**) comparing [DeepSeek](https://www.deepseek.com) **Flash** and **Pro** model tiers, showcasing the cost benefits of prompt caching architecture compared to alternative high-speed models like **Claude 3.5 Haiku** and **GPT-4o-mini**.

---

## 📊 1. Empirical Telemetry Data Summary

The following production telemetry dataset reflects active usage for multi-turn agentic coding and analysis sessions:

* **Evaluation Period:** 5 Days (2026-10-01 to 2026-10-05)
* **Total API Requests:** **3,001 requests**
* **Total Token Volume Processed:** **1,151,342,431 tokens** (~1.15 Billion Tokens)
* **Actual Total Cost Incurred:** **$10.37 USD** (Paid Wallet) + **6.10 CNY** (~$0.86 USD Granted Wallet)
* **Overall Prompt Cache Hit Ratio:** **99.51%**

---

## 💰 2. Model Tier Breakdown: `deepseek-flash` vs `deepseek-v4-pro`

| Model Tier | Requests | Input Cache Hit Tokens | Input Cache Miss Tokens | Output Tokens | Total Tokens | Billed Cost | Cache Hit Ratio |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| ⚡ **`deepseek-flash`** | 2,509 | 936,256,000 (~936.3M) | 4,452,057 (~4.45M) | 2,435,621 (~2.44M) | **943,143,734** (~0.94B) | **$4.02 USD** + **6.10 CNY** | **99.53%** |
| 🚀 **`deepseek-v4-pro`** | 492 | 206,477,696 (~206.5M) | 1,214,343 (~1.21M) | 506,658 (~0.51M) | **208,198,697** (~0.21B) | **$6.35 USD** | **99.42%** |
| **Combined Total** | **3,001** | **1,142,733,696** (~1.14B) | **5,666,400** (~5.67M) | **2,942,279** (~2.94M) | **1,151,342,431** (~1.15B) | **$10.37 USD** + **6.10 CNY** | **99.51%** |

---

## 🏷️ 3. Unit Pricing Comparison

| Token Category | `deepseek-flash` Unit Rate | `deepseek-v4-pro` Unit Rate | Price Ratio (Pro vs Flash) |
| :--- | :---: | :---: | :---: |
| **Input Cache Hit** | **$0.003 / 1M tokens** ($0.000000003) | **$0.022 / 1M tokens** ($0.000000022) | **7.3x** |
| **Input Cache Miss** | **$0.150 / 1M tokens** ($0.000000150) | **$0.660 / 1M tokens** ($0.000000660) | **4.4x** |
| **Output Generation** | **$0.600 / 1M tokens** ($0.000000600) | **$1.980 / 1M tokens** ($0.000001980) | **3.3x** |

---

## 📅 4. Daily Telemetry Breakdown

### ⚡ `deepseek-flash`
* **2026-10-01:** 1,005 Requests | 375.8M Cache Hits ($1.13) | 1.6M Misses ($0.25) | 990K Output ($0.59) ➔ **$1.97 USD**
* **2026-10-02:** 844 Requests | 308.2M Cache Hits ($0.92) | 1.6M Misses ($0.24) | 797K Output ($0.48) ➔ **$1.65 USD**
* **2026-10-03:** 653 Requests | 249.0M Cache Hits ($3.78) | 1.2M Misses ($0.98) | 645K Output ($1.73) ➔ **$0.39 USD (Paid)** + **6.10 CNY (Granted)**
* **2026-10-04:** 7 Requests | 3.2M Cache Hits ($0.01) | 18K Misses ($0.00) | 4K Output ($0.00) ➔ **$0.01 USD**

### 🚀 `deepseek-v4-pro`
* **2026-10-04:** 492 Requests | 206.5M Cache Hits ($4.54) | 1.2M Misses ($0.80) | 507K Output ($1.00) ➔ **$6.35 USD**

---

## 💡 5. Why DeepSeek Prompt Caching Disrupts LLM Economics

1. **99.51% Context Reuse:** Out of 1.14 Billion total input tokens sent, 1.14 Billion tokens were served directly from DeepSeek's prompt cache.
2. **The $0.003 / 1M Benchmark:** Serving cached prompt tokens at $0.003 per 1 Million tokens ($3 per 1 Billion tokens) reduced input cost by **98%** compared to un-cached input pricing ($0.15 / 1M).
3. **Net Financial Savings:** Without prompt caching, processing 1.15 Billion input tokens at standard un-cached rates would have cost **~$172.50 USD**. DeepSeek's prompt caching reduced the total bill to **$10.37 USD**—delivering a net savings of **$162.13 USD** on a 5-day agentic workload.

---

## 🔑 Key Comparative Takeaways

1. **Flash vs Pro Selection:** Flash is ultra-economical for high-volume background agent loops (nearly **1 Billion tokens for ~$4.88 USD**), while Pro tier provides top-tier frontier reasoning at **3.3x–7.3x** the unit price while maintaining a **99.42% cache hit ratio**.
2. **DeepSeek vs OpenAI & Anthropic:** For multi-turn agent sessions with high prompt reuse, DeepSeek's $0.003–$0.022/1M cached input pricing makes agentic workflows **15x–50x cheaper** than GPT-4o-mini and Claude 3.5 Haiku.
