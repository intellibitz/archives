# 🔌 Model Context Protocol (MCP) — Tools, Servers & Clients Hub

The **Model Context Protocol (MCP)** is an open standard introduced by Anthropic that enables AI agents, LLMs, and desktop clients (Claude Desktop, Cursor, Claude Code, OpenHands, Roo Code) to securely connect with external tools, data sources, and system resources over standardized JSON-RPC transports.

---

## 🔑 Key Concepts & Architecture

MCP standardizes three primary primitives:

1. **Tools (`tools/call`)**: Executable functions exposed by MCP servers that AI agents can call (e.g., file reading, database querying, git manipulation).
2. **Resources (`resources/read`)**: Static or dynamic data context exposed to LLMs (e.g., system logs, file contents, API endpoints).
3. **Prompts (`prompts/get`)**: Pre-configured prompt templates provided by MCP servers for agent workflows.

```
┌─────────────────────────────────────────────────────────┐
│ MCP Client (Claude Desktop / Cursor / Python Client)    │
└────────────────────────────┬────────────────────────────┘
                             │ (JSON-RPC 2.0 over stdio or SSE)
                             ▼
┌─────────────────────────────────────────────────────────┐
│ MCP Server (Filesystem / Git / SQLite / Custom Server)   │
│ • Tools: list_files, read_file, execute_sql, git_status │
│ • Resources: file:///, db://schema                      │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 MCP Implementations in Archives

The `mcp/` hub provides production-grade Python implementations for servers, clients, and IDE configuration templates:

```text
mcp/
├── README.md                   # Master MCP guide and architecture documentation
├── clients/
│   └── mcp_client.py           # Executable stdio MCP Client runner
├── servers/
│   ├── filesystem_server.py    # Native Filesystem MCP Server
│   ├── git_server.py           # Git Repository MCP Server
│   └── sqlite_server.py        # SQLite Database MCP Server
└── config/
    ├── claude_desktop_config.json  # Claude Desktop App MCP Config
    └── cursor_mcp.json            # Cursor IDE MCP Config
```

---

## 🚀 Quick Start: Running MCP Servers & Clients

### 1. Test the Executable MCP Client
Connect the MCP client to the Filesystem MCP Server:
```bash
python mcp/clients/mcp_client.py --server "python mcp/servers/filesystem_server.py"
```

### 2. Run the Git MCP Server
```bash
python mcp/servers/git_server.py
```

### 3. Run the SQLite MCP Server
```bash
python mcp/servers/sqlite_server.py --db path/to/database.db
```

---

## 🛠️ Configuring MCP in IDEs & Desktop Clients

### A. Claude Desktop Integration (`claude_desktop_config.json`)
Copy the config template from [`mcp/config/claude_desktop_config.json`](./config/claude_desktop_config.json) to:
* **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
* **macOS/Linux:** `~/.config/Claude/claude_desktop_config.json`

### B. Cursor & Roo Code Integration
Add MCP server commands to your Cursor Settings (`Settings -> Features -> MCP`) or Roo Code configuration file [`mcp/config/cursor_mcp.json`](./config/cursor_mcp.json).
