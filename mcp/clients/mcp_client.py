#!/usr/bin/env python3
"""
Executable Python MCP (Model Context Protocol) Client Runner.
Launches stdio MCP server subprocesses, sends JSON-RPC 2.0 initialization, lists tools, and executes tool calls.
"""

import argparse
import json
import subprocess
import sys
import time


class MCPClient:
    def __init__(self, command):
        self.command = command
        self.proc = None
        self.request_id = 1

    def start(self):
        print(f"🔌 Launching MCP Server Subprocess: '{self.command}'...")
        self.proc = subprocess.Popen(
            self.command,
            shell=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1
        )
        time.sleep(0.1)

    def send_request(self, method, params=None):
        req = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method
        }
        if params is not None:
            req["params"] = params

        self.request_id += 1
        raw_json = json.dumps(req)
        self.proc.stdin.write(raw_json + "\n")
        self.proc.stdin.flush()

        response_line = self.proc.stdout.readline()
        if not response_line:
            err = self.proc.stderr.read()
            raise RuntimeError(f"MCP Server closed connection. Stderr: {err}")

        return json.loads(response_line)

    def initialize(self):
        print("🤝 Initializing MCP Session...")
        res = self.send_request("initialize")
        server_info = res.get("result", {}).get("serverInfo", {})
        print(f"✅ Connected to Server: {server_info.get('name')} (v{server_info.get('version')})")
        return res

    def list_tools(self):
        print("📋 Querying Available MCP Tools (`tools/list`)...")
        res = self.send_request("tools/list")
        tools = res.get("result", {}).get("tools", [])
        print(f"🛠️ Found {len(tools)} Tools:")
        for t in tools:
            print(f"   • {t.get('name')}: {t.get('description')}")
        return tools

    def call_tool(self, name, arguments=None):
        print(f"⚡ Calling MCP Tool: '{name}' with args: {arguments}...")
        params = {
            "name": name,
            "arguments": arguments or {}
        }
        res = self.send_request("tools/call", params)
        content = res.get("result", {}).get("content", [])
        for item in content:
            print(f"📋 Tool Result:\n{item.get('text')}\n")
        return res

    def close(self):
        if self.proc:
            self.proc.terminate()
            self.proc.wait()


def main():
    parser = argparse.ArgumentParser(description="MCP Stdio Client Runner")
    parser.add_argument(
        "--server",
        default="python mcp/servers/filesystem_server.py",
        help="Command to run the stdio MCP server"
    )
    args = parser.parse_args()

    client = MCPClient(args.server)
    try:
        client.start()
        client.initialize()
        tools = client.list_tools()

        # Demonstrate tool call if filesystem server is active
        if any(t.get("name") == "list_directory" for t in tools):
            client.call_tool("list_directory", {"path": "."})

    finally:
        client.close()


if __name__ == "__main__":
    main()
