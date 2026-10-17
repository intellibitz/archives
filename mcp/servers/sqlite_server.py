#!/usr/bin/env python3
"""
Native SQLite Database MCP (Model Context Protocol) Server over stdio.
Exposes database schema inspection and SQL query execution tools to AI agents via JSON-RPC 2.0.
"""

import json
import sqlite3
import sys

SERVER_NAME = "archives-sqlite-mcp"
SERVER_VERSION = "1.0.0"

TOOLS = [
    {
        "name": "list_tables",
        "description": "Lists all tables inside a SQLite database.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "db_path": {"type": "string", "description": "Path to SQLite database file."}
            },
            "required": ["db_path"]
        }
    },
    {
        "name": "query_sql",
        "description": "Executes a read-only SQL query against a SQLite database.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "db_path": {"type": "string", "description": "Path to SQLite database file."},
                "sql": {"type": "string", "description": "SQL query to execute."}
            },
            "required": ["db_path", "sql"]
        }
    }
]


def send_response(res):
    sys.stdout.write(json.dumps(res) + "\n")
    sys.stdout.flush()


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
                db_path = args.get("db_path")

                if not db_path:
                    send_response({"jsonrpc": "2.0", "id": req_id, "error": {"code": -32602, "message": "db_path is required"}})
                    continue

                conn = sqlite3.connect(db_path)
                cursor = conn.cursor()

                if name == "list_tables":
                    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
                    tables = [row[0] for row in cursor.fetchall()]
                    conn.close()
                    out = "\n".join(tables) if tables else "No tables found."
                    send_response({"jsonrpc": "2.0", "id": req_id, "result": {"content": [{"type": "text", "text": out}]}})
                elif name == "query_sql":
                    sql = args.get("sql", "")
                    cursor.execute(sql)
                    rows = cursor.fetchall()
                    conn.close()
                    out = json.dumps(rows, indent=2)
                    send_response({"jsonrpc": "2.0", "id": req_id, "result": {"content": [{"type": "text", "text": out}]}})
                else:
                    conn.close()
                    send_response({"jsonrpc": "2.0", "id": req_id, "error": {"code": -32601, "message": f"Unknown tool: {name}"}})
        except Exception as e:
            sys.stderr.write(f"SQLite MCP Error: {e}\n")


if __name__ == "__main__":
    main()
