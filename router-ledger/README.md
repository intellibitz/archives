# Router Ledger: AI Model Inference Pricing & Fallback Architecture

Imported telemetry, benchmark metrics, fallback logic, and cost formulas from the **Router Ledger** (`router-ledger.pplx.app`).

---

## 📊 Overview & Provider Scope

The **Router Ledger** tracks per-million-token rates, prompt caching discounts, billing fees, fallback chain reliability, and measured output performance across major AI model routers and inference platforms:

* **Tracked Platforms & Routers**: OpenRouter, Chutes, Together AI, Groq, Cloudflare (Workers AI & AI Gateway), and DeepSeek API.
* **Tracked Model Families**: Llama (Llama 3.1 8B, Llama 3.3 70B, Llama 4), Mistral, Qwen (Qwen 2.5 Coder/Instruct), gpt-oss, and DeepSeek (V3 & R1).

---

## 🧮 Monthly Cost Calculation Formula

$$\text{Monthly Cost} = \left[\text{Input} \times (1 - \text{Cache Share}) \times \text{Input Rate}\right] + \left[\text{Input} \times \text{Cache Share} \times \text{Cached Rate}\right] + \left[\text{Output} \times \text{Output Rate}\right] + \text{Fees}$$

### Formula Parameters
* **Cache Share**: Proportion of input prompt tokens served via KV prompt cache hits.
* **Cached Rate**: Reduced per-million input token rate applied when prompt caching hits.
* **Top-Up / Gateway Fees**:
  * **OpenRouter**: Optional **5.5%** card top-up fee when using credit card billing.
  * **Cloudflare AI Gateway**: 0% for self-provided API keys (BYOK); optional 5% fee on Unified Billing.
* **DeepSeek Off-Peak Discount**: 50% discount on DeepSeek API input/output rates during off-peak hours (all hours *except* 01:00–04:00 and 06:00–10:00 UTC on weekdays).

---

## ⚡ Fallback & Dynamic Routing Architecture

### 1. OpenRouter (Winner — Automated Price-Floor Routing)
* **Routing Strategy**: Fully automatic, price-ordered fallback across independent hosting providers running the same open-weights model.
* **Configuration Header**: Setting `provider.sort: "price"` (or using the `:floor` model suffix) routes requests to the cheapest operational endpoint first.
* **Fallback Chain Example (Llama 3.3 70B)**:
  ```text
  DeepInfra ($0.10/M) ──► Novita ($0.135/M) ──► AkashML ($0.20/M) ──► Parasail ($0.22/M)
  ```
* **Resilience**: If the cheapest host is rate-limited or degraded, requests automatically failover to the next lowest-cost host without throwing client 5xx errors.

### 2. Cloudflare AI Gateway (Runner-Up — Manual Fallback Chain)
* **Routing Strategy**: Free core gateway layer that manages retries, fallbacks, and rate limits across user-provided API keys.
* **Configuration**: Requires manual configuration of fallback chains and rate limits per provider.

---

## 🚀 Performance Metrics & Benchmarks

* **Latency & Throughput**: Sourced via Artificial Analysis medians (P50, 72-hour window, 10,000-token input context).
  * **Output Speed**: Measured in tokens per second ($\text{tok/s}$).
  * **Time to First Token (TTFT)**: Measured in seconds ($\text{s}$).

---

## 📑 Provider Specific Notes

* **Groq**: Llama 3.1 8B and Llama 3.3 70B are listed as enterprise custom pricing on Groq directly; Groq rates in the ledger reflect its OpenRouter endpoints.
* **Chutes AI**: Offers decentralized serverless GPU endpoints for Llama 3.1, Qwen 2.5, and DeepSeek-R1.
* **DeepSeek Direct API**: 50% discount applies during off-peak weekday hours and weekends.
