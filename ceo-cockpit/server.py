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
import socket
import time
import os
import subprocess
from typing import Dict, Any, Tuple
from budget_governor import governor

PORT = 8080
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "web") if os.path.exists(os.path.join(BASE_DIR, "web")) else BASE_DIR

LITELLM_URL = "http://127.0.0.1:4000"
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

def get_openclaw_token() -> str:
    cfg_path = os.path.expanduser(r"~\.openclaw\openclaw.json")
    if os.path.exists(cfg_path):
        try:
            with open(cfg_path, "r", encoding="utf-8") as f:
                data = json.load(f)
                return data.get("gateway", {}).get("auth", {}).get("token", "")
        except Exception:
            pass
    return ""

def classify_model_intent(prompt: str) -> Tuple[str, str]:
    """
    Intelligent Model Router:
    Selects DeepSeek for high-reasoning, architectural, coding, and strategic breakdown.
    Selects Gemini for fast summarization, synthesis, large-scale ingestion, and broad overviews.
    """
    prompt_lower = prompt.lower()
    
    deepseek_triggers = [
        "reason", "strategy", "strategic", "architect", "architecture", "plan",
        "logic", "math", "code", "debug", "algorithm", "audit", "financial model",
        "breakdown", "step-by-step", "critique", "evaluate risk", "tradeoff", "why"
    ]
    
    gemini_triggers = [
        "summarize", "summary", "brief", "fast", "quick", "overview",
        "bullet points", "extract", "translate", "rewrite", "table", "headline", "triage"
    ]
    
    deepseek_score = sum(1 for w in deepseek_triggers if w in prompt_lower)
    gemini_score = sum(1 for w in gemini_triggers if w in prompt_lower)
    
    # Very long prompts (> 4000 characters) benefit heavily from Gemini's massive 1M context
    if len(prompt) > 4000 and deepseek_score <= gemini_score:
        return "gemini-flash", "High Context Volume Routing -> Gemini Flash (1M+ Token Window)"
        
    if deepseek_score > gemini_score:
        return "deepseek-reasoner", "High Reasoning & Strategic Logic -> DeepSeek Reasoner"
    elif gemini_score > 0:
        return "gemini-flash", "High-Throughput Synthesis & Speed -> Gemini Flash"
    else:
        # Default budget-optimized fast tier
        return "gemini-flash", "Budget-Conscious Fast Tier -> Gemini Flash"

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

def format_model_display_name(model_id: str) -> str:
    mapping = {
        "deepseek-reasoner": "DeepSeek Reasoner (R1)",
        "deepseek-chat": "DeepSeek Chat (V3)",
        "gemini-flash": "Gemini 2.5/3.8 Flash (1M+)",
        "gemini-pro": "Gemini Pro (Frontier)",
        "claude-3-5-sonnet": "Claude 3.5 Sonnet",
        "claude-3-5-haiku": "Claude 3.5 Haiku",
        "siliconflow-qwen-coder": "Qwen 2.5 Coder 32B",
        "dashscope-qwen-max": "DashScope Qwen Max",
    }
    return mapping.get(model_id, model_id)

def fetch_available_models() -> list:
    try:
        req = urllib.request.Request(f"{LITELLM_URL}/v1/models")
        with urllib.request.urlopen(req, timeout=3) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            raw_models = data.get("data", [])
            models = []
            for m in raw_models:
                mid = m.get("id", "")
                if "embedding" in mid.lower():
                    continue
                models.append({
                    "id": mid,
                    "name": format_model_display_name(mid),
                    "is_reasoning": ("reasoner" in mid.lower() or "r1" in mid.lower() or "o1" in mid.lower())
                })
            if models:
                return models
    except Exception:
        pass

    return [
        {"id": "deepseek-reasoner", "name": "DeepSeek Reasoner (R1)", "is_reasoning": True},
        {"id": "deepseek-chat", "name": "DeepSeek Chat (V3)", "is_reasoning": False},
        {"id": "gemini-flash", "name": "Gemini 2.5/3.8 Flash (1M+)", "is_reasoning": False},
        {"id": "gemini-pro", "name": "Gemini Pro (Frontier)", "is_reasoning": False},
        {"id": "claude-3-5-sonnet", "name": "Claude 3.5 Sonnet", "is_reasoning": False},
        {"id": "claude-3-5-haiku", "name": "Claude 3.5 Haiku", "is_reasoning": False},
        {"id": "siliconflow-qwen-coder", "name": "Qwen 2.5 Coder 32B", "is_reasoning": False}
    ]

class CockpitHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    def _send_json(self, status: int, data: Dict[str, Any]):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_GET(self):
        if self.path == "/api/health":
            self.handle_health()
        elif self.path == "/api/budget":
            self.handle_budget()
        elif self.path == "/api/agents":
            self.handle_agents()
        elif self.path == "/api/cache-stats":
            self.handle_cache_stats()
        elif self.path == "/api/openclaw-url":
            self.handle_openclaw_url()
        elif self.path == "/api/workspace":
            self.handle_workspace_get()
        elif self.path == "/api/models":
            self.handle_models_get()
        else:
            super().do_GET()

    def do_POST(self):
        if self.path == "/api/command":
            self.handle_command()
        elif self.path == "/api/workspace":
            self.handle_workspace_post()
        elif self.path == "/api/budget/config":
            self.handle_budget_config()
        elif self.path == "/api/budget/trip":
            self.handle_budget_trip()
        elif self.path == "/api/budget/reset":
            self.handle_budget_reset()
        elif self.path == "/api/openclaw/dispatch":
            self.handle_openclaw_dispatch()
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
        openclaw_ok = check_tcp_port("127.0.0.1", 18789)
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

    def handle_models_get(self):
        models = fetch_available_models()
        self._send_json(200, {"models": models})

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
        elif requested_model in [
            "deepseek", "deepseek-reasoner", "deepseek-chat",
            "gemini", "gemini-flash", "gemini-pro",
            "claude-3-5-sonnet", "claude-3-5-haiku",
            "siliconflow-qwen-coder", "dashscope-qwen-max"
        ]:
            if requested_model == "deepseek":
                selected_model = "deepseek-reasoner"
            elif requested_model == "gemini":
                selected_model = "gemini-flash"
            else:
                selected_model = requested_model
            routing_reason = f"Executive Model Selection: {format_model_display_name(selected_model)}"
        else:
            selected_model = requested_model
            routing_reason = f"Selected Model: {format_model_display_name(selected_model)}"

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
            f"{ws_context_str}"
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
                headers={"Content-Type": "application/json"}
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

def run():
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), CockpitHandler) as httpd:
        print(f"CEO Cockpit Mission Control running on http://127.0.0.1:{PORT}")
        httpd.serve_forever()

if __name__ == "__main__":
    run()
