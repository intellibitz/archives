# AI Artifacts & Knowledge Base

This top-level directory houses AI-related artifacts, models, agent configurations, prompt templates, evaluation datasets, and LLM integration guides across the repository.

🤖 **[Uncapped Agent Architecture](./UNCAPPED_AGENT_ARCHITECTURE.md)** | 📈 **[Router Ledger Telemetry](../router-ledger/)** | 📊 **[Cost Ranking](./COST_RANKING.md)** | 💻 **[Run Locally](./LOCAL_EXECUTION.md)** | ⚡ **[CachyOS Local AI Setup](./CACHYOS_LOCAL_AI_SETUP.md)** | 🟢 **[NVIDIA 128GB Hardware Guide](./NVIDIA_128GB_HARDWARE_GUIDE.md)** | 🇨🇳 vs 🌍 **[Chinese vs. Western AI](./CHINESE_VS_OTHERS_COMPARISON.md)** | 🏆 **[Leaderboard](./TOP_100_LEADERBOARD.md)** | 🧠 **[Architecture](./ARCHITECTURE_ECOSYSTEM.md)** | 🛠️ **[IDEs & Desktop](./IDE_PLUGINS_DESKTOP.md)** | 🔑 **[BYOK & API Keys](./BYOK_API_KEYS.md)** | 🌟 **[Top 100 GitHub AI Projects](./TOP_100_GITHUB_PROJECTS.md)**

## Top-Level AI Providers & Models
1. **[DeepSeek](../deepseek/)**: DeepSeek-V3, DeepSeek-R1 — [Real-World Cost Study](../deepseek/real_world_cost_study.md) \| [Cost Analysis](../deepseek/cost_analysis.md) \| [Eval Harness](../deepseek/eval_harness.md)
2. **[OpenRouter](../openrouter/)**: Unified API Gateway — [Cost Analysis](../openrouter/cost_analysis.md)
3. **[Chutes AI](../chutes/)**: Serverless GPU Compute — [Cost Analysis](../chutes/cost_analysis.md)
4. **[Qwen](../qwen/)**: Alibaba Qwen models — [Cost Analysis](../qwen/cost_analysis.md)
5. **[OpenAI](../openai/)**: GPT-4o, o1, o3-mini — [Cost Analysis](../openai/cost_analysis.md)
6. **[Claude](../claude/)**: Anthropic Claude 3.5 Sonnet & Opus — [Cost Analysis](../claude/cost_analysis.md)
7. **[Xiaomi MiMo](../xiaomi-mimo/)**: Xiaomi MiMo models — [Cost Analysis](../xiaomi-mimo/cost_analysis.md)
8. **[Zhipu GLM](../zhipu-glm/)**: Zhipu GLM-4 series — [Cost Analysis](../zhipu-glm/cost_analysis.md)
9. **[Agnes AI](../agnes-ai/)**: Agnes AI agents — [Cost Analysis](../agnes-ai/cost_analysis.md)
10. **[Google AI](../google-ai/)**: Gemini 1.5 Pro & 2.0 Flash — [Cost Analysis](../google-ai/cost_analysis.md)
11. **[Ministral / Mistral AI](../mistral/)**: Mistral Large & Codestral — [Cost Analysis](../mistral/cost_analysis.md)
12. **[Inclusion AI](../inclusion-ai/)**: Inclusion AI core models — [Cost Analysis](../inclusion-ai/cost_analysis.md)

## 🔗 Free AI Model Connectors & API Gateways
<!-- DYNAMIC_AI_CONNECTORS_START -->
> *Updated: `2026-10-03 08:06 UTC`*

| AI Provider & Connector | API Base Endpoint | Free / Low-Cost Models | Pricing Tier | Environment Variable |
| :--- | :--- | :--- | :--- | :--- |
| **[OpenRouter Free Tier](https://openrouter.ai/api/v1)** | `https://openrouter.ai/api/v1` | `deepseek/deepseek-r1:free`, `qwen/qwen-2.5-coder-32b:free`, `meta-llama/llama-3.3-70b-instruct:free` | **$0.00 / 1M** (Free tier) | `OPENROUTER_API_KEY` |
| **[Zhipu GLM](https://open.bigmodel.cn/api/paas/v4)** | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash` | **$0.00 / 1M** (100% Free API) | `ZHIPU_API_KEY` |
| **[Google Gemini Free Tier](https://generativelanguage.googleapis.com/v1beta/openai/)** | `https://generativelanguage.googleapis.com/v1beta/openai/` | `gemini-2.0-flash-exp`, `gemini-1.5-flash` | **$0.00** (15 Req/Min free in AI Studio) | `GEMINI_API_KEY` |
| **[Groq Cloud](https://api.groq.com/openai/v1)** | `https://api.groq.com/openai/v1` | `llama-3.3-70b-versatile`, `llama-3.1-8b-instant` | Free Developer Tier (Ultra-Fast LPUs) | `GROQ_API_KEY` |
| **[DeepSeek API Direct](https://api.deepseek.com/v1)** | `https://api.deepseek.com/v1` | `deepseek-chat` (V3), `deepseek-reasoner` (R1) | **$0.003 / 1M** (Cache Hit) / **$0.14** (Miss) | `DEEPSEEK_API_KEY` |
| **[Chutes AI](https://chutes.ai/v1)** | `https://chutes.ai/v1` | `deepseek-ai/DeepSeek-R1`, `Qwen/Qwen2.5-Coder-32B` | Serverless GPU Pay-Per-Token | `CHUTES_API_KEY` |
<!-- DYNAMIC_AI_CONNECTORS_END -->

## 🌟 Trending AI Repositories & Integrations
<!-- DYNAMIC_GITHUB_INTEGRATIONS_START -->
> *Telemetry Sync: `2026-10-03 08:06 UTC`*

| Open-Source AI Project | GitHub Stars | Description & Purpose |
| :--- | :--- | :--- |
| **[deepseek-ai/DeepSeek-R1](https://github.com/deepseek-ai/DeepSeek-R1)** | ⭐ Tracked | deepseek-ai/DeepSeek-R1 repository |
| **[deepseek-ai/DeepSeek-V3](https://github.com/deepseek-ai/DeepSeek-V3)** | ⭐ Tracked | deepseek-ai/DeepSeek-V3 repository |
| **[Qwen3-Coder](https://github.com/QwenLM/Qwen3-Coder)** (`QwenLM/Qwen2.5-Coder`) | **⭐ 16,841** | Qwen3-Coder is the code version of Qwen3, the large language model series dev... |
| **[vllm](https://github.com/vllm-project/vllm)** (`vllm-project/vllm`) | **⭐ 93,095** | A high-throughput and memory-efficient inference and serving engine for LLMs |
| **[llama.cpp](https://github.com/ggml-org/llama.cpp)** (`ggml-org/llama.cpp`) | **⭐ 130,187** | LLM inference in C/C++ |
| **[ollama](https://github.com/ollama/ollama)** (`ollama/ollama`) | **⭐ 182,078** | Get up and running with Kimi, GLM, MiniMax, DeepSeek, gpt-oss, Qwen, Gemma an... |
| **[open-webui](https://github.com/open-webui/open-webui)** (`open-webui/open-webui`) | **⭐ 153,847** | User-friendly AI Interface (Supports Ollama, OpenAI API, ...) |
| **[browser-use](https://github.com/browser-use/browser-use)** (`browser-use/browser-use`) | **⭐ 117,028** | Agents that use the browser. |
| **[Roo-Code](https://github.com/RooCodeInc/Roo-Code)** (`RooVetGit/Roo-Cline`) | **⭐ 24,289** | Roo Code gives you a whole dev team of AI agents in your code editor. |
| **[crewAI](https://github.com/crewAIInc/crewAI)** (`crewAIInc/crewAI`) | **⭐ 59,298** | Framework for orchestrating role-playing, autonomous AI agents. By fostering ... |
<!-- DYNAMIC_GITHUB_INTEGRATIONS_END -->
