# 🦙 [Alibaba Qwen AI](https://github.com/QwenLM/Qwen) & Qwen 2.5 Knowledge Base

This directory houses technical documentation, evaluation benchmarks, local deployment guides, and integration harnesses for Alibaba's open-weights **Qwen** foundation and coding models.

---

## 🚀 Tracked Qwen Models

| Model | Parameters | Context Window | Key Capability | Top Benchmark Rank |
| :--- | :---: | :---: | :--- | :--- |
| **[Qwen2.5-Coder-32B-Instruct](https://github.com/QwenLM/Qwen2.5-Coder)** | 32.5B | 128K | **#1 Open-Source Coding Model in the World.** Matches GPT-4o on code synthesis. | **SWE-bench Verified Leader** |
| **Qwen2.5-Coder-14B-Instruct** | 14.7B | 128K | Balanced power/memory model. Runs at full FP16 on 24GB GPUs (RTX 3090/4090). | High-efficiency local pair programmer |
| **Qwen2.5-Coder-7B-Instruct** | 7.6B | 128K | Lightweight, ultra-fast coding model. Runs comfortably on 8GB VRAM or Apple Silicon laptops. | Best 7B coding model on EvalPlus & LiveCodeBench |
| **Qwen2.5-72B-Instruct** | 72.7B | 128K | Flagship open-weights general intelligence model. Strong multilingual, math, and agentic reasoning. | Matches Claude 3.5 Sonnet on MMLU-Pro |

---

## 💻 Local Deployment Options

Qwen models are 100% open-weights and can be run completely offline with zero API costs:

* **Ollama:** `ollama run qwen2.5-coder:32b` or `ollama run qwen2.5-coder:7b`
* **vLLM (High-Throughput Production):** OpenAI-compatible API server with PagedAttention.
* **LM Studio & Jan.ai:** Graphical desktop apps with one-click Hugging Face model downloads.

---

## 📚 Documentation & Guides

* 💻 **[Qwen 2.5 Coder Deployment & Benchmarks](./coder_guide.md)** — Hardware sizing, quantization guides (GGUF / AWQ), SWE-bench results, and VS Code/Aider setup.
* 💰 **[Token Pricing & Cloud Hosting](./cost_analysis.md)** — Comparative economics of running Qwen on self-hosted GPUs vs serverless cloud providers (Chutes, Fireworks, DeepInfra).
