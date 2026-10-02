# [DeepSeek AI](https://www.deepseek.com) Knowledge Base & Integration Harness

This directory houses technical documentation, evaluation benchmark harnesses, cost analysis, integration scripts, and runner configurations for [DeepSeek](https://www.deepseek.com) models.

---

## 🚀 Tracked DeepSeek Models

| Model | Type | Context Window | Key Capability | Official GitHub Repository |
| :--- | :--- | :---: | :--- | :--- |
| **[DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3)** | Open-Weights MoE (671B / 37B Active) | 128K | Frontier general intelligence & code generation | [deepseek-ai/DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3) |
| **[DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1)** | Open-Weights Reasoning MoE | 128K | Large-scale RL reasoning (rivals OpenAI o1) | [deepseek-ai/DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1) |
| **DeepSeek-R1-Distill Models** | Distilled Open-Weights | Up to 128K | Local deployable reasoning models (Qwen 1.5B–32B, Llama 8B–70B) | [deepseek-ai/DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1) |
| **[DeepSeek-Coder-V2](https://github.com/deepseek-ai/DeepSeek-Coder-V2)** | Open-Weights Code MoE (236B / 21B Active) | 128K | Specialized repo-level code understanding & editing | [deepseek-ai/DeepSeek-Coder-V2](https://github.com/deepseek-ai/DeepSeek-Coder-V2) |
| **DeepSeek-VL2** | Open-Weights Multimodal MoE | 4K–128K | Advanced vision-language understanding & OCR | [deepseek-ai/DeepSeek-VL2](https://github.com/deepseek-ai/DeepSeek-VL2) |

---

## 📚 Documentation & Guides

1. 📊 **[Evaluation & Integration Harness](./eval_harness.md)** — Comprehensive benchmark harness (AIME 2024, MATH-500, SWE-bench Verified, LiveCodeBench), EleutherAI `lm-evaluation-harness` runner configuration, Python/TypeScript SDK integration, and local vLLM/Ollama execution harness.
2. 💰 **[Cost Analysis & Token Pricing](./cost_analysis.md)** — Per-million token input/output costs, 90% prompt caching savings ($0.014 / 1M hit), and ROI comparison against OpenAI o1 / GPT-4o and Claude 3.5 Sonnet.
