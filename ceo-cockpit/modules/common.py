"""
Shared utilities for CEO Cockpit extension modules.

Provides: paths, timestamped logging, JSON body/response helpers, SSE helpers,
active-workspace resolution, the activity event bus, and a governed LLM helper.
"""

import json
import os
import threading
import time
import urllib.request
from datetime import datetime
from typing import Any, Dict, List, Optional

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")
WORKSPACE_STATE_FILE = os.path.join(BASE_DIR, "workspace_state.json")
LITELLM_URL = "http://127.0.0.1:4000"

os.makedirs(DATA_DIR, exist_ok=True)

_file_lock = threading.Lock()


# ------------------------------------------------------------------------------
# Logging
# ------------------------------------------------------------------------------
def log(component: str, message: str) -> None:
    """Timestamped log line routed to stdout (captured by NSSM into cockpit.log)."""
    stamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    try:
        print(f"[{stamp}] [{component}] {message}", flush=True)
    except Exception:
        pass


# ------------------------------------------------------------------------------
# HTTP helpers
# ------------------------------------------------------------------------------
def read_json_body(handler) -> Dict[str, Any]:
    length = int(handler.headers.get("Content-Length", 0) or 0)
    if length <= 0:
        return {}
    raw = handler.rfile.read(length)
    try:
        data = json.loads(raw.decode("utf-8"))
        return data if isinstance(data, dict) else {}
    except Exception:
        raise ValueError("Request body must be valid JSON")


def send_json(handler, status: int, data: Dict[str, Any]) -> None:
    handler._send_json(status, data)


def start_sse(handler) -> None:
    handler.send_response(200)
    handler.send_header("Content-Type", "text/event-stream")
    handler.send_header("Cache-Control", "no-cache")
    handler.send_header("Connection", "close")
    handler.send_header("X-Accel-Buffering", "no")
    handler.end_headers()
    try:
        handler.close_connection = True
    except Exception:
        pass


def write_sse(handler, payload: Dict[str, Any]) -> bool:
    """Write one SSE frame. Returns False if the client disconnected."""
    try:
        handler.wfile.write(f"data: {json.dumps(payload)}\n\n".encode("utf-8"))
        handler.wfile.flush()
        return True
    except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError, OSError):
        return False


# ------------------------------------------------------------------------------
# JSON / JSONL persistence
# ------------------------------------------------------------------------------
def data_path(name: str) -> str:
    return os.path.join(DATA_DIR, name)


def load_json(path: str, default: Any) -> Any:
    try:
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
    except Exception as e:
        log("common", f"Failed to load {path}: {e}")
    return default


def save_json(path: str, data: Any) -> None:
    tmp = path + ".tmp"
    with _file_lock:
        with open(tmp, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
        os.replace(tmp, path)


def append_jsonl(path: str, record: Dict[str, Any], max_lines: int = 5000) -> None:
    with _file_lock:
        with open(path, "a", encoding="utf-8") as f:
            f.write(json.dumps(record) + "\n")
        # Opportunistic compaction to keep files bounded.
        try:
            if os.path.getsize(path) > max_lines * 600:
                with open(path, "r", encoding="utf-8") as f:
                    lines = f.readlines()
                if len(lines) > max_lines:
                    with open(path, "w", encoding="utf-8") as f:
                        f.writelines(lines[-max_lines:])
        except Exception:
            pass


def read_jsonl(path: str, limit: Optional[int] = None) -> List[Dict[str, Any]]:
    if not os.path.exists(path):
        return []
    out: List[Dict[str, Any]] = []
    try:
        with open(path, "r", encoding="utf-8") as f:
            lines = f.readlines()
        if limit:
            lines = lines[-limit:]
        for line in lines:
            line = line.strip()
            if not line:
                continue
            try:
                out.append(json.loads(line))
            except Exception:
                continue
    except Exception as e:
        log("common", f"Failed to read {path}: {e}")
    return out


# ------------------------------------------------------------------------------
# Workspace
# ------------------------------------------------------------------------------
def active_workspace() -> str:
    state = load_json(WORKSPACE_STATE_FILE, {})
    path = state.get("active_path") if isinstance(state, dict) else None
    if path and os.path.isdir(path):
        return path
    return os.path.dirname(BASE_DIR)


# ------------------------------------------------------------------------------
# Activity event bus (consumed by the Activity Feed module)
# ------------------------------------------------------------------------------
ACTIVITY_FILE = data_path("activity.jsonl")
EVENT_SUBSCRIBERS: List[Any] = []
_event_lock = threading.Lock()


def emit_event(kind: str, title: str, detail: str = "", level: str = "info",
               meta: Optional[Dict[str, Any]] = None) -> None:
    record = {
        "ts": time.time(),
        "kind": kind,
        "level": level,
        "title": title,
        "detail": detail[:2000] if detail else "",
        "meta": meta or {},
    }
    try:
        append_jsonl(ACTIVITY_FILE, record, max_lines=3000)
    except Exception as e:
        log("events", f"emit failed: {e}")

    with _event_lock:
        for q in list(EVENT_SUBSCRIBERS):
            try:
                q.put_nowait(record)
            except Exception:
                pass


# ------------------------------------------------------------------------------
# Governed LLM helper (budget-aware, used by background modules)
# ------------------------------------------------------------------------------
def llm_complete(model: str, messages: List[Dict[str, str]], max_tokens: int = 2048,
                 temperature: float = 0.3, timeout: int = 120,
                 source: str = "module") -> Dict[str, Any]:
    """Call LiteLLM with budget-governor enforcement and cost accounting."""
    from budget_governor import governor  # local import avoids circular deps

    if governor.is_tripped():
        model = "gemini-flash"

    payload = {"model": model, "messages": messages,
               "max_tokens": max_tokens, "temperature": temperature}
    t0 = time.time()
    req = urllib.request.Request(
        f"{LITELLM_URL}/v1/chat/completions",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        body = json.loads(resp.read().decode("utf-8"))
    latency_ms = int((time.time() - t0) * 1000)
    choice = (body.get("choices") or [{}])[0]
    msg = choice.get("message", {}) or {}
    usage = body.get("usage", {}) or {}
    cached = latency_ms < 350
    cost = governor.record_completion(model, usage, cached)
    try:
        from modules import telemetry
        telemetry.record_latency(model, latency_ms, source)
    except Exception:
        pass
    return {
        "model": model,
        "content": msg.get("content", "") or "",
        "reasoning": msg.get("reasoning_content", "") or "",
        "usage": usage,
        "latency_ms": latency_ms,
        "cost_usd": cost.get("actual_cost_usd", 0.0),
        "cached": cached,
    }
