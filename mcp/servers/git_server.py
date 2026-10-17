#!/usr/bin/env python3
"""
Native Git Repository MCP (Model Context Protocol) Server over stdio.
Exposes Git status, commit history, and diff tools to AI agents via JSON-RPC 2.0.
"""

import json
import os
import subprocess
import sys

SERVER_NAME = "archives-git-mcp"
SERVER_VERSION = "1.0.0"

TOOLS = [
    {
        "name": "git_status",
        "description": "Returns current git repository status.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "repo_path": {"type": "string", "description": "Path to git repository (default: current dir)."}
            }
        }
    },
    {
        "name": "git_log",
        "description": "Returns recent git commits log.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "repo_path": {"type": "string", "description": "Path to git repository."},
                "max_commits": {"type": "integer", "description": "Maximum number of commits to list (default: 5)."}
            }
        }
    },
    {
        "name": "git_diff",
        "description": "Returns current git diff output.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "repo_path": {"type": "string", "description": "Path to git repository."}
            }
        }
    }
]


def send_response(res):
    sys.stdout.write(json.dumps(res) + "\n")
    sys.stdout.flush()


def run_git(args, cwd=None):
    res = subprocess.run(["git"] + args, cwd=cwd, capture_output=True, text=True)
    return res.stdout if res.returncode == 0 else res.stderr


def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            req = json.loads(line)
            req_id = req.get("id")
            method = req.get("method")

            if method == "initialize":
                send_response({
                    "jsonrpc": "2.0",
                    "id": req_id,
                    "result": {
                        "protocolVersion": "2024-11-05",
                        "capabilities": {"tools": {}},
                        "serverInfo": {"name": SERVER_NAME, "version": SERVER_VERSION}
                    }
                })
            elif method == "notifications/initialized":
                pass
            elif method == "tools/list":
                send_response({"jsonrpc": "2.0", "id": req_id, "result": {"tools": TOOLS}})
            elif method == "tools/call":
                params = req.get("params", {})
                name = params.get("name")
                args = params.get("arguments", {})
                repo_path = args.get("repo_path", ".")

                if name == "git_status":
                    out = run_git(["status"], cwd=repo_path)
                    send_response({"jsonrpc": "2.0", "id": req_id, "result": {"content": [{"type": "text", "text": out}]}})
                elif name == "git_log":
                    max_c = str(args.get("max_commits", 5))
                    out = run_git(["log", f"-{max_c}", "--oneline"], cwd=repo_path)
                    send_response({"jsonrpc": "2.0", "id": req_id, "result": {"content": [{"type": "text", "text": out}]}})
                elif name == "git_diff":
                    out = run_git(["diff"], cwd=repo_path)
                    send_response({"jsonrpc": "2.0", "id": req_id, "result": {"content": [{"type": "text", "text": out or "No changes."}]}})
                else:
                    send_response({"jsonrpc": "2.0", "id": req_id, "error": {"code": -32601, "message": f"Unknown tool: {name}"}})
        except Exception as e:
            sys.stderr.write(f"Git MCP Error: {e}\n")


if __name__ == "__main__":
    main()
