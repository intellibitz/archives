# AI Architecture & Ecosystem: Models, Engines, Agents, Multi-Agent Hierarchies & MCP

This document defines the core layers of modern AI system design tracked within this repository: **Models**, **Engines**, **Agents**, **Agents of Agents (Hierarchical Multi-Agent Systems)**, and **Model Context Protocol (MCP)**.

---

## 1. 🧬 Models (Foundation & Specialized)
Models are the underlying intelligence engines trained on massive corpora.
- **Foundation LLMs:** General-purpose reasoning and generation engines (e.g., DeepSeek-R1, GPT-4o, Claude 3.5 Sonnet, [QwenLM/Qwen](https://github.com/QwenLM/Qwen)).
- **Vision-Language Models (VLMs):** Multimodal models processing text and image/video inputs (e.g., [QwenLM/Qwen-VL](https://github.com/QwenLM/Qwen-VL), GPT-4o, Gemini 1.5 Pro).
- **Embedding & Reranker Models:** Specialized models for vector search, semantic similarity, and document reranking (e.g., [FlagOpen/FlagEmbedding](https://github.com/FlagOpen/FlagEmbedding), OpenAI text-embedding-3).

---

## 2. ⚡ Engines (Inference & Serving Runtimes)
Engines execute model weights efficiently on consumer or cloud hardware.
- **Production Serving:** 
  - **[vllm-project/vllm](https://github.com/vllm-project/vllm)** (PagedAttention for high throughput)
  - **[NVIDIA/TensorRT-LLM](https://github.com/NVIDIA/TensorRT-LLM)** (NVIDIA optimized serving)
  - **[huggingface/text-generation-inference](https://github.com/huggingface/text-generation-inference)** (TGI)
- **Local / Edge Runtimes:** 
  - **[ollama/ollama](https://github.com/ollama/ollama)**
  - **[ggerganov/llama.cpp](https://github.com/ggerganov/llama.cpp)**
  - **[Mozilla-Ocho/llamafile](https://github.com/Mozilla-Ocho/llamafile)** (optimized GGUF execution)

---

## 3. 🤖 Agents (Autonomous Tool-Using Entities)
Agents combine LLMs with memory, planning loops (ReAct / Plan-and-Solve), and tool execution capabilities.
- **Tool-Use & Function Calling:** Executing APIs, querying databases, running shell commands, and parsing structured JSON outputs.
- **Retrieval-Augmented Generation (RAG):** Dynamic retrieval from vector databases and knowledge bases to ground responses in verified facts.
- **Stateful Memory:** Short-term working memory (conversation buffer) and long-term memory (episodic/semantic storage).

---

## 4. 👥 Agents of Agents (Hierarchical Multi-Agent Systems)
When tasks exceed single-agent capabilities, hierarchical **"Agents of Agents"** architectures deploy specialized agent swarms managed by orchestrators.
- **Orchestrator-Worker Pattern:** A supervisory "Manager Agent" breaks down complex user objectives into sub-tasks, delegates them to specialist agents (e.g., Coding Agent, Research Agent, QA Testing Agent), synthesizes their outputs, and manages error correction loops.
- **Consensus & Debate Protocols:** Multiple independent agents critique each other's outputs (e.g., Red-Teamer agent challenging a Developer agent) before finalizing results.

---

## 5. 🔌 MCP (Model Context Protocol)
- **Official Specification:** [modelcontextprotocol.io](https://modelcontextprotocol.io)
- **GitHub Repository:** [modelcontextprotocol/modelcontextprotocol](https://github.com/modelcontextprotocol)
- **Description:** Anthropic's open standard that allows developers to build secure, bidirectional connections between AI models (clients) and data sources/tools (servers).
- **Architecture:** 
  - **MCP Clients:** LLM-powered interfaces (e.g., Claude Desktop, IDE extensions, custom agent applications).
  - **MCP Servers:** Lightweight services providing direct access to local files, Git repositories, databases (PostgreSQL/MySQL), browser tools, and APIs.
