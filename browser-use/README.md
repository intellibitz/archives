# 🌐 Browser Use — Autonomous Web Browser AI Agent Framework

**[Browser Use](https://github.com/browser-use/browser-use)** is the leading open-source Python library for building **autonomous web browser agents**. It connects LLMs with Playwright to make web browsers accessible to AI agents.

---

## 🔑 Key Features & Capabilities

* **Vision + DOM Integration:** Combines visual screenshots with accessibility tree DOM parsing to interact with modern, dynamic websites.
* **Multi-Tab & Action Loop:** Supports multi-tab browsing, form filling, dropdown selection, file uploads, and content extraction.
* **Model Agnostic:** Works out of the box with **DeepSeek-V3**, **Google Gemini 2.0 Flash**, **OpenAI GPT-4o**, and **Anthropic Claude 3.5 Sonnet**.
* **Self-Correction & Memory:** Detects navigation errors or missing UI elements and autonomously retries with alternative actions.

---

## 🛠️ Architecture & Integration

```
┌──────────────────────────────────────────────────────────┐
│ Browser Use Agent (Agent, Controller, BrowserContext)    │
└────────────────────────────┬─────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
┌─────────────────────────┐       ┌─────────────────────────┐
│ Playwright Engine       │       │ LLM Backend             │
│ • Chromium / Firefox    │       │ • DeepSeek-V3 ($0.003)  │
│ • DOM Accessibility Tree│       │ • Gemini 2.0 Flash      │
│ • Screenshot Captures   │       │ • OpenRouter Unified    │
└─────────────────────────┘       └─────────────────────────┘
```

---

## 🚀 Quick Setup & Execution

### 1. Installation
```bash
pip install browser-use langchain-openai playwright
playwright install chromium
```

### 2. Environment Configuration
```bash
export DEEPSEEK_API_KEY="sk-..."
# Option B: OpenRouter
export OPENROUTER_API_KEY="sk-or-v1-..."
```

### 3. Run the Example Agent
```bash
python browser-use/example_agent.py
```

---

## 📊 Cost Optimization

For high-throughput web automation tasks, pair `browser-use` with **DeepSeek-V3** or **Google Gemini 2.0 Flash**. Because browser agents send repetitive DOM context across turns, DeepSeek's 99.5% prompt-caching hit ratio reduces execution cost by up to **90%** compared to proprietary models.
