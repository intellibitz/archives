# 💻 Trae AI IDE — Complete Windows & Multi-Platform Guide

[Trae](https://trae.ai) is an adaptive, AI-native Integrated Development Environment (IDE) built on top of the Visual Studio Code architecture. Developed by ByteDance, Trae provides native, out-of-the-box integration with frontier AI models including **DeepSeek-R1**, **DeepSeek-V3**, and **Claude 3.5 Sonnet**, coupled with an autonomous agentic engine and **native Model Context Protocol (MCP)** support.

---

## ⚡ 1. Why Trae as the Native DeepSeek IDE on Windows?

Unlike standard code editors that require complex plugin setups or web wrappers, Trae offers:

1. **Native DeepSeek Integration:** Direct access to DeepSeek-R1 (frontier reasoning) and DeepSeek-V3 (high-speed coding) natively within the IDE model dropdown.
2. **SOLO / Builder Agent Mode:** Autonomous agent execution capable of generating whole projects, editing multiple files simultaneously, diagnosing terminal outputs, and fixing errors automatically.
3. **Built-in Model Context Protocol (MCP):** Native stdio and SSE MCP engine (`byted-solo.builtin-mcp`) allowing the AI to query external databases, explore local filesystems, and interact with Git.
4. **100% VS Code Ecosystem Compatibility:** Seamless one-click migration for VS Code themes, keybindings, settings, and extensions.
5. **No Fixed Seat Subscriptions Required:** Compatible with both generous built-in model quotas and direct **Bring Your Own Key (BYOK)** Pay-As-You-Go API endpoints.

---

## 📦 2. Installation on Windows

### Option A: Windows Package Manager (`winget`) — Recommended
Run PowerShell or Windows Terminal as your current user:
```powershell
winget install --id ByteDance.Trae -e --accept-package-agreements --accept-source-agreements
```

### Option B: Executable Launch Path
Once installed, Trae's executable is located at:
```powershell
& "$env:LOCALAPPDATA\Programs\Trae\Trae.exe"
```

---

## 🎯 3. Operating Modes in Trae

Trae organizes AI development into two distinct operating modes:

### 1. Chat & Inline Editing Mode (`Ctrl + L` / `Ctrl + I`)
* **Interactive Chat (`Ctrl + L`):** Ask questions about the current codebase, explain complex functions, or inspect diffs.
* **Inline Code Generation (`Ctrl + I`):** Highlight a code block, press `Ctrl + I`, and instruct the model to refactor, optimize, or write unit tests directly in the editor buffer.

### 2. SOLO / Builder Mode (Autonomous Agent)
* **Goal-Oriented Workflows:** Provide high-level instructions (e.g., *"Add authentication middleware and write integration tests with pytest"*).
* **Autonomous File Manipulation:** Trae creates, edits, and removes files across the repository tree.
* **Self-Healing Terminal Loop:** Trae executes terminal commands, reads errors, and automatically refactors code until all tests pass.

---

## 🔌 4. Configuring Model Context Protocol (MCP) in Trae

Trae features first-class native MCP support. You can connect Trae directly to the Model Context Protocol servers in this repository:

### Step 1: Open MCP Settings
In Trae, navigate to:
**Settings (`Ctrl + ,`) > Extensions > Model Context Protocol (MCP)**
or locate the `mcp_servers.json` configuration in your Trae user profile:
`%APPDATA%\Trae\User\globalStorage\byted-solo.builtin-mcp\mcp_settings.json`

### Step 2: Add Archives MCP Servers
Paste the pre-configured server definitions from [`mcp/config/trae_mcp.json`](file:///e:/src/github.com/intellibitz/archives/mcp/config/trae_mcp.json):
```json
{
  "mcpServers": {
    "archives-filesystem": {
      "command": "python",
      "args": [
        "E:/src/github.com/intellibitz/archives/mcp/servers/filesystem_server.py"
      ]
    },
    "archives-git": {
      "command": "python",
      "args": [
        "E:/src/github.com/intellibitz/archives/mcp/servers/git_server.py"
      ]
    },
    "archives-sqlite": {
      "command": "python",
      "args": [
        "E:/src/github.com/intellibitz/archives/mcp/servers/sqlite_server.py"
      ]
    }
  }
}
```

Once saved, Trae's Builder Mode can invoke filesystem exploration, git inspection, and sqlite queries natively.

---

## 🔑 5. Custom DeepSeek API Configuration (BYOK)

To connect your own uncapped Pay-As-You-Go DeepSeek API key for infinite throughput:

1. Open Trae Settings (`Ctrl + ,`).
2. Search for `AI Provider` or click the gear icon in the AI sidebar.
3. Select **DeepSeek** as the provider.
4. Input your API Key (`sk-...`) and set the Base URL to:
   ```
   https://api.deepseek.com/v1
   ```
5. You now have uncapped access to `deepseek-chat` (V3) and `deepseek-reasoner` (R1) with 99%+ prompt cache hit rates.

---

## 📊 6. Comparison: Trae vs. Cursor vs. VS Code (Continue / Roo)

| Feature | Trae IDE (ByteDance) | Cursor (Anysphere) | VS Code + Continue / Roo Code |
| :--- | :--- | :--- | :--- |
| **DeepSeek-R1 / V3** | ✅ Built-in native support | ⚠️ Requires custom API key | ✅ Full support via BYOK / Ollama |
| **Pricing Model** | 🎁 Generous Free Tier + BYOK | 💳 $20/month Subscription + Caps | 🆓 100% Free Open Source + BYOK |
| **MCP Integration** | ✅ Native builtin extension | ⚠️ Community config / Stdio | ✅ Full stdio and SSE support |
| **Autonomous Builder** | ✅ Built-in SOLO Mode | ✅ Built-in Composer | ✅ Built-in Roo Code / Cline loops |
| **Windows Support** | ✅ Native 64-bit Windows installer | ✅ Native 64-bit Windows | ✅ Native Windows |
