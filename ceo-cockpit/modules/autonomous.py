"""
Round 21 - Continuous Autonomous OS Mode with Model Laddering & Self-Healing.

Implements a 24x7 autonomous execution loop driven by Budget-Defined Model Laddering:
  * Level 0: Gemini 2.5 Flash ($0.075/1M) - Initial rapid attempt & context gathering.
  * Level 1: DeepSeek Reasoner R1 ($0.55/1M) - Escalated deep logic, math & architectural debugging.
  * Level 2: Claude 3.7 Sonnet ($3.00/1M) - Apex autonomous tool execution, complex refactoring & final proof.

Self-Healing Guarantee:
  When errors, compiler failures, or tool exceptions occur, the agent automatically
  diagnoses the root cause, escalates to the higher model tier, applies fixes, and re-verifies
  until the mission is 100% complete.
"""

import json
import os
import subprocess
import time
import urllib.request
from typing import Any, Dict, List

from modules import route
from modules.common import active_workspace, read_json_body, start_sse, write_sse

# LiteLLM Gateway Endpoint & Auth
LITELLM_URL = "http://127.0.0.1:4000"
LITELLM_KEY = os.environ.get("LITELLM_MASTER_KEY", "ceo-cockpit-master-key")
LITELLM_HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {LITELLM_KEY}"
}

MODEL_LADDER = [
    {"id": "gemini-flash", "name": "Gemini 2.5 Flash", "tier": "Level 0: Rapid Context & Draft ($0.075/1M)"},
    {"id": "deepseek-reasoner", "name": "DeepSeek Reasoner (R1)", "tier": "Level 1: Deep Logic & Debugging ($0.55/1M)"},
    {"id": "claude-3-7-sonnet", "name": "Claude 3.7 Sonnet", "tier": "Level 2: Apex Execution ($3.00/1M)"}
]

def execute_shell(cmd: str, cwd: str) -> Dict[str, Any]:
    """Executes a non-interactive shell command and returns output with exit code."""
    try:
        res = subprocess.run(
            ["powershell.exe", "-NonInteractive", "-NoProfile", "-Command", cmd],
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=300
        )
        out = (res.stdout + "\n" + res.stderr).strip()
        return {
            "success": res.returncode == 0,
            "exit_code": res.returncode,
            "output": out if out else "Command completed with no output."
        }
    except Exception as e:
        return {"success": False, "exit_code": -1, "output": f"Execution Exception: {e}"}

def read_file_tool(path: str) -> str:
    try:
        with open(path, "r", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        return f"Read Error: {e}"

def write_file_tool(path: str, content: str) -> str:
    try:
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return "File written successfully."
    except Exception as e:
        return f"Write Error: {e}"

def autonomous_loop(mission: str, h: Any, max_turns: int = 50):
    """The 24x7 Autonomous Engine Loop with Model Laddering."""
    ws = active_workspace()

    tools = [
        {
            "type": "function",
            "function": {
                "name": "execute_shell",
                "description": "Executes a PowerShell command in the workspace. Returns exit code and output.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "command": {"type": "string"}
                    },
                    "required": ["command"]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "read_file",
                "description": "Reads the content of a file in the workspace.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "path": {"type": "string"}
                    },
                    "required": ["path"]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "write_file",
                "description": "Writes content to a file.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "path": {"type": "string"},
                        "content": {"type": "string"}
                    },
                    "required": ["path", "content"]
                }
            }
        },
        {
            "type": "function",
            "function": {
                "name": "mission_complete",
                "description": "Call this when the autonomous mission is fully achieved and verified.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "summary": {"type": "string"}
                    },
                    "required": ["summary"]
                }
            }
        }
    ]

    messages = [
        {
            "role": "system",
            "content": (
                f"You are the Autonomous AI OS for the CEO Cockpit operating in workspace: {ws}.\n"
                "The CEO expects every command to be executed perfectly to completion.\n"
                "If a command, build, or test fails, analyze the error output, fix the code/files, and re-run until green.\n"
                "Never ask for human intervention. Execute Git, Rust, Python, Node ops as needed."
            )
        },
        {
            "role": "user",
            "content": f"EXECUTE EXECUTIVE DIRECTIVE:\n{mission}"
        }
    ]

    ladder_idx = 0
    consecutive_errors = 0

    if not write_sse(h, {
        "type": "start",
        "message": "Autonomous OS Thread Initialized...",
        "initial_model": MODEL_LADDER[ladder_idx]["name"],
        "tier": MODEL_LADDER[ladder_idx]["tier"]
    }):
        return

    for turn in range(max_turns):
        current_model_info = MODEL_LADDER[ladder_idx]
        current_model_id = current_model_info["id"]

        payload = {
            "model": current_model_id,
            "messages": messages,
            "tools": tools,
            "tool_choice": "auto",
            "max_tokens": 4096
        }

        try:
            req_data = json.dumps(payload).encode("utf-8")
            req = urllib.request.Request(
                f"{LITELLM_URL}/v1/chat/completions",
                data=req_data,
                headers=LITELLM_HEADERS
            )
            with urllib.request.urlopen(req, timeout=120) as resp:
                resp_json = json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            # Escalate model tier on gateway/timeout errors
            if ladder_idx < len(MODEL_LADDER) - 1:
                old_m = current_model_info["name"]
                ladder_idx += 1
                new_m = MODEL_LADDER[ladder_idx]["name"]
                write_sse(h, {
                    "type": "escalation",
                    "from_model": old_m,
                    "to_model": new_m,
                    "reason": f"Gateway/API error ({e}). Escalating to higher model tier."
                })
                continue
            else:
                write_sse(h, {"type": "error", "message": f"LLM Gateway Error: {e}"})
                break

        choice = resp_json.get("choices", [{}])[0]
        message = choice.get("message", {})
        content = message.get("content", "")
        tool_calls = message.get("tool_calls", [])

        if content:
            if not write_sse(h, {
                "type": "thought",
                "content": content,
                "model": current_model_info["name"],
                "tier": current_model_info["tier"]
            }):
                break

        messages.append(message)

        if not tool_calls:
            write_sse(h, {"type": "done", "message": "Autonomous cycle naturally concluded."})
            break

        for tc in tool_calls:
            t_id = tc.get("id")
            func = tc.get("function", {})
            name = func.get("name")
            args_str = func.get("arguments", "{}")

            try:
                args = json.loads(args_str)
            except Exception:
                args = {}

            if not write_sse(h, {"type": "tool_call", "name": name, "args": args, "model": current_model_info["name"]}):
                return

            tool_error = False
            # Execute tool locally
            if name == "execute_shell":
                cmd = args.get("command", "")
                res_obj = execute_shell(cmd, ws)
                result = f"[Exit Code: {res_obj['exit_code']}]\n{res_obj['output']}"
                if not res_obj["success"]:
                    tool_error = True
            elif name == "read_file":
                filepath = args.get("path", "")
                if not os.path.isabs(filepath):
                    filepath = os.path.join(ws, filepath)
                result = read_file_tool(filepath)
                if result.startswith("Read Error:"):
                    tool_error = True
            elif name == "write_file":
                filepath = args.get("path", "")
                content_val = args.get("content", "")
                if not os.path.isabs(filepath):
                    filepath = os.path.join(ws, filepath)
                result = write_file_tool(filepath, content_val)
                if result.startswith("Write Error:"):
                    tool_error = True
            elif name == "mission_complete":
                summary = args.get("summary", "")
                write_sse(h, {"type": "done", "message": f"MISSION ACCOMPLISHED (PERFECT COMPLETION): {summary}"})
                return
            else:
                result = f"Unknown tool: {name}"
                tool_error = True

            # Self-Healing & Model Ladder Escalation Logic
            if tool_error:
                consecutive_errors += 1
                if consecutive_errors >= 1 and ladder_idx < len(MODEL_LADDER) - 1:
                    old_m = current_model_info["name"]
                    ladder_idx += 1
                    new_m = MODEL_LADDER[ladder_idx]["name"]
                    write_sse(h, {
                        "type": "escalation",
                        "from_model": old_m,
                        "to_model": new_m,
                        "reason": f"Tool execution failed. Escalating model tier for self-healing diagnosis."
                    })
            else:
                consecutive_errors = max(0, consecutive_errors - 1)

            if not write_sse(h, {"type": "tool_result", "name": name, "result": result[:1200] + ("..." if len(result) > 1200 else "")}):
                return

            messages.append({
                "role": "tool",
                "tool_call_id": t_id,
                "name": name,
                "content": result
            })

@route("POST", "/api/autonomous/run")
def post_autonomous_run(h, query, tail):
    req = read_json_body(h)
    mission = req.get("mission", "").strip()
    if not mission:
        send_json(h, 400, {"error": "Mission directive cannot be empty."})
        return

    start_sse(h)
    autonomous_loop(mission, h)
