# 👐 OpenHands (Formerly OpenDevin) — Autonomous Software Engineer Platform

**[OpenHands](https://github.com/All-Hands-AI/OpenHands)** is the premier open-source autonomous software engineering platform. It acts as an AI developer capable of modifying code bases, running terminal commands, executing unit tests, browsing documentation, and resolving GitHub issues automatically.

---

## 🔑 Key Features & Capabilities

* **SWE-bench Leader:** Consistently ranks among top-performing open-source agents on benchmark benchmarks (SWE-bench Lite and SWE-bench Verified).
* **Flexible Sandboxing:** Supports isolated Docker containers, Kubernetes pods, or direct local workspace attachment.
* **Multi-Modal Agent Tools:** Integrates terminal bash execution, file editing, web browser exploration, and static analysis.
* **BYOK Endpoint Integration:** Fully compatible with **DeepSeek-V3 / R1**, **Google Gemini 2.0 Flash**, **Claude 3.5 Sonnet**, and **OpenRouter**.

---

## 🛠️ Architecture & Workflows

```
┌─────────────────────────────────────────────────────────┐
│ OpenHands Event Stream Controller                       │
└────────────────────────────┬────────────────────────────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Code Editor  │      │ Bash Terminal│      │ Web Browser  │
│ Tool         │      │ Sandbox      │      │ Tool         │
└──────────────┘      └──────────────┘      └──────────────┘
```

---

## 🚀 Configuration & Execution

### 1. Installation
```bash
pipx install openhands-ai
# or run via Docker
docker run -it -e SANDBOX_RUNTIME_CONTAINER_IMAGE=docker.all-hands.dev/all-hands-ai/runtime:0.28-nikolaik \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -p 3000:3000 \
    --add-host host.docker.internal:host-gateway \
    docker.all-hands.dev/all-hands-ai/openhands:0.28
```

### 2. Standalone Local Non-Docker Mode
To run OpenHands directly against local host directories without Docker:
```bash
export RUNTIME="local"
export SANDBOX_TYPE="local"
export WORKSPACE_BASE="$(pwd)"
openhands-web --config openhands/openhands_config.toml
```

### 3. DeepSeek Uncapped Configuration (`openhands_config.toml`)
See [`openhands_config.toml`](./openhands_config.toml) for complete DeepSeek & OpenRouter model provider settings.
