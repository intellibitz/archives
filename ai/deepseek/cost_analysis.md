# DeepSeek Cost Analysis & Token Pricing

## Overview
DeepSeek models (DeepSeek-V3 and DeepSeek-R1) offer breakthrough performance in reasoning and general tasks at ultra-low pricing, disrupting traditional LLM cost economics.

## Token Pricing (Per 1 Million Tokens)

| Model | Input Cost (Cache Miss) | Input Cost (Cache Hit) | Output Cost | Cost per Token (Input / Output) |
| :--- | :--- | :--- | :--- | :--- |
| **DeepSeek-V3** | $0.14 | $0.014 | $0.28 | $0.00000014 / $0.00000028 |
| **DeepSeek-R1** | $0.55 | $0.14 | $2.19 | $0.00000055 / $0.00000219 |

## Cost Analysis & Economics
- **Context Window:** Up to 64K tokens.
- **Caching Efficiency:** DeepSeek's prompt caching reduces repeat input token costs by 90% ($0.014 / 1M tokens), making agentic loops and multi-turn conversations exceptionally economical.
- **Reasoning ROI:** DeepSeek-R1 provides near-frontier reasoning performance at ~1/10th the cost of proprietary reasoning models.
