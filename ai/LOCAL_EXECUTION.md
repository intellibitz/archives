# Running AI Models Locally For Free (Open-Weights Guide)

While proprietary models (like OpenAI GPT-4o, o1, and Anthropic Claude) require cloud APIs, many of the top AI providers offer **open-weights / open-source models** that you can download and run completely free on your own hardware using dedicated desktop apps or command-line tools.

---

## 🖥️ Official Desktop Application Downloads

If you prefer native graphical desktop apps (ChatGPT-like interfaces with no coding required), download and install any of the following free desktop clients:

### 1. LM Studio (Recommended for Power Users)
- **Features:** Full hardware acceleration (Apple Silicon / NVIDIA / AMD), model search on Hugging Face, built-in local OpenAI-compatible API server, and prompt playground.
- **Download:** [lmstudio.ai/download](https://lmstudio.ai/download) (Available for macOS Apple Silicon/Intel, Windows, and Linux).
- **Usage:** Open app -> Search for `DeepSeek-R1-Distill-Qwen-7B` or `Qwen2.5-7B-Instruct` -> Download -> Chat or click **Local Server**.

### 2. [Ollama Desktop](https://github.com/ollama/ollama)
- **Features:** Background service daemon with system tray control for running models locally and exposing them to local tools and IDE extensions.
- **Download:** [ollama.com/download](https://ollama.com/download) ([GitHub](https://github.com/ollama/ollama)) (Available for macOS, Windows, and Linux).
- **Usage:** Install app -> Open terminal -> Run `ollama run qwen2.5` or `ollama run deepseek-r1:8b`.

### 3. Jan.ai
- **Features:** 100% offline, privacy-first desktop application ([GitHub](https://github.com/janhq/jan)) that runs open-source models locally on your machine.
- **Download:** [jan.ai](https://jan.ai) (Available for macOS, Windows, and Linux).
- **Usage:** Download -> Hub -> Install desired model (Qwen, Mistral, DeepSeek) -> Start chatting offline.

### 4. [GPT4All (by Nomic AI)](https://github.com/nomic-ai/gpt4all)
- **Features:** Optimized for consumer laptops and desktops (even without dedicated GPUs), running local models efficiently.
- **Download:** [gpt4all.io](https://gpt4all.io) ([GitHub](https://github.com/nomic-ai/gpt4all)) (Available for macOS, Windows, and Linux).
- **Usage:** Download installer -> Launch app -> Select model from local model explorer -> Chat.

### 5. [AnythingLLM Desktop](https://github.com/Mintplex-Labs/anything-llm)
- **Features:** Full desktop application with built-in RAG (Retrieval-Augmented Generation) so you can chat with your local documents and PDFs using local models.
- **Download:** [useanything.com](https://useanything.com) ([GitHub](https://github.com/Mintplex-Labs/anything-llm)) (Available for macOS, Windows, and Linux).
- **Usage:** Download -> Select local LLM provider (Ollama / LM Studio) -> Upload documents -> Chat.

---

## 💻 Hardware & GPU Requirements for Local Models

Running LLMs locally requires sufficient **VRAM (GPU Memory)** or **System RAM** (using 4-bit / 8-bit quantization for efficiency).

| Model Parameter Size | Min. VRAM / RAM Required (4-bit Quantization) | Recommended Hardware |
| :--- | :--- | :--- |
| **3B – 8B Models** <br>*(e.g., Ministral 8B, Qwen2.5-7B, DeepSeek-R1-8B)* | **8 GB** | Standard laptops, MacBooks (8GB+ RAM), mid-range GPUs (RTX 3060/4060). |
| **14B – 32B Models** <br>*(e.g., Gemma 2 27B, Qwen2.5-14B)* | **16 GB – 24 GB** | High-end consumer GPUs (RTX 3090/4090 24GB) or Apple Mac (24GB+ Unified Memory). |
| **70B – 72B Models** <br>*(e.g., DeepSeek-R1-70B, Qwen2.5-72B)* | **48 GB – 64 GB+** | Multi-GPU rigs, Mac Studio (64GB/128GB Unified Memory), or cloud instance. |

### Hardware Recommendations:
- **Apple Silicon (M1/M2/M3/M4 Pro/Max/Ultra):** Ideal for local inference due to high-bandwidth **Unified Memory** sharing RAM and GPU seamlessly.
- **NVIDIA GPUs (RTX series with CUDA):** Best-in-class acceleration for Ollama, vLLM, and LM Studio.
- **CPU Fallback:** GPT4All and Ollama can run models on CPU/System RAM if no dedicated GPU is present, though token generation speeds will be significantly slower.

---

## 🚀 CLI Setup: Ollama Terminal Commands

Once you have Ollama installed, you can spin up open-weights models from the supported providers instantly:

### 1. Qwen (Alibaba)
```bash
ollama run qwen2.5:72b
# Or lightweight coding model:
ollama run qwen2.5-coder:7b
```

### 2. DeepSeek (DeepSeek-R1 & V3 Distills)
```bash
ollama run deepseek-r1:70b
ollama run deepseek-r1:8b
```

### 3. Mistral AI & Ministral
```bash
ollama run mistral
ollama run codestral
ollama run ministral:8b
```

### 4. Zhipu GLM
```bash
ollama run glm4:9b
```

### 5. Google (Gemma 2)
```bash
ollama run gemma2:27b
```

---

## ⚡ High-Performance Production Setup: [vLLM](https://github.com/vllm-project/vllm)
For serving open models with high throughput and PagedAttention in production ([vLLM GitHub](https://github.com/vllm-project/vllm)):
```bash
pip install vllm
python -m vllm.entrypoints.openai.api_server \
    --model deepseek-ai/DeepSeek-R1-Distill-Qwen-7B \
    --port 8000
```
