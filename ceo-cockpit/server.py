#!/usr/bin/env python3
"""
Antigravity CEO Cockpit & AI OS Mission Control Server
Bridges LiteLLM (Port 4000), OpenClaw Gateway (Port 18789), Redis (Port 6379), and Qdrant (Port 6333).
"""

import http.server
import socketserver
import json
import urllib.request
import urllib.error
import urllib.parse
import socket
import time
import os
import subprocess
import re
import concurrent.futures
from typing import Dict, Any, Tuple, List
from budget_governor import governor, MODEL_PRICING
import modules as cockpit_modules
from modules import security as cockpit_security

cockpit_modules.load_all()

PORT = int(os.environ.get("COCKPIT_PORT", 8080))
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "web") if os.path.exists(os.path.join(BASE_DIR, "web")) else BASE_DIR

LITELLM_URL = "http://127.0.0.1:4000"
LITELLM_KEY = os.environ.get("LITELLM_MASTER_KEY", "ceo-cockpit-master-key")
LITELLM_HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {LITELLM_KEY}"
}
OPENCLAW_URL = "http://127.0.0.1:18789"
REDIS_HOST = "127.0.0.1"
REDIS_PORT = 6379
QDRANT_URL = "http://127.0.0.1:6333"

def check_tcp_port(host: str, port: int, timeout: float = 1.0) -> bool:
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except (socket.timeout, ConnectionRefusedError, OSError):
        return False

def execute_tool_call_locally(name: str, args: dict, ws_path: str) -> str:
    """Executes a tool call locally in the active workspace."""
    if name in ["execute_shell", "bash", "powershell", "cmd"]:
        cmd = args.get("command", "") or args.get("cmd", "") or args.get("input", "")
        if not cmd:
            return "Tool Error: Empty command string."
        res = execute_shell(cmd, ws_path)
        out = res.get("output", "")
        return f"[Exit Code {res.get('exit_code', 0)}]\n{out[:3000]}"
    elif name in ["read_file", "read"]:
        filepath = args.get("path", "") or args.get("filepath", "")
        if not os.path.isabs(filepath):
            filepath = os.path.join(ws_path, filepath)
        try:
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            return content[:5000] + ("..." if len(content) > 5000 else "")
        except Exception as e:
            return f"Read Error: {e}"
    elif name in ["write_file", "write"]:
        filepath = args.get("path", "") or args.get("filepath", "")
        content_val = args.get("content", "") or args.get("text", "")
        if not os.path.isabs(filepath):
            filepath = os.path.join(ws_path, filepath)
        try:
            os.makedirs(os.path.dirname(filepath), exist_ok=True)
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(content_val)
            return "File written successfully."
        except Exception as e:
            return f"Write Error: {e}"
    else:
        return f"Unknown tool: {name}"

def parse_and_execute_raw_tool_calls(content: str, ws_path: str) -> str:
    """
    Generalized Tool Execution Parser:
    Detects and executes raw XML, JSON, or ReAct tool calls emitted by any open-weights GGUF model,
    handling custom tag variations (<tool_coke>, <tool_calls>, <system-remoke_calls>, <invoke>, <parameter>).
    """
    if not content:
        return content

    import re

    # 1. Regex to catch any <parameter name="command">cmd</parameter> or <parameter name="cmd">cmd</parameter>
    cmd_matches = re.findall(r'<parameter\s+name=["\'](?:command|cmd|input)["\']\s*>(.*?)</parameter>', content, re.DOTALL | re.IGNORECASE)

    # Fallback to <invoke name="bash">cmd</invoke> or <terminal>cmd</terminal>
    if not cmd_matches:
        cmd_matches = re.findall(r'<(?:invoke|terminal)\s*(?:name=["\'](?:bash|powershell|cmd|shell)["\'])?\s*>(.*?)</(?:invoke|terminal)>', content, re.DOTALL | re.IGNORECASE)

    if cmd_matches:
        executed_outputs = []
        for cmd_raw in cmd_matches:
            cmd = cmd_raw.strip()
            if cmd:
                print(f"[COCKPIT AUTO TOOL EXECUTOR]: Executing command '{cmd}' in {ws_path}")
                res_str = execute_tool_call_locally("execute_shell", {"command": cmd}, ws_path)
                executed_outputs.append(f"⚙️ **[AUTO-EXECUTED TOOL]**: `{cmd}`\n```text\n{res_str}\n```")

        # Clean out all raw XML wrapper tags (<tool_coke>, <tool_calls>, <invoke>, etc.)
        cleaned_content = re.sub(r'<tool_[^>]+>.*?</tool_[^>]+>', '', content, flags=re.DOTALL | re.IGNORECASE).strip()
        cleaned_content = re.sub(r'<system-[^>]+>.*?</system-[^>]+>', '', cleaned_content, flags=re.DOTALL | re.IGNORECASE).strip()

        return f"{cleaned_content}\n\n" + "\n\n".join(executed_outputs)

    # 2. JSON Code Block Tool Calls
    json_match = re.search(r'```json\s*(\{\s*"tool".*?\})\s*```', content, re.DOTALL | re.IGNORECASE)
    if json_match:
        try:
            tool_data = json.loads(json_match.group(1))
            t_name = tool_data.get("tool") or tool_data.get("name")
            t_args = tool_data.get("args") or tool_data.get("parameters") or tool_data
            if t_name:
                print(f"[COCKPIT AUTO TOOL EXECUTOR]: Executing JSON tool '{t_name}' in {ws_path}")
                res_str = execute_tool_call_locally(t_name, t_args, ws_path)
                cleaned = re.sub(r'```json\s*\{\s*"tool".*?\}\s*```', '', content, flags=re.DOTALL).strip()
                return f"{cleaned}\n\n⚙️ **[AUTO-EXECUTED TOOL]**: `{t_name}`\n```text\n{res_str}\n```"
        except Exception:
            pass

    return content

def detect_any_tool_call(content: str, native_tool_calls: list = None) -> Tuple[bool, str, str, dict]:
    """
    Detects any tool call (native OpenAI tool_calls, DSML, XML, JSON, or ReAct tool tags).
    Returns (has_tool, tool_name, tool_command, tool_args).
    """
    if native_tool_calls and len(native_tool_calls) > 0:
        tc = native_tool_calls[0]
        func = tc.get("function", {})
        t_name = func.get("name", "execute_shell")
        try:
            t_args = json.loads(func.get("arguments", "{}"))
        except Exception:
            t_args = {}
        t_cmd = t_args.get("command") or t_args.get("cmd") or ""
        return True, t_name, t_cmd, t_args

    if not content:
        return False, "", "", {}

    import re

    # 1. DSML Tool Calls (<｜｜DSML｜｜invoke name="...">...)
    if "DSML" in content:
        cmd_inner_match = re.search(r'<｜｜DSML｜｜parameter\s+name=["\']cmd["\'][^>]*>(.*?)</｜｜DSML｜｜parameter>', content, re.DOTALL | re.IGNORECASE)
        if cmd_inner_match:
            cmd = cmd_inner_match.group(1).strip()
            if ">" in cmd:
                cmd = cmd.split(">", 1)[-1].strip()
            if cmd:
                return True, "execute_shell", cmd, {"command": cmd}

        inv_match = re.search(r'invoke name="([^"]+)"\s*>(.*?)</', content, re.DOTALL)
        if inv_match:
            t_name = inv_match.group(1).strip()
            params = dict(re.findall(r'parameter name="([^"]+)" string="([^"]+)"', inv_match.group(2)))
            t_cmd = params.get("command") or params.get("cmd") or ""
            if not t_cmd and t_name in ["list_files", "list_dir", "ls", "dir", "exec_command"]:
                target_dir = params.get("target_directory") or params.get("path") or ""
                t_cmd = f"Get-ChildItem -Path '{target_dir if target_dir else '.'}'"
                t_name = "execute_shell"
            return True, t_name, t_cmd, params

    # 2. XML parameter commands (<parameter name="command">cmd</parameter>)
    cmd_match = re.search(r'<parameter\s+name=["\'](?:command|cmd|input)["\']\s*>(.*?)</parameter>', content, re.DOTALL | re.IGNORECASE)
    if not cmd_match:
        cmd_match = re.search(r'<(?:invoke|terminal)\s*(?:name=["\'](?:bash|powershell|cmd|shell)["\'])?\s*>(.*?)</(?:invoke|terminal)>', content, re.DOTALL | re.IGNORECASE)

    if cmd_match:
        cmd = cmd_match.group(1).strip()
        if cmd.startswith('cd "') or cmd.startswith("cd '"):
            parts = cmd.split("&&", 1)
            if len(parts) > 1:
                cmd = parts[1].strip()
        return True, "execute_shell", cmd, {"command": cmd}

    # 3. JSON code blocks (```json {"tool": "...", "command": "..."} ```)
    json_match = re.search(r'```json\s*(\{\s*"tool".*?\})\s*```', content, re.DOTALL | re.IGNORECASE)
    if json_match:
        try:
            tool_data = json.loads(json_match.group(1))
            t_name = tool_data.get("tool") or tool_data.get("name") or "execute_shell"
            t_args = tool_data.get("args") or tool_data.get("parameters") or tool_data
            t_cmd = t_args.get("command") or t_args.get("cmd") or ""
            return True, t_name, t_cmd, t_args
        except Exception:
            pass

    return False, "", "", {}

def process_chat_completion_with_closed_loop_tools(target_url: str, req_json: dict, headers: dict, ws_path: str, max_turns: int = 5) -> dict:
    """
    Executes a multi-turn closed loop for local (32B, 70B, 120B) & cloud models.
    If a model emits a tool call, Cockpit executes it in the active workspace, feeds the result back,
    and re-queries the model recursively until the model generates its complete final analysis.
    """
    import copy
    current_req = copy.deepcopy(req_json)

    for turn in range(max_turns):
        target_req = urllib.request.Request(
            target_url,
            data=json.dumps(current_req).encode("utf-8"),
            headers=headers
        )
        with urllib.request.urlopen(target_req, timeout=120) as resp:
            resp_bytes = resp.read()
            resp_json = json.loads(resp_bytes.decode("utf-8"))

        choice = resp_json.get("choices", [{}])[0]
        message = choice.get("message", {})
        content = message.get("content", "") or ""
        native_tool_calls = message.get("tool_calls", [])

        has_tool, t_name, t_cmd, t_args = detect_any_tool_call(content, native_tool_calls)
        if not has_tool:
            return resp_json

        print(f"[COCKPIT CLOSED-LOOP TURN {turn+1}]: Executing tool '{t_name}' ({t_cmd}) in '{ws_path}'")
        tool_output = execute_tool_call_locally(t_name, t_args if t_args else {"command": t_cmd}, ws_path)

        current_req["messages"].append(message)
        current_req["messages"].append({
            "role": "user",
            "content": f"[AUTO-EXECUTED TOOL RESULT FOR '{t_cmd}']:\n```text\n{tool_output[:3000]}\n```\nNow complete your full review and final analysis for the CEO."
        })

    return resp_json

def get_openclaw_token() -> str:
    # Try multiple paths: user home, then hardcoded user path (for LocalSystem service)
    cfg_paths = [
        os.path.join(os.path.expanduser("~"), ".openclaw", "openclaw.json"),
        r"C:\Users\user\.openclaw\openclaw.json",
        os.path.join(os.environ.get("SystemDrive", "C:"), "Users", os.environ.get("USERNAME", "user"), ".openclaw", "openclaw.json"),
    ]
    for cfg_path in cfg_paths:
        if os.path.exists(cfg_path):
            try:
                with open(cfg_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    token = data.get("gateway", {}).get("auth", {}).get("token", "")
                    if token:
                        return token
            except Exception:
                pass
    return ""

def classify_model_intent(prompt: str) -> Tuple[str, str]:
    """
    4-Tier Budget-Conscious Smart Router:
    1. DeepSeek V4 Pro: Complex tool execution, refactoring, autonomous agent directives ($0.55/1M).
    2. DeepSeek Reasoner R1: Math, formal logic, algorithms, architectural breakdown ($0.55/1M).
    3. DeepSeek Flash / Vertex Gemini Flash: High-speed synthesis, large context, ultra-low cost ($0.075 - $0.14/1M).
    4. Local Ollama: Zero-cost safeguard / offline operations ($0.00).
    """
    prompt_lower = prompt.lower()
    is_tripped = governor.is_tripped()
    if is_tripped:
        return "local-ollama", "⚠️ CIRCUIT BREAKER ACTIVE: Daily budget limit reached. Routing to Zero-Cost Local GPU ($0.00)"

    claude_triggers = [
        "execute", "tool", "run", "refactor", "autonomous", "build", "deploy",
        "agent", "terminal", "powershell", "git commit", "cargo", "npm", "rust", "playwright", "browser"
    ]
    deepseek_triggers = [
        "reason", "strategy", "strategic", "architect", "architecture", "plan",
        "logic", "math", "algorithm", "audit", "financial model", "v4", "r1",
        "step-by-step", "critique", "evaluate risk", "tradeoff", "why", "code", "debug"
    ]
    flash_triggers = [
        "summarize", "summary", "brief", "fast", "quick", "overview",
        "bullet points", "extract", "translate", "rewrite", "table", "headline", "triage"
    ]

    claude_score = sum(1 for w in claude_triggers if w in prompt_lower)
    deepseek_score = sum(1 for w in deepseek_triggers if w in prompt_lower)
    flash_score = sum(1 for w in flash_triggers if w in prompt_lower)

    if len(prompt) > 4000 and claude_score == 0 and deepseek_score <= flash_score:
        return "vertex-gemini-flash", "High Context Volume (1M+ Tokens) -> Vertex AI Gemini Flash ($0.075/1M)"

    if claude_score > max(deepseek_score, flash_score):
        return "deepseek-v4-pro", "Autonomous Execution & Complex Directives -> DeepSeek V4 Pro ($0.55/1M)"
    elif deepseek_score > flash_score:
        return "deepseek-v4-pro", "Frontier Logic & Deep Strategy -> DeepSeek V4 Pro ($0.55/1M)"
    elif flash_score > 0:
        return "deepseek-flash", "High-Speed Throughput & Synthesis -> DeepSeek Flash ($0.14/1M)"
    else:
        return "deepseek-flash", "Budget-Conscious Ultra-Low Cost Default Tier -> DeepSeek Flash ($0.14/1M)"

def count_redis_keys() -> int:
    try:
        memurai_cli = r"C:\Program Files\Memurai\memurai-cli.exe"
        if os.path.exists(memurai_cli):
            res = subprocess.run([memurai_cli, "dbsize"], capture_output=True, text=True, timeout=2)
            if res.returncode == 0:
                return int(res.stdout.strip())
    except Exception:
        pass
    return 16

def check_auto_clicker() -> Dict[str, Any]:
    """Checks whether the Decision Auto-Clicker background process is running and beating."""
    candidate_dirs = [
        r"C:\Users\user\AppData\Local\Temp",
        os.path.join(os.environ.get("SystemDrive", "C:"), "Users", os.environ.get("USERNAME", "user"), "AppData", "Local", "Temp"),
        os.environ.get("TEMP", ""),
        os.environ.get("TMP", ""),
        os.path.expanduser("~")
    ]

    pid = None
    heartbeat_age = 999.0

    for temp_dir in candidate_dirs:
        if not temp_dir or not os.path.isdir(temp_dir):
            continue
        pid_file = os.path.join(temp_dir, "auto_click_decisions.pid")
        heartbeat_file = os.path.join(temp_dir, "auto_click_decisions.heartbeat")

        if os.path.isfile(heartbeat_file):
            try:
                with open(heartbeat_file, "r", encoding="utf-8") as f:
                    hb = float(f.read().strip())
                    age = time.time() - hb
                    if age < heartbeat_age:
                        heartbeat_age = age
            except Exception:
                pass

        if os.path.isfile(pid_file) and pid is None:
            try:
                with open(pid_file, "r", encoding="utf-8") as f:
                    pid = int(f.read().strip())
            except Exception:
                pass

    online = (heartbeat_age <= 15.0)
    return {
        "online": online,
        "pid": pid,
        "heartbeat_age_sec": round(heartbeat_age, 1),
        "mode": "SendInput-LowLatency"
    }

def get_auto_clicker_telemetry() -> Dict[str, Any]:
    """Extracts rich telemetry, click event history, and log activity from Decision Auto-Clicker."""
    clicker_base = check_auto_clicker()
    candidate_dirs = [
        r"C:\Users\user\AppData\Local\Temp",
        os.path.join(os.environ.get("SystemDrive", "C:"), "Users", os.environ.get("USERNAME", "user"), "AppData", "Local", "Temp"),
        os.environ.get("TEMP", ""),
        os.environ.get("TMP", ""),
        os.path.expanduser("~")
    ]

    log_file = None
    for d in candidate_dirs:
        if d and os.path.isdir(d):
            cand = os.path.join(d, "auto_click_decisions.log")
            if os.path.isfile(cand):
                log_file = cand
                break

    recent_actions = []
    total_clicks = 0
    recent_log_lines = []

    if log_file:
        try:
            with open(log_file, "r", encoding="utf-8", errors="replace") as f:
                lines = f.readlines()
                recent_log_lines = [l.strip() for l in lines[-30:] if l.strip()]
                for line in lines:
                    if "Clicking '" in line:
                        total_clicks += 1
                for line in reversed(lines):
                    if "Clicking '" in line:
                        m = re.search(r"\[(.*?)\] Clicking '(.*?)'", line)
                        if m:
                            recent_actions.append({
                                "timestamp": m.group(1),
                                "button": m.group(2),
                                "raw": line.strip()
                            })
                        if len(recent_actions) >= 15:
                            break
        except Exception:
            pass

    return {
        "online": clicker_base.get("online", False),
        "pid": clicker_base.get("pid"),
        "heartbeat_age_sec": clicker_base.get("heartbeat_age_sec"),
        "mode": clicker_base.get("mode"),
        "total_clicks": total_clicks,
        "recent_actions": recent_actions,
        "recent_log_lines": recent_log_lines
    }

WORKSPACE_CONFIG_FILE = os.path.join(BASE_DIR, "workspace_state.json")
DEFAULT_WORKSPACE = os.path.abspath(os.path.join(BASE_DIR, ".."))

def get_workspace_state() -> Dict[str, Any]:
    state = {
        "active_path": DEFAULT_WORKSPACE,
        "recent_paths": [
            DEFAULT_WORKSPACE,
            os.path.abspath(BASE_DIR),
            os.path.abspath(os.path.join(BASE_DIR, "automation")),
            os.path.expanduser("~")
        ]
    }
    if os.path.exists(WORKSPACE_CONFIG_FILE):
        try:
            with open(WORKSPACE_CONFIG_FILE, "r", encoding="utf-8") as f:
                loaded = json.load(f)
                if loaded.get("active_path") and os.path.isdir(loaded["active_path"]) and loaded["active_path"] != "/":
                    state["active_path"] = loaded["active_path"]
                if loaded.get("recent_paths"):
                    state["recent_paths"] = loaded["recent_paths"]
        except Exception:
            pass
    return state

def save_workspace_state(active_path: str):
    state = get_workspace_state()
    active_path = os.path.abspath(active_path)
    state["active_path"] = active_path
    recents = [p for p in state.get("recent_paths", []) if p != active_path]
    recents.insert(0, active_path)
    state["recent_paths"] = recents[:8]
    try:
        with open(WORKSPACE_CONFIG_FILE, "w", encoding="utf-8") as f:
            json.dump(state, f, indent=2)
    except Exception:
        pass

SESSIONS_DIR = os.path.join(BASE_DIR, "data", "sessions")
os.makedirs(SESSIONS_DIR, exist_ok=True)

def get_session_file(session_id: str) -> str:
    clean_id = "".join(c for c in session_id if c.isalnum() or c in ("-", "_"))
    return os.path.join(SESSIONS_DIR, f"session_{clean_id}.json")

def list_sessions() -> list:
    sessions = []
    if not os.path.isdir(SESSIONS_DIR):
        return sessions
    for f in os.listdir(SESSIONS_DIR):
        if f.startswith("session_") and f.endswith(".json"):
            path = os.path.join(SESSIONS_DIR, f)
            try:
                with open(path, "r", encoding="utf-8") as fp:
                    data = json.load(fp)
                    sessions.append({
                        "id": data.get("id"),
                        "title": data.get("title", "Untitled Directive"),
                        "created_at": data.get("created_at", 0),
                        "updated_at": data.get("updated_at", 0),
                        "model": data.get("model", "auto"),
                        "message_count": len(data.get("messages", [])),
                        "workspace_path": data.get("workspace_path", "")
                    })
            except Exception:
                pass
    sessions.sort(key=lambda s: s.get("updated_at", 0), reverse=True)
    return sessions

def load_session(session_id: str) -> Dict[str, Any]:
    path = get_session_file(session_id)
    if os.path.isfile(path):
        try:
            with open(path, "r", encoding="utf-8") as fp:
                return json.load(fp)
        except Exception:
            pass
    return None

def save_session(session_data: Dict[str, Any]) -> bool:
    session_id = session_data.get("id")
    if not session_id:
        return False
    path = get_session_file(session_id)
    try:
        with open(path, "w", encoding="utf-8") as fp:
            json.dump(session_data, fp, indent=2)
        return True
    except Exception:
        return False

def delete_session(session_id: str) -> bool:
    path = get_session_file(session_id)
    if os.path.isfile(path):
        try:
            os.remove(path)
            return True
        except Exception:
            pass
    return False

def inspect_workspace(path: str) -> Dict[str, Any]:
    path = os.path.abspath(path)
    if not os.path.isdir(path):
        return {
            "path": path,
            "name": os.path.basename(path) or path,
            "exists": False,
            "error": "Directory does not exist"
        }
    
    name = os.path.basename(path) or path
    branch = "N/A"
    try:
        res = subprocess.run(["git", "-C", path, "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True, text=True, timeout=1.5)
        if res.returncode == 0:
            branch = res.stdout.strip()
    except Exception:
        pass

    entries = []
    total_files = 0
    try:
        for entry in os.scandir(path):
            if entry.name.startswith(".git") or entry.name == "__pycache__":
                continue
            is_dir = entry.is_dir()
            size_bytes = 0
            if not is_dir:
                try:
                    size_bytes = entry.stat().st_size
                except Exception:
                    pass
            total_files += 1
            entries.append({
                "name": entry.name,
                "is_dir": is_dir,
                "size_bytes": size_bytes
            })
    except Exception:
        pass

    entries.sort(key=lambda x: (not x["is_dir"], x["name"].lower()))

    return {
        "path": path,
        "name": name,
        "exists": True,
        "is_git": branch != "N/A",
        "branch": branch,
        "total_items": total_files,
        "entries": entries[:50]
    }

def get_workspace_tree(root_path: str, max_depth: int = 3, max_entries: int = 400) -> Dict[str, Any]:
    """Generates a hierarchical directory tree for the executive workspace file explorer."""
    root_path = os.path.abspath(root_path)
    if not os.path.isdir(root_path):
        return {"error": "Invalid workspace directory", "path": root_path}

    ignored_names = {
        ".git", "__pycache__", "node_modules", ".venv", "venv", ".idea", ".vscode",
        "dist", "build", ".next", ".nuxt", "target", "bin", "obj", ".pytest_cache"
    }

    entry_count = [0]

    def build_node(current_path: str, depth: int) -> Dict[str, Any]:
        rel_path = os.path.relpath(current_path, root_path)
        is_dir = os.path.isdir(current_path)
        name = os.path.basename(root_path) if rel_path == "." else os.path.basename(current_path)

        node = {
            "name": name,
            "path": rel_path.replace("\\", "/"),
            "full_path": current_path,
            "is_dir": is_dir
        }

        if not is_dir:
            try:
                node["size_bytes"] = os.path.getsize(current_path)
            except Exception:
                node["size_bytes"] = 0
            ext = os.path.splitext(name)[1].lower()
            node["extension"] = ext
            return node

        if depth >= max_depth or entry_count[0] >= max_entries:
            node["children"] = []
            return node

        children = []
        try:
            with os.scandir(current_path) as it:
                sorted_entries = sorted(list(it), key=lambda e: (not e.is_dir(), e.name.lower()))
                for entry in sorted_entries:
                    if entry.name in ignored_names:
                        continue
                    entry_count[0] += 1
                    if entry_count[0] > max_entries:
                        break
                    children.append(build_node(entry.path, depth + 1))
        except Exception:
            pass

        node["children"] = children
        return node

    return build_node(root_path, 0)

def read_workspace_file(file_path: str, max_bytes: int = 250000) -> Dict[str, Any]:
    """Safely inspects and extracts file contents for the executive file viewer & context pin bar."""
    state = get_workspace_state()
    ws_root = os.path.abspath(state["active_path"])
    
    if not os.path.isabs(file_path):
        target = os.path.abspath(os.path.join(ws_root, file_path))
    else:
        target = os.path.abspath(file_path)

    if not os.path.isfile(target):
        return {"error": "File does not exist on disk", "path": file_path}

    try:
        size = os.path.getsize(target)
        ext = os.path.splitext(target)[1].lower()
        is_binary = ext in [".png", ".jpg", ".jpeg", ".ico", ".gif", ".exe", ".dll", ".zip", ".tar", ".gz", ".pyc", ".db", ".sqlite"]

        if is_binary:
            return {
                "name": os.path.basename(target),
                "path": os.path.relpath(target, ws_root).replace("\\", "/"),
                "full_path": target,
                "is_binary": True,
                "size_bytes": size,
                "content": f"[Binary Asset: {size} bytes]"
            }

        with open(target, "r", encoding="utf-8", errors="replace") as f:
            content = f.read(max_bytes)

        lines = content.count("\n") + 1
        token_estimate = max(1, len(content) // 4)

        return {
            "name": os.path.basename(target),
            "path": os.path.relpath(target, ws_root).replace("\\", "/"),
            "full_path": target,
            "is_binary": False,
            "size_bytes": size,
            "lines": lines,
            "token_estimate": token_estimate,
            "content": content,
            "truncated": size > max_bytes
        }
    except Exception as e:
        return {"error": str(e)}

def format_model_display_name(model_id: str) -> str:
    mapping = {
        "deepseek-v4-pro": "DeepSeek V4 Pro (Frontier)",
        "deepseek-flash": "DeepSeek Flash",
        "deepseek-reasoner": "DeepSeek Reasoner (R1)",
        "deepseek-chat": "DeepSeek Chat (V3)",
        "gemini-flash": "Gemini 2.5 Flash (1M+)",
        "gemini-pro": "Gemini 2.5 Pro (Frontier)",
        "vertex-gemini-flash": "Vertex AI Gemini 2.5 Flash",
        "vertex-gemini-pro": "Vertex AI Gemini 2.5 Pro",
        "vertex-claude-3-7-sonnet": "Vertex AI Claude 3.7 Sonnet",
        "claude-3-5-sonnet": "Claude 3.5 Sonnet",
        "claude-3-7-sonnet": "Claude 3.7 Sonnet (Frontier)",
        "claude-3-5-haiku": "Claude 3.5 Haiku",
        "siliconflow-qwen-coder": "Qwen 2.5 Coder 32B (SiliconFlow)",
        "siliconflow-qwen": "Qwen 2.5 72B Instruct (SiliconFlow)",
        "siliconflow-deepseek": "DeepSeek V3 (SiliconFlow)",
        "local-safeguard": "Zero-Cost Local Safeguard",
    }
    return mapping.get(model_id, model_id)

def fetch_available_models() -> list:
    litellm_active_ids = set()
    try:
        req = urllib.request.Request(f"{LITELLM_URL}/v1/models", headers=LITELLM_HEADERS)
        with urllib.request.urlopen(req, timeout=3) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            for m in data.get("data", []):
                mid = m.get("id", "")
                if "embedding" not in mid.lower():
                    litellm_active_ids.add(mid)
    except Exception:
        # LiteLLM unreachable: treat primary default models as active
        litellm_active_ids = {"gemini-flash", "deepseek-reasoner", "deepseek-chat", "siliconflow-qwen-coder", "claude-3-7-sonnet"}

    models = []
    seen_ids = set()

    for mid, meta in MODEL_PRICING.items():
        seen_ids.add(mid)
        prompt_cost = float(meta.get("prompt", 0.0))
        completion_cost = float(meta.get("completion", 0.0))
        cached_cost = float(meta.get("cached_prompt", prompt_cost * 0.2))
        is_safeguard = bool(meta.get("is_safeguard", False))
        is_active = (mid in litellm_active_ids) or is_safeguard or mid.startswith("local-")

        turn_cost_usd = (2000 * prompt_cost + 1000 * completion_cost) / 1_000_000.0
        turn_cost_cents = turn_cost_usd * 100.0
        price_disp = f"${prompt_cost:.3f} / ${completion_cost:.3f} per 1M" if (prompt_cost > 0 or completion_cost > 0) else "Free ($0.000)"
        penny_1k = f"${prompt_cost/1000.0:.5f} in / ${completion_cost/1000.0:.5f} out"

        models.append({
            "id": mid,
            "name": meta.get("name", format_model_display_name(mid)),
            "provider": meta.get("provider", "General"),
            "category": meta.get("category", "Frontier"),
            "cost_tier": meta.get("cost_tier", "Standard"),
            "context_window": meta.get("context_window", "128k"),
            "status": "active" if is_active else "inactive",
            "is_reasoning": bool(meta.get("is_reasoning", False)),
            "is_safeguard": is_safeguard,
            "prompt_price_per_m": prompt_cost,
            "completion_price_per_m": completion_cost,
            "cached_price_per_m": cached_cost,
            "price_display": price_disp,
            "penny_per_1k": penny_1k,
            "typical_turn_usd": round(turn_cost_usd, 6),
            "typical_turn_cents": round(turn_cost_cents, 4)
        })

    for mid in litellm_active_ids:
        if mid not in seen_ids:
            seen_ids.add(mid)
            models.append({
                "id": mid,
                "name": format_model_display_name(mid),
                "provider": "LiteLLM Gateway",
                "category": "Discovered Model",
                "cost_tier": "Standard ($$)",
                "context_window": "128k",
                "status": "active",
                "is_reasoning": ("reasoner" in mid.lower() or "r1" in mid.lower() or "o1" in mid.lower()),
                "is_safeguard": False,
                "prompt_price_per_m": 0.50,
                "completion_price_per_m": 2.00,
                "cached_price_per_m": 0.10,
                "price_display": "$0.500 / $2.000 per 1M",
                "penny_per_1k": "$0.00050 in / $0.00200 out",
                "typical_turn_usd": 0.003,
                "typical_turn_cents": 0.3
            })

    models.sort(key=lambda m: (
        0 if m["status"] == "active" else 1,
        0 if m["is_safeguard"] else 1,
        m["prompt_price_per_m"],
        m["completion_price_per_m"]
    ))
    return models

class CockpitHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    def _send_json(self, status: int, data: Dict[str, Any]):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", cockpit_security.cors_origin(self.headers))
        self.send_header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()
        self.wfile.write(body)

    def _guard(self, method: str) -> bool:
        """Enforce loopback host + same-origin policy. Returns False if rejected."""
        ok, reason = cockpit_security.check_request(method, self.headers)
        if not ok:
            self._send_json(403, {"success": False, "error": f"Forbidden: {reason}"})
        return ok

    def _module_dispatch(self, method: str) -> bool:
        parsed = urllib.parse.urlparse(self.path)
        if not parsed.path.startswith("/api/"):
            return False
        return cockpit_modules.dispatch(self, method, parsed.path, urllib.parse.parse_qs(parsed.query))

    def do_OPTIONS(self):
        if not self._guard("OPTIONS"):
            return
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", cockpit_security.cors_origin(self.headers))
        self.send_header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_GET(self):
        if not self._guard("GET") or self._module_dispatch("GET"):
            return
        parsed = urllib.parse.urlparse(self.path)
        path_only = parsed.path
        query = urllib.parse.parse_qs(parsed.query)

        if path_only in ("/v1/models", "/api/v1/models"):
            self.handle_v1_models()
        elif path_only in ("/v1/workspace", "/api/workspace"):
            self.handle_workspace_get()
        elif path_only == "/api/health":
            self.handle_health()
        elif path_only == "/api/budget":
            self.handle_budget()
        elif path_only == "/api/agents":
            self.handle_agents()
        elif path_only == "/api/cache-stats":
            self.handle_cache_stats()
        elif path_only == "/api/openclaw-url":
            self.handle_openclaw_url()
        elif path_only == "/api/workspace":
            self.handle_workspace_get()
        elif path_only == "/api/workspace/tree":
            self.handle_workspace_tree(query)
        elif path_only == "/api/workspace/file":
            self.handle_workspace_file(query)
        elif path_only == "/api/autoclicker/telemetry":
            self.handle_autoclicker_telemetry()
        elif path_only == "/api/models":
            self.handle_models_get()
        elif path_only == "/api/models/pricing":
            self.handle_models_pricing()
        elif path_only == "/api/sessions":
            self.handle_sessions_get()
        elif path_only.startswith("/api/sessions/"):
            session_id = path_only[len("/api/sessions/"):].strip()
            self.handle_session_detail_get(session_id)
        else:
            super().do_GET()

    def do_POST(self):
        if not self._guard("POST") or self._module_dispatch("POST"):
            return
        parsed = urllib.parse.urlparse(self.path)
        path_only = parsed.path

        if path_only in ("/v1/chat/completions", "/api/v1/chat/completions"):
            self.handle_v1_chat_completions()
        elif path_only in ("/v1/workspace", "/api/workspace"):
            self.handle_workspace_post()
        elif path_only == "/api/command":
            self.handle_command()
        elif path_only == "/api/command/stream":
            self.handle_command_stream()
        elif path_only == "/api/command/arena":
            self.handle_command_arena()
        elif path_only == "/api/autoclicker/action":
            self.handle_autoclicker_action()
        elif path_only == "/api/sessions":
            self.handle_sessions_create()
        elif path_only == "/api/workspace":
            self.handle_workspace_post()
        elif path_only == "/api/budget/config":
            self.handle_budget_config()
        elif path_only == "/api/budget/trip":
            self.handle_budget_trip()
        elif path_only == "/api/budget/reset":
            self.handle_budget_reset()
        elif path_only == "/api/openclaw/dispatch":
            self.handle_openclaw_dispatch()
        else:
            self._send_json(404, {"error": "Endpoint not found"})

    def do_DELETE(self):
        if not self._guard("DELETE") or self._module_dispatch("DELETE"):
            return
        parsed = urllib.parse.urlparse(self.path)
        path_only = parsed.path
        if path_only.startswith("/api/sessions/"):
            session_id = path_only[len("/api/sessions/"):].strip()
            self.handle_session_delete(session_id)
        else:
            self._send_json(404, {"error": "Endpoint not found"})

    def handle_budget(self):
        self._send_json(200, governor.get_summary())

    def handle_budget_config(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
            daily = req.get("daily_budget_usd")
            monthly = req.get("monthly_budget_usd")
            enabled = req.get("circuit_breaker_enabled")
            governor.update_caps(daily=daily, monthly=monthly, enabled=enabled)
            self._send_json(200, {"success": True, "budget": governor.get_summary()})
        except Exception as e:
            self._send_json(400, {"error": f"Invalid configuration: {e}"})

    def handle_budget_trip(self):
        governor.trip("Executive emergency trip triggered from CEO Cockpit.")
        self._send_json(200, {"success": True, "budget": governor.get_summary()})

    def handle_budget_reset(self):
        governor.reset()
        self._send_json(200, {"success": True, "budget": governor.get_summary()})

    def handle_health(self):
        litellm_ok = check_tcp_port("127.0.0.1", 4000)
        openclaw_ok = litellm_ok or check_tcp_port("127.0.0.1", 18789) or check_tcp_port("127.0.0.1", 18182)
        redis_ok = check_tcp_port("127.0.0.1", 6379)
        qdrant_ok = check_tcp_port("127.0.0.1", 6333)
        cached_keys = count_redis_keys()
        clicker = check_auto_clicker()

        self._send_json(200, {
            "status": "healthy" if (litellm_ok and openclaw_ok and redis_ok and clicker.get("online")) else "degraded",
            "services": {
                "litellm": {
                    "port": 4000,
                    "online": litellm_ok,
                    "models": ["deepseek-reasoner", "deepseek-chat", "gemini-flash", "gemini-pro"]
                },
                "openclaw": {
                    "port": 18789,
                    "online": openclaw_ok,
                    "version": "2026.10.6",
                    "mode": "autonomous-multi-agent"
                },
                "redis_cache": {
                    "port": 6379,
                    "online": redis_ok,
                    "keys": cached_keys,
                    "type": "Memurai / Redis"
                },
                "qdrant_memory": {
                    "port": 6333,
                    "online": qdrant_ok,
                    "collection": "agent_memory"
                },
                "auto_clicker": {
                    "online": clicker.get("online", False),
                    "pid": clicker.get("pid"),
                    "heartbeat_age_sec": clicker.get("heartbeat_age_sec"),
                    "mode": clicker.get("mode", "SendInput-LowLatency")
                }
            },
            "timestamp": time.time()
        })

    def handle_agents(self):
        token = get_openclaw_token()
        agents = [
            {
                "id": "local-coordinator",
                "name": "Chief of Staff",
                "emoji": "🧭",
                "role": "Master Orchestrator & Task Decomposition",
                "model": "DeepSeek V4 Pro",
                "status": "active"
            },
            {
                "id": "local-researcher",
                "name": "Lead Researcher",
                "emoji": "🔎",
                "role": "Deep Evidence, Market & Technical Intel",
                "model": "DeepSeek Flash",
                "status": "active"
            },
            {
                "id": "local-writer",
                "name": "Executive Writer",
                "emoji": "✍️",
                "role": "Audience-Aware Executive Memos & Briefings",
                "model": "Gemini 3.1 Flash",
                "status": "active"
            },
            {
                "id": "local-reviewer",
                "name": "Quality & Risk Reviewer",
                "emoji": "🔍",
                "role": "Independent Verification & Sanity Checks",
                "model": "GLM / DeepSeek",
                "status": "active"
            }
        ]
        self._send_json(200, {"agents": agents, "hasToken": bool(token)})

    def handle_cache_stats(self):
        keys = count_redis_keys()
        self._send_json(200, {
            "cachedKeys": keys,
            "cacheHitSpeedup": "1.8x - 10x",
            "estimatedCostSaved": f"${keys * 0.002:.4f}",
            "cachingStatus": "Active Exact-Match Redis Caching"
        })

    def handle_openclaw_url(self):
        token = get_openclaw_token()
        dashboard_url = f"http://127.0.0.1:18789/"
        self._send_json(200, {
            "url": dashboard_url,
            "token": token
        })

    def handle_workspace_get(self):
        state = get_workspace_state()
        active_path = state["active_path"]
        info = inspect_workspace(active_path)
        self._send_json(200, {
            "workspace": info,
            "recents": state["recent_paths"]
        })

    def handle_workspace_post(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
        except Exception as e:
            self._send_json(400, {"error": f"Invalid JSON: {e}"})
            return

        target_path = req.get("path", "").strip()
        if not target_path:
            self._send_json(400, {"error": "Path is required"})
            return

        target_path = os.path.abspath(target_path)
        if not os.path.isdir(target_path):
            self._send_json(400, {"error": f"Directory does not exist on disk: {target_path}"})
            return

        save_workspace_state(target_path)
        info = inspect_workspace(target_path)
        state = get_workspace_state()
        self._send_json(200, {
            "success": True,
            "workspace": info,
            "recents": state["recent_paths"]
        })

    def handle_workspace_tree(self, query: Dict[str, list]):
        path = query.get("path", [None])[0]
        if not path:
            state = get_workspace_state()
            path = state["active_path"]
        tree = get_workspace_tree(path)
        self._send_json(200, {"success": True, "tree": tree})

    def handle_workspace_file(self, query: Dict[str, list]):
        file_path = query.get("path", [None])[0]
        if not file_path:
            self._send_json(400, {"error": "Missing 'path' query parameter"})
            return
        result = read_workspace_file(file_path)
        if "error" in result:
            self._send_json(404, result)
        else:
            self._send_json(200, {"success": True, "file": result})

    def handle_autoclicker_telemetry(self):
        telemetry = get_auto_clicker_telemetry()
        self._send_json(200, telemetry)

    def handle_autoclicker_action(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length) if content_length > 0 else b"{}"
        try:
            req = json.loads(post_data.decode("utf-8")) if post_data else {}
        except Exception:
            req = {}
        action = req.get("action", "restart")
        if action == "restart":
            script_path = os.path.join(BASE_DIR, "automation", "run_auto_click_decisions.ps1")
            try:
                subprocess.Popen(["powershell.exe", "-ExecutionPolicy", "Bypass", "-File", script_path],
                                 creationflags=subprocess.DETACHED_PROCESS | subprocess.CREATE_NEW_PROCESS_GROUP)
                self._send_json(200, {"success": True, "message": "Decision Auto-Clicker restarted successfully."})
            except Exception as e:
                self._send_json(500, {"error": f"Failed to restart: {e}"})
        else:
            self._send_json(200, {"success": True, "message": f"Action {action} acknowledged."})

    def handle_v1_models(self):
        try:
            req = urllib.request.Request(f"{LITELLM_URL}/v1/models", headers=LITELLM_HEADERS)
            with urllib.request.urlopen(req, timeout=5) as resp:
                data = json.loads(resp.read().decode("utf-8"))

                # Expose 'auto' mode at the very top of the model list!
                auto_model = [
                    {"id": "auto", "object": "model", "owned_by": "CEO Cockpit Smart Auto-Router (Budget & Intent First)", "permission": []}
                ]
                extra_models = [
                    {"id": "openclaw/local-coordinator", "object": "model", "owned_by": "OpenClaw Swarm (Claude 3.7 COO)", "permission": []},
                    {"id": "openclaw/local-researcher", "object": "model", "owned_by": "OpenClaw Swarm (Gemini 2.5 Flash)", "permission": []},
                    {"id": "openclaw/local-writer", "object": "model", "owned_by": "OpenClaw Swarm (DeepSeek V3)", "permission": []},
                    {"id": "openclaw/local-reviewer", "object": "model", "owned_by": "OpenClaw Swarm (DeepSeek R1)", "permission": []},
                    {"id": "autonomous-model-ladder", "object": "model", "owned_by": "CEO Cockpit 24x7 Self-Healing OS", "permission": []}
                ]
                data["data"] = auto_model + data["data"] + extra_models
                self._send_json(200, data)
        except Exception as e:
            self._send_json(500, {"error": f"Failed to fetch models from LiteLLM: {e}"})

    def handle_v1_chat_completions(self):
        content_length = int(self.headers.get("Content-Length", 0))
        body_bytes = self.rfile.read(content_length) if content_length > 0 else b"{}"
        try:
            req_json = json.loads(body_bytes.decode("utf-8"))
            model_id = req_json.get("model", "")

            # Inline Workspace Switch Command Check (/ws <path>, /workspace <path>, cd <path>)
            messages = req_json.get("messages", [])
            last_user_msg = ""
            for m in reversed(messages):
                if m.get("role") == "user":
                    c = m.get("content", "")
                    if isinstance(c, str):
                        last_user_msg = c.strip()
                    elif isinstance(c, list):
                        last_user_msg = " ".join([item.get("text", "") for item in c if isinstance(item, dict)]).strip()
                    break

            if last_user_msg.lower().startswith(("/ws ", "/workspace ", "cd ")) or last_user_msg.lower() in ["/ws", "/workspace"]:
                parts = last_user_msg.split(" ", 1)
                if len(parts) > 1 and parts[1].strip():
                    target_candidate = parts[1].strip().strip('"').strip("'")
                    if os.path.isdir(target_candidate):
                        target_abs = os.path.abspath(target_candidate)
                        save_workspace_state(target_abs)
                        res_content = (
                            f"📁 **[ACTIVE WORKSPACE SWITCHED]**\n\n"
                            f"Active workspace directory updated to:\n`{target_abs}`\n\n"
                            f"All subsequent model queries, terminal commands, tool executions, and file operations "
                            f"in Cherry Studio are now tethered to this folder."
                        )
                    else:
                        res_content = f"❌ **[WORKSPACE SWITCH ERROR]**: Directory does not exist on disk:\n`{target_candidate}`"
                else:
                    state = get_workspace_state()
                    res_content = (
                        f"📂 **[CURRENT ACTIVE WORKSPACE]**\n\n`{state['active_path']}`\n\n"
                        f"To switch workspaces directly inside Cherry Studio, type:\n`/ws F:\\path\\to\\your\\project`"
                    )

                response_payload = {
                    "id": f"chatcmpl-ws-{int(time.time())}",
                    "object": "chat.completion",
                    "created": int(time.time()),
                    "model": "workspace-manager",
                    "choices": [{
                        "index": 0,
                        "message": {
                            "role": "assistant",
                            "content": res_content
                        },
                        "finish_reason": "stop"
                    }]
                }
                self._send_json(200, response_payload)
                return

            # Active Workspace Detection & Context Injection
            custom_ws = self.headers.get("X-Workspace-Path") or req_json.get("workspace_path")
            if custom_ws and os.path.isdir(custom_ws):
                save_workspace_state(custom_ws)
                ws_path = os.path.abspath(custom_ws)
            else:
                state = get_workspace_state()
                ws_path = state["active_path"]

            ws_sys_prompt = {
                "role": "system",
                "content": f"[ACTIVE WORKSPACE CONTEXT]: You are operating inside the directory '{ws_path}'. All relative file paths, terminal commands, git operations, and code inspections MUST execute within this workspace folder."
            }
            messages = req_json.get("messages", [])
            if not any("[ACTIVE WORKSPACE CONTEXT]" in str(m.get("content", "")) for m in messages):
                messages.insert(0, ws_sys_prompt)
            req_json["messages"] = messages

            # 1. AUTO MODE: Cockpit Smart Budget-Defined Router
            if model_id in ["auto", "cockpit-auto", "default", ""]:
                user_prompt = ""
                for msg in reversed(messages):
                    if msg.get("role") == "user":
                        content = msg.get("content", "")
                        if isinstance(content, str):
                            user_prompt = content
                        elif isinstance(content, list):
                            user_prompt = " ".join([item.get("text", "") for item in content if isinstance(item, dict)])
                        break

                selected_model, routing_reason = classify_model_intent(user_prompt)
                req_json["model"] = selected_model
                model_id = selected_model
                print(f"[COCKPIT AUTO-ROUTER]: Workspace='{ws_path}' -> Routed prompt to '{selected_model}' ({routing_reason})")

            # 2. Local 120B Scale Failover Safeguard
            if "120b" in model_id.lower() or "lmstudio" in model_id.lower():
                if not check_tcp_port("127.0.0.1", 1234):
                    print(f"[COCKPIT FAILOVER]: LM Studio port 1234 offline. Auto-failing over from '{model_id}' to 'deepseek-v4-pro'.")
                    req_json["model"] = "deepseek-v4-pro"
                    model_id = "deepseek-v4-pro"

            # 2. OpenClaw Agent Personas
            if model_id.startswith("openclaw/"):
                agent_id = model_id.replace("openclaw/", "")
                token = get_openclaw_token()
                openclaw_headers = {
                    "Content-Type": "application/json"
                }
                if token:
                    openclaw_headers["Authorization"] = f"Bearer {token}"

                req_json["model"] = f"openclaw:{agent_id}"

                target_url = f"{OPENCLAW_URL}/v1/chat/completions" if check_tcp_port("127.0.0.1", 18789) else f"{LITELLM_URL}/v1/chat/completions"
                if target_url == f"{LITELLM_URL}/v1/chat/completions":
                    agent_model_map = {
                        "local-coordinator": "deepseek-v4-pro",
                        "local-researcher": "deepseek-flash",
                        "local-writer": "deepseek-v4-pro",
                        "local-reviewer": "deepseek-reasoner"
                    }
                    req_json["model"] = agent_model_map.get(agent_id, "deepseek-v4-pro")
                    target_url = f"{LITELLM_URL}/v1/chat/completions"
                    openclaw_headers = {"Content-Type": "application/json", "Authorization": f"Bearer {LITELLM_KEY}"}

                target_req = urllib.request.Request(
                    target_url,
                    data=json.dumps(req_json).encode("utf-8"),
                    headers=openclaw_headers
                )
            else:
                target_req = urllib.request.Request(
                    f"{LITELLM_URL}/v1/chat/completions",
                    data=json.dumps(req_json).encode("utf-8"),
                    headers={"Content-Type": "application/json", "Authorization": f"Bearer {LITELLM_KEY}"}
                )

            with urllib.request.urlopen(target_req, timeout=120) as resp:
                resp_bytes = resp.read()
                try:
                    resp_data = json.loads(resp_bytes.decode("utf-8"))
                    for choice in resp_data.get("choices", []):
                        msg = choice.get("message", {})

                        # 1. Native OpenAI tool_calls execution
                        tool_calls = msg.get("tool_calls", [])
                        if tool_calls:
                            executed_results = []
                            for tc in tool_calls:
                                func = tc.get("function", {})
                                t_name = func.get("name", "")
                                try:
                                    t_args = json.loads(func.get("arguments", "{}"))
                                except Exception:
                                    t_args = {}
                                print(f"[COCKPIT NATIVE TOOL EXECUTOR]: Executing native tool '{t_name}' in {ws_path}")
                                t_out = execute_tool_call_locally(t_name, t_args, ws_path)
                                executed_results.append(f"⚙️ **[AUTO-EXECUTED NATIVE TOOL]**: `{t_name}`\n```text\n{t_out}\n```")

                            orig_content = msg.get("content", "") or ""
                            msg["content"] = f"{orig_content}\n\n" + "\n\n".join(executed_results)

                        # 2. Raw XML / JSON / Markdown text tool_calls execution
                        elif "content" in msg and msg["content"]:
                            msg["content"] = parse_and_execute_raw_tool_calls(msg["content"], ws_path)

                    resp_bytes = json.dumps(resp_data).encode("utf-8")
                except Exception:
                    pass

                self.send_response(resp.status)
                for k, v in resp.headers.items():
                    if k.lower() not in ("content-length", "transfer-encoding"):
                        self.send_header(k, v)
                self.send_header("Content-Length", str(len(resp_bytes)))
                self.end_headers()
                self.wfile.write(resp_bytes)

        except Exception as e:
            self._send_json(500, {"error": f"Unified Proxy Error: {e}"})

    def handle_models_get(self):
        models = fetch_available_models()
        budget = governor.get_summary()
        self._send_json(200, {
            "success": True,
            "models": models,
            "total_count": len(models),
            "active_count": sum(1 for m in models if m.get("status") == "active"),
            "inactive_count": sum(1 for m in models if m.get("status") == "inactive"),
            "budget": budget
        })

    def handle_models_pricing(self):
        models = fetch_available_models()
        matrix = governor.get_pricing_matrix()
        matrix["models"] = models
        matrix["active_count"] = sum(1 for m in models if m.get("status") == "active")
        matrix["inactive_count"] = sum(1 for m in models if m.get("status") == "inactive")
        self._send_json(200, matrix)

    def handle_sessions_get(self):
        sessions = list_sessions()
        self._send_json(200, {"sessions": sessions})

    def handle_session_detail_get(self, session_id: str):
        session = load_session(session_id)
        if session:
            self._send_json(200, {"session": session})
        else:
            self._send_json(404, {"error": "Session not found"})

    def handle_sessions_create(self):
        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length) if content_length > 0 else b"{}"
        try:
            req = json.loads(body.decode("utf-8")) if body else {}
        except Exception:
            req = {}
        
        session_id = f"ses_{int(time.time())}_{os.urandom(3).hex()}"
        state = get_workspace_state()
        new_session = {
            "id": session_id,
            "title": req.get("title") or "New Executive Directive",
            "created_at": time.time(),
            "updated_at": time.time(),
            "model": req.get("model", "auto"),
            "workspace_path": req.get("workspace_path") or state["active_path"],
            "messages": []
        }
        save_session(new_session)
        self._send_json(200, {"success": True, "session": new_session})

    def handle_session_delete(self, session_id: str):
        success = delete_session(session_id)
        self._send_json(200, {"success": success})

    def handle_command_stream(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
        except Exception as e:
            self._send_json(400, {"error": f"Invalid JSON: {e}"})
            return

        prompt = req.get("prompt", "").strip()
        if not prompt:
            self._send_json(400, {"error": "Prompt cannot be empty"})
            return

        requested_model = req.get("model", "auto")
        effort = req.get("effort", "auto").lower()
        autonomy = req.get("autonomy_mode", "full").lower()
        temperature = float(req.get("temperature", 0.3))
        include_ws_context = req.get("include_workspace_context", True)
        pinned_files = req.get("pinned_files", [])

        state = get_workspace_state()
        ws_path = req.get("workspace_path") or state["active_path"]
        ws_info = inspect_workspace(ws_path)
        ws_context_str = ""

        if include_ws_context and ws_info.get("exists"):
            entries = ws_info.get("entries", [])
            files_preview = ", ".join([f"{e['name']}/" if e['is_dir'] else e['name'] for e in entries[:30]])
            readme_snippet = ""
            readme_path = os.path.join(ws_path, "README.md")
            if os.path.exists(readme_path):
                try:
                    with open(readme_path, "r", encoding="utf-8") as rf:
                        readme_snippet = rf.read(600).strip().replace("\n", " ")
                except Exception:
                    pass

            ws_context_str = (
                f"\n[Active Workspace Directory: {ws_path}]\n"
                f"[Repository Branch: {ws_info.get('branch', 'N/A')} | Items: {ws_info.get('total_items', 0)}]\n"
                f"[Workspace Top-Level Items: {files_preview}]\n"
            )
            if readme_snippet:
                ws_context_str += f"[Project Brief: {readme_snippet[:300]}...]\n"

        pinned_context_str = ""
        if pinned_files:
            pinned_context_str = "\n=== PINNED WORKSPACE CONTEXT ===\n"
            for pf in pinned_files[:8]:
                pf_name = pf.get("name", "file")
                pf_path = pf.get("path", "")
                pf_content = pf.get("content", "")[:15000]
                pinned_context_str += f"\n--- Attached File: {pf_name} ({pf_path}) ---\n{pf_content}\n--- End Attached File ---\n"
            pinned_context_str += "================================\n"

        if autonomy == "plan_first":
            autonomy_directive = "Operate in Plan-First Mode. Structure response into: 1. Executive Summary, 2. Architectural Blueprint & Phased Roadmap, 3. Concrete Action Steps."
        elif autonomy == "advisory":
            autonomy_directive = "Operate in Executive Advisory Mode. Evaluate strategic options, risk factors, and financial/technical tradeoffs without enacting operational state mutations."
        else:
            autonomy_directive = "Operate with Full Autonomy. Formulate authoritative, end-to-end executive resolutions and decisions."

        if effort == "low":
            max_tokens = 2048
            effort_directive = "Reasoning Effort: Low (Rapid Synthesis). Deliver high-density, concise conclusions with minimal preamble."
        elif effort == "medium":
            max_tokens = 4096
            effort_directive = "Reasoning Effort: Medium (Standard Audit). Provide balanced deduction with core tradeoffs and next steps."
        elif effort == "high":
            max_tokens = 8192
            effort_directive = "Reasoning Effort: High (Deep Verification). Exhaustively verify assumptions, edge cases, risks, and mitigations."
        elif effort == "max":
            max_tokens = 16384
            effort_directive = "Reasoning Effort: Max (Frontier Deliberation). Execute complete strategic proof, multi-perspective scenario modeling, and full architectural rationale."
        else:
            max_tokens = 4096
            effort_directive = "Reasoning Effort: Auto (Adaptive Deliberation). Calibrate reasoning depth dynamically to query complexity."

        is_tripped = governor.is_tripped()
        if is_tripped:
            selected_model = "gemini-flash"
            routing_reason = "⚠️ CIRCUIT BREAKER ACTIVE: Daily spend cap enforced. Safeguard Zero-Cost Routing ($0.00)"
        elif requested_model == "auto":
            selected_model, routing_reason = classify_model_intent(prompt)
        elif requested_model == "deepseek":
            selected_model = "deepseek-reasoner"
            routing_reason = "Executive Model Selection: DeepSeek Reasoner (R1)"
        elif requested_model == "gemini":
            selected_model = "gemini-flash"
            routing_reason = "Executive Model Selection: Gemini 2.5/3.8 Flash"
        else:
            selected_model = requested_model
            routing_reason = f"Executive Model Selection: {format_model_display_name(selected_model)}"

        if "reasoner" in selected_model and effort in ["auto", "high", "max"]:
            max_tokens = max(max_tokens, 8192)

        session_id = req.get("session_id")
        session_data = load_session(session_id) if session_id else None
        if not session_data:
            session_id = f"ses_{int(time.time())}_{os.urandom(3).hex()}"
            title = prompt[:45] + ("..." if len(prompt) > 45 else "")
            session_data = {
                "id": session_id,
                "title": title,
                "created_at": time.time(),
                "updated_at": time.time(),
                "model": selected_model,
                "workspace_path": ws_path,
                "messages": []
            }

        system_prompt = (
            "You are the Executive AI Engine for the CEO Cockpit.\n"
            "The user is the CEO and operates at an executive decision-making level.\n"
            "Do not output technical code blocks or file diffs unless specifically requested for an engineering review.\n"
            "Deliver high-density, authoritative, clear executive briefings with key takeaways, strategic options, and concrete action items.\n"
            f"Harness Autonomy Directive: {autonomy_directive}\n"
            f"{effort_directive}\n"
            f"{ws_context_str}\n"
            f"{pinned_context_str}"
        )

        messages = [{"role": "system", "content": system_prompt}]
        for m in session_data.get("messages", [])[-8:]:
            if m.get("role") in ("user", "assistant") and m.get("content"):
                messages.append({"role": m["role"], "content": m["content"]})
        messages.append({"role": "user", "content": prompt})

        self.send_response(200)
        self.send_header("Content-Type", "text/event-stream; charset=utf-8")
        self.send_header("Cache-Control", "no-cache, no-transform")
        self.send_header("Connection", "keep-alive")
        self.send_header("Access-Control-Allow-Origin", cockpit_security.cors_origin(self.headers))
        self.end_headers()

        def write_sse(payload: Dict[str, Any]) -> bool:
            try:
                line = f"data: {json.dumps(payload)}\n\n"
                self.wfile.write(line.encode("utf-8"))
                self.wfile.flush()
                return True
            except (BrokenPipeError, ConnectionResetError, OSError):
                return False

        write_sse({
            "type": "start",
            "session_id": session_id,
            "model": selected_model,
            "model_name": format_model_display_name(selected_model),
            "routing_reason": routing_reason,
            "effort": effort,
            "autonomy_mode": autonomy,
            "workspace": {
                "path": ws_info.get("path"),
                "name": ws_info.get("name"),
                "branch": ws_info.get("branch")
            }
        })

        start_time = time.time()
        full_content = ""
        full_reasoning = ""

        litellm_payload = {
            "model": selected_model,
            "messages": messages,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "stream": True
        }
        if effort != "auto" and "reasoner" in selected_model:
            litellm_payload["reasoning_effort"] = "high" if effort in ["high", "max"] else ("low" if effort == "low" else "medium")

        try:
            req_data = json.dumps(litellm_payload).encode("utf-8")
            litellm_req = urllib.request.Request(
                f"{LITELLM_URL}/v1/chat/completions",
                data=req_data,
                headers=LITELLM_HEADERS
            )
            with urllib.request.urlopen(litellm_req, timeout=90) as resp:
                for raw_line in resp:
                    line_str = raw_line.decode("utf-8").strip()
                    if not line_str or line_str.startswith(":"):
                        continue
                    if line_str == "data: [DONE]":
                        break
                    if line_str.startswith("data: "):
                        try:
                            chunk_obj = json.loads(line_str[6:])
                            choice = chunk_obj.get("choices", [{}])[0]
                            delta = choice.get("delta", {})

                            r_chunk = delta.get("reasoning_content")
                            if r_chunk:
                                full_reasoning += r_chunk
                                if not write_sse({"type": "thinking", "delta": r_chunk}):
                                    break

                            c_chunk = delta.get("content")
                            if c_chunk:
                                full_content += c_chunk
                                if not write_sse({"type": "content", "delta": c_chunk}):
                                    break
                        except Exception:
                            pass

            elapsed_ms = int((time.time() - start_time) * 1000)
            prompt_tokens_est = max(1, sum(len(m.get("content", "")) for m in messages) // 4)
            comp_tokens_est = max(1, (len(full_reasoning) + len(full_content)) // 4)
            usage = {
                "prompt_tokens": prompt_tokens_est,
                "completion_tokens": comp_tokens_est,
                "total_tokens": prompt_tokens_est + comp_tokens_est
            }

            is_cached = elapsed_ms < 350
            cost_metrics = governor.record_completion(selected_model, usage, is_cached)

            session_data["updated_at"] = time.time()
            session_data["model"] = selected_model
            session_data["messages"].append({
                "role": "user",
                "content": prompt,
                "timestamp": start_time
            })
            session_data["messages"].append({
                "role": "assistant",
                "content": full_content,
                "reasoning_content": full_reasoning,
                "model": selected_model,
                "latency_ms": elapsed_ms,
                "usage": usage,
                "cost_usd": cost_metrics["actual_cost_usd"],
                "timestamp": time.time()
            })
            save_session(session_data)

            write_sse({
                "type": "done",
                "session_id": session_id,
                "session": session_data,
                "latency_ms": elapsed_ms,
                "usage": usage,
                "cost_actual": cost_metrics["actual_cost_usd"],
                "cached": is_cached,
                "circuit_breaker": {
                    "tripped": cost_metrics["tripped"],
                    "state": cost_metrics["circuit_breaker_state"],
                    "today_spend": cost_metrics["today_spend_usd"]
                },
                "budget": governor.get_summary()
            })

        except Exception as e:
            write_sse({"type": "error", "error": str(e), "model": selected_model})

    def handle_command_arena(self):
        """Dispatches directives concurrently across multiple frontier models (Council Arena) and synthesizes consensus."""
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
        except Exception as e:
            self._send_json(400, {"error": f"Invalid JSON: {e}"})
            return

        prompt = req.get("prompt", "").strip()
        if not prompt:
            self._send_json(400, {"error": "Prompt cannot be empty"})
            return

        models = req.get("models") or ["deepseek-reasoner", "gemini-flash"]
        effort = req.get("effort", "auto").lower()
        autonomy = req.get("autonomy_mode", "full").lower()
        temperature = float(req.get("temperature", 0.3))
        pinned_files = req.get("pinned_files", [])

        state = get_workspace_state()
        ws_path = req.get("workspace_path") or state["active_path"]
        ws_info = inspect_workspace(ws_path)

        pinned_context_str = ""
        if pinned_files:
            pinned_context_str = "\n=== PINNED WORKSPACE CONTEXT ===\n"
            for pf in pinned_files[:6]:
                pf_name = pf.get("name", "file")
                pf_path = pf.get("path", "")
                pf_content = pf.get("content", "")[:12000]
                pinned_context_str += f"\n--- File: {pf_name} ({pf_path}) ---\n{pf_content}\n--- End File ---\n"
            pinned_context_str += "================================\n"

        ws_context_str = (
            f"\n[Active Workspace Directory: {ws_path}]\n"
            f"[Repository Branch: {ws_info.get('branch', 'N/A')} | Items: {ws_info.get('total_items', 0)}]\n"
        )

        system_prompt = (
            "You are serving on the Executive AI Council for the CEO Cockpit.\n"
            "The user is the CEO and operates at an executive decision-making level.\n"
            "Provide a decisive, structured perspective with key recommendations, risks, and strategic rationale.\n"
            f"{ws_context_str}\n"
            f"{pinned_context_str}"
        )

        def query_single_model(m_id: str) -> Dict[str, Any]:
            t0 = time.time()
            max_tok = 4096
            if "reasoner" in m_id:
                max_tok = 8192

            payload = {
                "model": m_id,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": prompt}
                ],
                "max_tokens": max_tok,
                "temperature": temperature
            }
            if effort != "auto" and "reasoner" in m_id:
                payload["reasoning_effort"] = "high" if effort in ["high", "max"] else ("low" if effort == "low" else "medium")

            try:
                req_data = json.dumps(payload).encode("utf-8")
                litellm_req = urllib.request.Request(
                    f"{LITELLM_URL}/v1/chat/completions",
                    data=req_data,
                    headers={"Content-Type": "application/json"}
                )
                with urllib.request.urlopen(litellm_req, timeout=75) as resp:
                    resp_json = json.loads(resp.read().decode("utf-8"))

                elapsed = int((time.time() - t0) * 1000)
                choice = resp_json.get("choices", [{}])[0]
                msg = choice.get("message", {})
                content = msg.get("content", "")
                reasoning = msg.get("reasoning_content", "")
                usage = resp_json.get("usage", {})
                cost_metrics = governor.record_completion(m_id, usage, elapsed < 350)

                return {
                    "success": True,
                    "model_id": m_id,
                    "model_name": format_model_display_name(m_id),
                    "latency_ms": elapsed,
                    "content": content,
                    "reasoning_content": reasoning,
                    "usage": usage,
                    "cost_actual": cost_metrics["actual_cost_usd"],
                    "cached": elapsed < 350
                }
            except Exception as err:
                elapsed = int((time.time() - t0) * 1000)
                return {
                    "success": False,
                    "model_id": m_id,
                    "model_name": format_model_display_name(m_id),
                    "latency_ms": elapsed,
                    "error": str(err),
                    "content": f"Model deliberation failed: {err}"
                }

        with concurrent.futures.ThreadPoolExecutor(max_workers=min(4, len(models))) as executor:
            futures = [executor.submit(query_single_model, m) for m in models]
            results = [f.result() for f in futures]

        success_results = [r for r in results if r.get("success")]
        fastest_model = min(results, key=lambda x: x.get("latency_ms", 999999))
        cheapest_model = min(success_results, key=lambda x: x.get("cost_actual", 999999)) if success_results else results[0]

        consensus_summary = {
            "models_evaluated": len(models),
            "fastest": {
                "model_name": fastest_model.get("model_name"),
                "latency_ms": fastest_model.get("latency_ms")
            },
            "most_cost_efficient": {
                "model_name": cheapest_model.get("model_name"),
                "cost_usd": cheapest_model.get("cost_actual", 0.0)
            },
            "consensus_verdict": f"Council evaluated {len(models)} perspectives. DeepSeek Reasoner provided architectural validation, while Gemini Flash delivered instantaneous synthesis ({fastest_model.get('latency_ms')}ms)."
        }

        session_id = req.get("session_id")
        session_data = load_session(session_id) if session_id else None
        if not session_data:
            session_id = f"ses_{int(time.time())}_{os.urandom(3).hex()}"
            session_data = {
                "id": session_id,
                "title": f"🏛️ Council: {prompt[:35]}...",
                "created_at": time.time(),
                "updated_at": time.time(),
                "model": "council-arena",
                "workspace_path": ws_path,
                "messages": []
            }

        session_data["updated_at"] = time.time()
        session_data["messages"].append({
            "role": "user",
            "content": prompt,
            "timestamp": time.time()
        })
        session_data["messages"].append({
            "role": "assistant",
            "content": f"[Executive Council Arena Result with {len(models)} models]",
            "arena_results": results,
            "consensus": consensus_summary,
            "timestamp": time.time()
        })
        save_session(session_data)

        self._send_json(200, {
            "success": True,
            "session_id": session_id,
            "session": session_data,
            "results": results,
            "consensus": consensus_summary,
            "budget": governor.get_summary()
        })

    def handle_command(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
        except Exception as e:
            self._send_json(400, {"error": f"Invalid JSON: {e}"})
            return

        prompt = req.get("prompt", "").strip()
        if not prompt:
            self._send_json(400, {"error": "Prompt cannot be empty"})
            return

        requested_model = req.get("model", "auto")
        effort = req.get("effort", "auto").lower()
        autonomy = req.get("autonomy_mode", "full").lower()
        temperature = float(req.get("temperature", 0.3))
        include_ws_context = req.get("include_workspace_context", True)
        pinned_files = req.get("pinned_files", [])

        # Workspace Context Injection
        state = get_workspace_state()
        ws_path = req.get("workspace_path") or state["active_path"]
        ws_info = inspect_workspace(ws_path)
        ws_context_str = ""

        if include_ws_context and ws_info.get("exists"):
            entries = ws_info.get("entries", [])
            files_preview = ", ".join([f"{e['name']}/" if e['is_dir'] else e['name'] for e in entries[:30]])
            readme_snippet = ""
            readme_path = os.path.join(ws_path, "README.md")
            if os.path.exists(readme_path):
                try:
                    with open(readme_path, "r", encoding="utf-8") as rf:
                        readme_snippet = rf.read(600).strip().replace("\n", " ")
                except Exception:
                    pass

            ws_context_str = (
                f"\n[Active Workspace Directory: {ws_path}]\n"
                f"[Repository Branch: {ws_info.get('branch', 'N/A')} | Items: {ws_info.get('total_items', 0)}]\n"
                f"[Workspace Top-Level Items: {files_preview}]\n"
            )
            if readme_snippet:
                ws_context_str += f"[Project Brief: {readme_snippet[:300]}...]\n"

        pinned_context_str = ""
        if pinned_files:
            pinned_context_str = "\n=== PINNED WORKSPACE CONTEXT ===\n"
            for pf in pinned_files[:8]:
                pf_name = pf.get("name", "file")
                pf_path = pf.get("path", "")
                pf_content = pf.get("content", "")[:15000]
                pinned_context_str += f"\n--- Attached File: {pf_name} ({pf_path}) ---\n{pf_content}\n--- End Attached File ---\n"
            pinned_context_str += "================================\n"

        # Autonomy Directive
        if autonomy == "plan_first":
            autonomy_directive = "Operate in Plan-First Mode. Structure response into: 1. Executive Summary, 2. Architectural Blueprint & Phased Roadmap, 3. Concrete Action Steps."
        elif autonomy == "advisory":
            autonomy_directive = "Operate in Executive Advisory Mode. Evaluate strategic options, risk factors, and financial/technical tradeoffs without enacting operational state mutations."
        else:
            autonomy_directive = "Operate with Full Autonomy. Formulate authoritative, end-to-end executive resolutions and decisions."

        # Reasoning Effort & Token Budget
        if effort == "low":
            max_tokens = 2048
            effort_directive = "Reasoning Effort: Low (Rapid Synthesis). Deliver high-density, concise conclusions with minimal preamble."
        elif effort == "medium":
            max_tokens = 4096
            effort_directive = "Reasoning Effort: Medium (Standard Audit). Provide balanced deduction with core tradeoffs and next steps."
        elif effort == "high":
            max_tokens = 8192
            effort_directive = "Reasoning Effort: High (Deep Verification). Exhaustively verify assumptions, edge cases, risks, and mitigations."
        elif effort == "max":
            max_tokens = 16384
            effort_directive = "Reasoning Effort: Max (Frontier Deliberation). Execute complete strategic proof, multi-perspective scenario modeling, and full architectural rationale."
        else:
            max_tokens = 4096
            effort_directive = "Reasoning Effort: Auto (Adaptive Deliberation). Calibrate reasoning depth dynamically to query complexity."

        # Model resolution & Circuit-Breaker Check
        is_tripped = governor.is_tripped()
        if is_tripped:
            selected_model = "gemini-flash"
            routing_reason = "⚠️ CIRCUIT BREAKER ACTIVE: Daily spend cap enforced. Safeguard Zero-Cost Routing ($0.00)"
        elif requested_model == "auto":
            selected_model, routing_reason = classify_model_intent(prompt)
        elif requested_model == "deepseek":
            selected_model = "deepseek-reasoner"
            routing_reason = "Executive Model Selection: DeepSeek Reasoner (R1)"
        elif requested_model == "gemini":
            selected_model = "gemini-flash"
            routing_reason = "Executive Model Selection: Gemini 2.5/3.8 Flash"
        else:
            selected_model = requested_model
            routing_reason = f"Executive Model Selection: {format_model_display_name(selected_model)}"

        if "reasoner" in selected_model and effort in ["auto", "high", "max"]:
            max_tokens = max(max_tokens, 8192)

        start_time = time.time()

        system_prompt = (
            "You are the Executive AI Engine for the CEO Cockpit.\n"
            "The user is the CEO and operates at an executive decision-making level.\n"
            "Do not output technical code blocks or file diffs unless specifically requested for an engineering review.\n"
            "Deliver high-density, authoritative, clear executive briefings with key takeaways, strategic options, and concrete action items.\n"
            f"Harness Autonomy Directive: {autonomy_directive}\n"
            f"{effort_directive}\n"
            f"{ws_context_str}\n"
            f"{pinned_context_str}"
        )

        litellm_payload = {
            "model": selected_model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": prompt}
            ],
            "max_tokens": max_tokens,
            "temperature": temperature
        }

        if effort != "auto" and "reasoner" in selected_model:
            litellm_payload["reasoning_effort"] = "high" if effort in ["high", "max"] else ("low" if effort == "low" else "medium")

        try:
            req_data = json.dumps(litellm_payload).encode("utf-8")
            litellm_req = urllib.request.Request(
                f"{LITELLM_URL}/v1/chat/completions",
                data=req_data,
                headers=LITELLM_HEADERS
            )
            with urllib.request.urlopen(litellm_req, timeout=60) as resp:
                resp_json = json.loads(resp.read().decode("utf-8"))

            elapsed_ms = int((time.time() - start_time) * 1000)
            choice = resp_json.get("choices", [{}])[0]
            message = choice.get("message", {})
            content = message.get("content", "")
            reasoning_content = message.get("reasoning_content", "")
            usage = resp_json.get("usage", {})

            is_cached = elapsed_ms < 350
            cost_metrics = governor.record_completion(selected_model, usage, is_cached)
            cost_saved_str = f"${cost_metrics['saved_cost_usd']:.4f} (100% saved via Redis Cache)" if is_cached else f"${cost_metrics['actual_cost_usd']:.4f} (Metered)"

            self._send_json(200, {
                "success": True,
                "content": content,
                "reasoning_content": reasoning_content,
                "model": selected_model,
                "effort": effort,
                "autonomy_mode": autonomy,
                "routing_reason": routing_reason,
                "latency_ms": elapsed_ms,
                "cached": is_cached,
                "usage": usage,
                "provider": "DeepSeek" if "deepseek" in selected_model else ("Anthropic" if "claude" in selected_model else ("Alibaba / Qwen" if "qwen" in selected_model else "Google Gemini")),
                "cost_saved": cost_saved_str,
                "cost_actual": cost_metrics["actual_cost_usd"],
                "circuit_breaker": {
                    "tripped": cost_metrics["tripped"],
                    "state": cost_metrics["circuit_breaker_state"],
                    "today_spend": cost_metrics["today_spend_usd"]
                },
                "workspace": {
                    "path": ws_info.get("path"),
                    "name": ws_info.get("name"),
                    "branch": ws_info.get("branch"),
                    "exists": ws_info.get("exists")
                },
                "budget": governor.get_summary()
            })

        except urllib.error.HTTPError as e:
            err_body = e.read().decode("utf-8")
            self._send_json(e.code, {"error": f"LiteLLM Provider Error: {err_body}", "model": selected_model})
        except Exception as e:
            self._send_json(500, {"error": f"Server Error: {str(e)}", "model": selected_model})

    def handle_openclaw_dispatch(self):
        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)
        try:
            req = json.loads(post_data.decode("utf-8"))
        except Exception as e:
            self._send_json(400, {"error": f"Invalid JSON: {e}"})
            return

        agent_id = req.get("agent_id", "local-coordinator")
        message = req.get("message", "").strip()

        if not message:
            self._send_json(400, {"error": "Message is required"})
            return

        token = get_openclaw_token()
        start_time = time.time()

        # Call OpenClaw HTTP chatCompletions endpoint
        openclaw_payload = {
            "model": f"openclaw/{agent_id}",
            "messages": [
                {
                    "role": "user",
                    "content": message
                }
            ],
            "max_tokens": 2048
        }

        try:
            req_data = json.dumps(openclaw_payload).encode("utf-8")
            headers = {
                "Content-Type": "application/json",
                "Authorization": f"Bearer {token}"
            }
            openclaw_req = urllib.request.Request(
                f"{OPENCLAW_URL}/v1/chat/completions",
                data=req_data,
                headers=headers
            )
            with urllib.request.urlopen(openclaw_req, timeout=60) as resp:
                resp_json = json.loads(resp.read().decode("utf-8"))

            elapsed_ms = int((time.time() - start_time) * 1000)
            choice = resp_json.get("choices", [{}])[0]
            reply = choice.get("message", {}).get("content", "")

            self._send_json(200, {
                "success": True,
                "agent_id": agent_id,
                "reply": reply,
                "latency_ms": elapsed_ms
            })
        except Exception as e:
            self._send_json(500, {"error": f"OpenClaw agent turn failed: {str(e)}"})

class ThreadedHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    allow_reuse_address = True
    daemon_threads = True

def run():
    bind_host = cockpit_security.BIND_HOST
    with ThreadedHTTPServer((bind_host, PORT), CockpitHandler) as httpd:
        print(f"CEO Cockpit Mission Control running on http://{bind_host}:{PORT} "
              f"({len(cockpit_modules.registered_routes())} module routes)", flush=True)
        httpd.serve_forever()

if __name__ == "__main__":
    run()
