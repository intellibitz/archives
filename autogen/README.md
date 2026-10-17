# 🤖 Microsoft AutoGen (v0.4+ / AgentChat) — Event-Driven Multi-Agent Framework

**[Microsoft AutoGen](https://github.com/microsoft/autogen)** is an event-driven, asynchronous programming framework for building multi-agent conversational applications. The v0.4+ architecture introduces modular agent runtimes, cross-agent message passing, task-centric memory, and sandboxed code execution.

---

## 🔑 Key Features & Capabilities

* **Asynchronous & Event-Driven:** Built on top of `autogen_core` for high-concurrency multi-agent communication.
* **AgentChat Abstraction:** Simplified high-level API (`autogen_agentchat`) for creating multi-agent teams (`RoundRobinGroupChat`, `SelectorGroupChat`).
* **Task-Centric Memory:** Enables agents to maintain state, context windows, and code execution history across complex task loops.
* **Open-Source & BYOK Support:** Native compatibility with **DeepSeek-V3/R1**, **Ollama local models**, **OpenAI**, and **Anthropic**.

---

## 🛠️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│ AutoGen Core Runtime (Event Loop / Message Bus)        │
└────────────────────────────┬────────────────────────────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Assistant    │      │ Code Executor│      │ User Proxy   │
│ Agent        │      │ Agent        │      │ Agent        │
└──────────────┘      └──────────────┘      └──────────────┘
```

---

## 🚀 Quick Setup & Execution

### 1. Installation
```bash
pip install autogen-agentchat autogen-ext[openai]
```

### 2. Run Example Team Script
```bash
python autogen/autogen_team.py
```
