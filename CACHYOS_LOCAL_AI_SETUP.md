# CachyOS Local AI Setup & Performance Optimization Guide

This document details the configuration, tuning, local inference engines, benchmarks, and IDE integration for running LLMs on CachyOS (Arch Linux).

---

## 💻 Hardware Environment Profile

* **CPU**: Intel Core i7-13850HX (20 Cores / 28 Threads)
* **GPU**: NVIDIA RTX 2000 Ada Generation (8 GB VRAM, Compute 89)
* **RAM**: 220 GB System RAM (DDR5)
* **OS**: CachyOS Linux (x86_64-v3 / v4 optimized kernel)

---

## 🛠️ Local Inference Engine Configuration

### 1. Ollama Service (Daemon & CUDA Acceleration)
Systemd unit drop-in `/etc/systemd/system/ollama.service.d/optimize.conf`:

```ini
[Service]
LimitMEMLOCK=infinity
LimitNOFILE=65536
Environment="OLLAMA_KEEP_ALIVE=5m"
Environment="OLLAMA_MAX_LOADED_MODELS=1"
Environment="OLLAMA_NUM_PARALLEL=1"
Environment="OLLAMA_KV_CACHE_TYPE=q8_0"
Environment="OLLAMA_FLASH_ATTENTION=1"
Environment="OLLAMA_CONTEXT_LENGTH=65536"
Environment="OLLAMA_NUM_THREADS=20"
Environment="OLLAMA_GPU_OVERHEAD=512"
Environment="OLLAMA_HOST=0.0.0.0:11434"
```

### 2. Native CUDA 13.4 `llama.cpp` (`llama-server`)
Custom-compiled with native `x86_64-v3/v4` host flags and Ada Lovelace CUDA SM 89 architecture:

```bash
cmake -B build -DGGML_CUDA=ON -DCMAKE_CUDA_ARCHITECTURES="89" -DCMAKE_BUILD_TYPE=Release
cmake --build build --config Release -j $(nproc)
```

Installed executables: `~/.local/bin/llama-server`, `~/.local/bin/llama-cli`.

### 3. vLLM Engine (High-Throughput PagedAttention)
Environment: `~/.vllm-env` (PyTorch 2.6 + CUDA 12.4 + Triton + xFormers)
CLI Link: `~/.local/bin/vllm`

---

## 📊 Performance & Benchmark Metrics (Live System Audit)

| Model Tier & Name | Parameters / Size | Architecture | Generation Speed (`eval_tps`) | Prompt Speed (`prompt_tps`) | Total Latency (128 Tokens) | Execution Profile |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **`qwen2.5-coder:7b`** | 7B (4.7 GB) | Dense 7B | ⚡ **46.42 Tokens/Sec** | 🚀 **677.19 Tokens/Sec** | **6.90s** | 100% NVIDIA RTX 2000 Ada VRAM |
| **`qwen3-coder:30b`** | 30B (18 GB) | Dense 30B | ⚡ **11.42 Tokens/Sec** | **32.45 Tokens/Sec** | **20.09s** | Hybrid (VRAM + System RAM) |
| **`gpt-oss_opt120b:latest`** | 120B (65 GB) | MoE 120B | 🐘 **5.61 Tokens/Sec** | **16.83 Tokens/Sec** | **82.23s** | System RAM (219 GB DDR5) |
| **`deepseek-r1_opt32b:latest`** | 32B (19 GB) | Dense 32B Reasoning | 🧠 **1.39 Tokens/Sec** | **22.68 Tokens/Sec** | **103.25s** | System RAM + GPU Offload |
| **`llama33_opt70b:latest`** | 70B (42 GB) | Dense 70B | 🦙 **0.51 Tokens/Sec** | **10.09 Tokens/Sec** | **287.76s** | System RAM (219 GB DDR5) |
| **`deepseek-r1_opt70b:latest`** | 70B (42 GB) | Dense 70B Reasoning | 🧠 **0.37 Tokens/Sec** | **7.08 Tokens/Sec** | **353.92s** | System RAM (219 GB DDR5) |

---

## 🔌 IDE & Extension Configuration (VS Code & Android Studio)

### Continue Extension (`~/.continue/config.yaml` & `~/.continue/config.json`)
Applies to both VS Code and JetBrains / Android Studio Continue plugins:

```yaml
name: Local Hardware Accelerated AI
version: 1.0.0
schema: v1
models:
  - name: Qwen 2.5 Coder 7B (Fast)
    provider: ollama
    model: qwen2.5-coder:7b
    roles:
      - chat
      - edit
      - inline_edit
  - name: DeepSeek R1 32B (Reasoning)
    provider: ollama
    model: deepseek-r1:32b
    roles:
      - chat
  - name: Llama 3.3 70B (Heavy)
    provider: ollama
    model: llama3.3:70b
    roles:
      - chat
tabAutocompleteModel:
  name: Autocomplete (Qwen 2.5 Coder)
  provider: ollama
  model: qwen2.5-coder:7b
```

### Local API Endpoints
* **Ollama**: `http://localhost:11434`
* **Open WebUI**: `http://localhost:8080`
* **llama-server**: `http://localhost:8081/v1`
* **vLLM**: `http://localhost:8000/v1`

---

## 🔐 Security Audit & Credentials Policy

* **No Hardcoded Secrets**: All configuration files in the project use sanitized placeholders (`your_api_key_here`, `not-needed`).
* **Environment Isolation**: API tokens are provided via system environment variables (`TOGETHER_API_KEY`, `DEEPINFRA_TOKEN`) or isolated local configuration paths outside version control.
