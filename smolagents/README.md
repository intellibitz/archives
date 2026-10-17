# ⚡ Smolagents — Hugging Face Executable Code Agent Framework

**[Smolagents](https://github.com/huggingface/smolagents)** is Hugging Face's lightweight, code-first library for building autonomous AI agents. Unlike traditional frameworks that rely heavily on JSON tool calling, `smolagents` centers on **CodeAgents**—where the agent writes and executes raw Python code snippets inside a secure sandbox.

---

## 🔑 Key Features & Capabilities

* **CodeAction Execution:** The LLM generates Python code snippets directly to express its multi-step actions, leading to higher success rates on benchmarks like **GAIA**.
* **Sandboxed Python Interpreter:** Executes generated code inside `E2B` sandbox or local isolated Python runtime.
* **Minimal Footprint:** Extremely small codebase (~1,000 lines of code) with zero heavy framework bloat.
* **Hugging Face Hub Integration:** Push and pull custom agent tools directly from Hugging Face Spaces.

---

## 🛠️ CodeAgent vs ToolCallingAgent

```
┌─────────────────────────────────────────────────────────┐
│ CodeAgent (Smolagents)                                  │
│ Generates: `result = search("AI") ; print(result)`      │
└────────────────────────────┬────────────────────────────┘
                             │ (Direct Python Execution)
                             ▼
┌─────────────────────────────────────────────────────────┐
│ Local / E2B Python Sandbox                              │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Setup & Execution

### 1. Installation
```bash
pip install smolagents
```

### 2. Run Example Agent
```bash
python smolagents/smol_agent.py
```
