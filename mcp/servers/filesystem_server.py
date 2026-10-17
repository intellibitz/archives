#!/usr/bin/env python3
"""
Native Filesystem MCP (Model Context Protocol) Server over stdio.
Exposes file inspection, directory search, and content reading tools to AI agents via JSON-RPC 2.0.
"""

import json
import os
import sys

# Protocol Constants
SERVER_NAME = "archives-filesystem-mcp"
SERVER_VERSION = "1.0.0"

# Exposed Tools Schema
TOOLS = [
    {
        "name": "list_directory",
        "description": "Lists files and subdirectories inside a given path.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "path": {
                    "type": "string",
                    "description": "Absolute or relative directory path to inspect."
                }
            },
            "required": ["path"]
        }
    },
    {
        "name": "read_file",
        "description": "Retrieves text content from a specified file.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "path": {
                    "type": "string",
                    "description": "Path to the target file."
                },
                "max_bytes": {
                    "type": "integer",
                    "description": "Maximum bytes to read (default: 50,000)."
                }
            },
            "required": ["path"]
        }
    },
    {
        "name": "search_files",
        "description": "Searches for files matching a pattern inside a directory.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "directory": {
                    "type": "string",
                    "description": "Directory to search in."
                },
                "pattern": {
                    "type": "string",
                    "description": "Filename search pattern or substring."
                }
            },
            "required": ["directory", "pattern"]
        }
    }
]


def send_response(response):
    sys.stdout.write(json.dumps(response) + "\n")
    sys.stdout.flush()


def handle_initialize(req_id):
    return {
        "jsonrpc": "2.0",
        "id": req_id,
        "result": {
            "protocolVersion": "2024-11-05",
            "capabilities": {
                "tools": {}
            },
            "serverInfo": {
                "name": SERVER_NAME,
                "version": SERVER_VERSION
            }
        }
    }


def handle_tools_list(req_id):
    return {
        "jsonrpc": "2.0",
        "id": req_id,
        "result": {
            "tools": TOOLS
        }
    }


def handle_tools_call(req_id, params):
    tool_name = params.get("name")
    args = params.get("arguments", {})

    if tool_name == "list_directory":
        path = args.get("path", ".")
        try:
            entries = os.listdir(path)
            items = []
            for name in entries:
                full = os.path.join(path, name)
                is_dir = os.path.isdir(full)
                items.append(f"{'[DIR] ' if is_dir else '[FILE]'} {name}")
            content = "\n".join(items) if items else "Directory is empty."
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {
                    "content": [{"type": "text", "text": content}]
                }
            }
        except Exception as e:
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32603, "message": f"Error listing directory: {e}"}
            }

    elif tool_name == "read_file":
        path = args.get("path")
        max_bytes = args.get("max_bytes", 50000)
        try:
            with open(path, "r", encoding="utf-8", errors="replace") as f:
                content = f.read(max_bytes)
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {
                    "content": [{"type": "text", "text": content}]
                }
            }
        except Exception as e:
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32603, "message": f"Error reading file: {e}"}
            }

    elif tool_name == "search_files":
        directory = args.get("directory", ".")
        pattern = args.get("pattern", "").lower()
        matches = []
        try:
            for root, _, files in os.walk(directory):
                for f in files:
                    if pattern in f.lower():
                        matches.append(os.path.join(root, f))
                    if len(matches) >= 100:
                        break
            content = "\n".join(matches) if matches else "No matching files found."
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {
                    "content": [{"type": "text", "text": content}]
                }
            }
        except Exception as e:
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32603, "message": f"Error searching files: {e}"}
            }

    else:
        return {
            "jsonrpc": "2.0",
            "id": req_id,
            "error": {"code": -32601, "message": f"Tool not found: {tool_name}"}
        }


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
                send_response(handle_initialize(req_id))
            elif method == "notifications/initialized":
                pass
            elif method == "tools/list":
                send_response(handle_tools_list(req_id))
            elif method == "tools/call":
                params = req.get("params", {})
                send_response(handle_tools_call(req_id, params))
            else:
                if req_id is not None:
                    send_response({
                        "jsonrpc": "2.0",
                        "id": req_id,
                        "error": {"code": -32601, "message": f"Method not supported: {method}"}
                    })
        except Exception as e:
            sys.stderr.write(f"MCP Server Error: {e}\n")


if __name__ == "__main__":
    main()
