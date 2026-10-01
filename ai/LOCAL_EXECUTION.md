# Running AI Models Locally For Free (Open-Weights Guide)

While proprietary models (like OpenAI GPT-4o, o1, and Anthropic Claude) require cloud APIs, many of the top AI providers offer **open-weights / open-source models** that you can download and run completely free on your own hardware using tools like **Ollama**, **vLLM**, **Llama.cpp**, or **Hugging Face Transformers**.

---

## 🚀 Quickest Local Setup: Ollama
[Ollama](https://ollama.com) is the easiest way to run local models on macOS, Linux, and Windows. Once installed, you can spin up models with a single terminal command.

### 1. Qwen (Alibaba)
Run Qwen2.5 (General / Coding):
```bash
ollama run qwen2.5:72b
# Or lightweight coding model:
ollama run qwen2.5-coder:7b
```

### 2. DeepSeek (DeepSeek-R1 & V3 Distills)
Run DeepSeek reasoning models locally:
```bash
# DeepSeek-R1 distilled on Llama or Qwen architectures
ollama run deepseek-r1:70b
ollama run deepseek-r1:8b
```

### 3. Mistral AI & Ministral
Run Mistral, Codestral, or Ministral locally:
```bash
ollama run mistral
ollama run codestral
ollama run ministral:8b
```

### 4. Zhipu GLM
Run GLM open-weights models:
```bash
ollama run glm4:9b
```

### 5. Google (Gemma 2)
While Gemini models are cloud-only, Google's open-weights **Gemma 2** series is available locally:
```bash
ollama run gemma2:27b
```

---

## ⚡ High-Performance Production Setup: vLLM
For serving open models with high throughput and PagedAttention in production:
```bash
pip install vllm
python -m vllm.entrypoints.openai.api_server \
    --model deepseek-ai/DeepSeek-R1-Distill-Qwen-7B \
    --port 8000
```

---

## 🖥️ Graphical Local UI: LM Studio / AnythingLLM
If you prefer a desktop application with a ChatGPT-like UI:
1. Download [LM Studio](https://lmstudio.ai).
2. Search and download **Qwen2.5**, **DeepSeek-R1-Distill**, or **Mistral**.
3. Click **Start Local Server** (fully compatible with OpenAI API client libraries).
