# 🛡️ Pydantic AI — Type-Safe Production Agent Framework

**[Pydantic AI](https://github.com/pydantic/pydantic-ai)** is a Python agent framework built by the creators of Pydantic. It brings production-grade **type safety, dependency injection, and schema validation** to autonomous AI agent development.

---

## 🔑 Key Features & Capabilities

* **Pydantic V2 Type Safety:** All tool arguments, context inputs, and agent output structures are strictly validated by Pydantic models.
* **Dependency Injection:** Inject runtime dependencies (database pools, API clients, authenticated user tokens) directly into tool functions.
* **Model Agnostic & Fallback:** Seamless switching between **DeepSeek-V3**, **Google Gemini 2.0 Flash**, **OpenAI**, and **Groq**.
* **Streaming & Web Integration:** First-class async streaming for FastAPI and Granian web services.

---

## 🛠️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│ Pydantic AI Agent (Agent[Dependencies, ResultType])    │
└────────────────────────────┬────────────────────────────┘
                             │ (Type Validation)
                             ▼
┌─────────────────────────────────────────────────────────┐
│ Pydantic V2 Schema Models & Tool Dependency Injection    │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Setup & Execution

### 1. Installation
```bash
pip install pydantic-ai
```

### 2. Run Example Agent
```bash
python pydantic-ai/pydantic_agent.py
```
