# 💻 Claude Code CLI — Anthropic Official Autonomous Coding Agent

**Claude Code** (`claude`) is Anthropic's official command-line interface (CLI) autonomous agent tool for software engineering. It operates directly inside terminal environments to read codebases, run commands, create git commits, and integrate with Model Context Protocol (MCP) servers.

---

## 🔑 Key Features & Capabilities

* **Terminal Native:** Runs natively in terminal shells (zsh, bash, PowerShell) with direct terminal tool execution.
* **Model Context Protocol (MCP):** Connects to custom MCP tools for database queries, local filesystem indexing, and API access.
* **Git & Worktree Integration:** Automatically tracks code modifications, manages branch checkouts, and writes clean commit messages.
* **Token & Budget Guardrails:** Configurable cost limits, token caps, and approval confirmations for shell commands.

---

## 🛠️ Architecture & Workflow

```
┌────────────────────────────────────────────────────────┐
│ Claude Code CLI (`claude`)                             │
└───────────────────────────┬────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌────────────────────────┐      ┌────────────────────────┐
│ Terminal / Shell       │      │ MCP Server Tools       │
│ • Command Execution    │      │ • Database Readers     │
│ • Git Operations       │      │ • File Search          │
│ • Test Harnesses       │      │ • Internal APIs        │
└────────────────────────┘      └────────────────────────┘
```

---

## 🚀 Quick Setup & Execution

### 1. Installation
```bash
npm install -g @anthropic-ai/claude-code
```

### 2. Execution
```bash
export ANTHROPIC_API_KEY="sk-ant-..."
claude
```

### 3. Example CLI Options
```bash
# Non-interactive single task execution
claude -p "Refactor fix_deps.py to handle missing dependencies gracefully"

# Budget capped execution
claude --max-thinking-tokens 4000
```
