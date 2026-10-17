# 💻 Qwen 2.5 Coder: The World's Premier Open-Weights Coding Model

This guide details architecture, empirical benchmarks, hardware requirements, and local deployment recipes for the **Qwen 2.5 Coder** series (32B, 14B, and 7B).

---

## 🏆 1. Benchmark Dominance

Qwen 2.5 Coder 32B rivals closed-source proprietary frontier models across major software engineering benchmarks:

| Benchmark | Qwen 2.5 Coder 32B | GPT-4o | Claude 3.5 Sonnet | DeepSeek-Coder-V2 (236B) |
| :--- | :---: | :---: | :---: | :---: |
| **SWE-bench Verified** | **40.2%** | 38.8% | **49.0%** | 38.8% |
| **LiveCodeBench (2024-11)** | **31.1%** | 30.6% | 34.5% | 27.8% |
| **Aider Coding Benchmark** | **73.7%** | 72.9% | 77.2% | 73.0% |
| **MultiPL-E (Average)** | **88.2%** | 86.4% | 88.0% | 85.5% |

Qwen 2.5 Coder 32B is the **first open-source model under 70B parameters** to match or exceed GPT-4o on real-world issue resolution.

---

## 🖥️ 2. Hardware & VRAM Requirements

| Model Tier | Quantization | Required VRAM / System RAM | Recommended Hardware |
| :--- | :---: | :---: | :--- |
| **Qwen 2.5 Coder 7B** | Q4_K_M | **5.5 GB** | Consumer laptops, Apple M1/M2 (8GB+ RAM), RTX 3060 |
| **Qwen 2.5 Coder 7B** | FP16 | **15.0 GB** | RTX 3090 / 4080 / 4090 |
| **Qwen 2.5 Coder 14B** | Q4_K_M | **9.5 GB** | RTX 3060 (12GB), RTX 4070 (12GB), Mac (16GB RAM) |
| **Qwen 2.5 Coder 32B** | Q4_K_M | **20.5 GB** | **Single RTX 3090 / 4090 (24GB)**, Mac Studio (32GB+) |
| **Qwen 2.5 Coder 32B** | Q8_0 | **34.0 GB** | 2x RTX 3090/4090 (48GB), Mac Studio (64GB RAM) |

---

## 🚀 3. Deploying Locally with Ollama & vLLM

### Fast Local Execution via Ollama
```bash
# Pull and run the 7B model for lightweight tasks
ollama run qwen2.5-coder:7b

# Pull and run the flagship 32B model (requires 24GB VRAM or 32GB RAM)
ollama run qwen2.5-coder:32b
```

### High-Throughput Production Serving via vLLM
Serve an OpenAI-compatible API endpoint locally with PagedAttention:
```bash
pip install vllm

python -m vllm.entrypoints.openai.api_server \
    --model Qwen/Qwen2.5-Coder-32B-Instruct-AWQ \
    --port 8000 \
    --max-model-len 32768 \
    --gpu-memory-utilization 0.95
```

---

## 🔌 4. Integration with Autonomous Agent Clients

### 1. Aider CLI (Terminal Pair Programmer)
```bash
# Direct Ollama connection
export OLLAMA_API_BASE="http://localhost:11434"
aider --model ollama/qwen2.5-coder:32b

# Direct vLLM connection
export OPENAI_API_BASE="http://localhost:8000/v1"
export OPENAI_API_KEY="none"
aider --model openai/Qwen/Qwen2.5-Coder-32B-Instruct-AWQ
```

### 2. Trae / VS Code (Continue Extension)
Add to your `~/.continue/config.json`:
```json
{
  "models": [
    {
      "title": "Local Qwen 2.5 Coder 32B",
      "provider": "ollama",
      "model": "qwen2.5-coder:32b"
    }
  ],
  "tabAutocompleteModel": {
    "title": "Local Qwen 2.5 Coder 7B",
    "provider": "ollama",
    "model": "qwen2.5-coder:7b"
  }
}
```
