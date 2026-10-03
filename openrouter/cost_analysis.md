# [OpenRouter](https://openrouter.ai) Cost Analysis & Token Pricing

## Overview
[OpenRouter](https://openrouter.ai) provides a unified OpenAI-compatible API aggregator supplying access to hundreds of open-weights and proprietary foundation models (DeepSeek-R1, Llama 3.3, Claude 3.5 Sonnet, Qwen 2.5, GPT-4o) with dynamic fallback routing, zero-margin pricing option, and prompt caching.

## Key Feature & Pricing Highlights

| Feature / Model Category | Base API Endpoint | Highlights & Pricing |
| :--- | :--- | :--- |
| **OpenRouter Unified Endpoint** | `https://openrouter.ai/api/v1` | Single API key for 200+ models |
| **Free / Open Models** | `openrouter/auto`, `deepseek/deepseek-r1:free` | $0.00 / 1M tokens (rate-limited) |
| **DeepSeek-R1 (Hosted)** | `deepseek/deepseek-r1` | ~$0.55 / $2.19 per 1M tokens |
| **Llama 3.3 70B Instruct** | `meta-llama/llama-3.3-70b-instruct` | ~$0.12 / $0.30 per 1M tokens |
| **Claude 3.5 Sonnet** | `anthropic/claude-3.5-sonnet` | Passthrough pricing ($3.00 / $15.00) |

## Cost Analysis & Economics
- **Unified BYOK & Fallback:** Eliminates vendor lock-in by providing automated provider failover (e.g. automatically routing to Together, DeepInfra, or Fireworks if primary provider experiences downtime).
- **Prompt Caching Support:** Supports upstream prompt cache discounts across compatible models (DeepSeek, Anthropic).
- **Environment Integration:** Uses standard `OPENAI_BASE_URL="https://openrouter.ai/api/v1"` and `OPENAI_API_KEY` for instant drop-in integration with CLI tools and IDE extensions (Roo Code, Cline, Cursor, Continue).
