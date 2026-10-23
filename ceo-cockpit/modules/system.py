"""
Round 10 — System Hardening, Executive Manifest & Mission Control Diagnostics.

Provides:
  * GET  /api/system/manifest - Full capability inventory, 10-round roadmap status, registered routes, modules, and infrastructure telemetry.
  * GET  /api/system/health   - Fast aggregate health assessment across all subsystems.
  * POST /api/system/ping     - Ping/pong heartbeat verification with audit logging.
"""

import os
import platform
import socket
import subprocess
import time
from typing import Any, Dict

from budget_governor import governor
from modules import registered_routes, route
from modules.common import active_workspace, emit_event, log, send_json

START_TIME = time.time()
VERSION = "2.10.0-mission-control"

ROADMAP_ROUNDS = [
    {"round": 1, "name": "Multi-Model Council Arena", "status": "operational", "focus": "Parallel multi-model routing & consensus arena"},
    {"round": 2, "name": "Workspace Explorer & Governor", "status": "operational", "focus": "Recursive tree hierarchy, context pinning dock & budget governor"},
    {"round": 3, "name": "Command Deck & Curated Runbooks", "status": "operational", "focus": "Slide-out executive workspace & 12 curated operational runbooks"},
    {"round": 4, "name": "Multi-Agent Swarm Mission Control", "status": "operational", "focus": "5 specialized agent archetypes & Chief of Staff synthesis"},
    {"round": 5, "name": "Executive Voice & Audio Briefing", "status": "operational", "focus": "STT directive dictation & SpeechSynthesis audio briefings"},
    {"round": 6, "name": "Strategic Executive Playbooks", "status": "operational", "focus": "Curated board-grade technical blueprints & RFC generation"},
    {"round": 7, "name": "Spend Intelligence & Latency", "status": "operational", "focus": "Financial ledger, run rate, Redis savings & latency metrics"},
    {"round": 8, "name": "Git Hygiene & Conventional Release", "status": "operational", "focus": "Git status, staged diff inspector & AI commit builder"},
    {"round": 9, "name": "Real-Time Activity Stream", "status": "operational", "focus": "Live SSE operational event stream & persistent audit trail"},
    {"round": 10, "name": "Mission Control Hardening & Matrix", "status": "operational", "focus": "System manifest, cross-wired shortcuts & complete verification suite"}
]


def _check_tcp(host: str, port: int, timeout: float = 0.8) -> bool:
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except (socket.timeout, ConnectionRefusedError, OSError):
        return False


def _check_clicker() -> Dict[str, Any]:
    candidate_dirs = [
        r"C:\Users\user\AppData\Local\Temp",
        os.path.join(os.environ.get("SystemDrive", "C:"), "Users", os.environ.get("USERNAME", "user"), "AppData", "Local", "Temp"),
        os.environ.get("TEMP", ""),
        os.environ.get("TMP", ""),
        os.path.expanduser("~")
    ]
    pid = None
    heartbeat_age = 999.0
    for d in candidate_dirs:
        if not d or not os.path.isdir(d):
            continue
        pid_file = os.path.join(d, "auto_click_decisions.pid")
        hb_file = os.path.join(d, "auto_click_decisions.heartbeat")
        if os.path.isfile(hb_file):
            try:
                with open(hb_file, "r", encoding="utf-8") as f:
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

    return {
        "online": heartbeat_age <= 20.0,
        "pid": pid,
        "heartbeat_age_sec": round(heartbeat_age, 1)
    }


def _get_git_summary(ws_path: str) -> Dict[str, Any]:
    if not ws_path or not os.path.isdir(ws_path):
        return {"is_repo": False}
    try:
        r = subprocess.run(["git", "rev-parse", "--is-inside-work-tree"],
                           cwd=ws_path, capture_output=True, text=True, timeout=2)
        if r.returncode != 0:
            return {"is_repo": False}
        branch_proc = subprocess.run(["git", "branch", "--show-current"],
                                     cwd=ws_path, capture_output=True, text=True, timeout=2)
        status_proc = subprocess.run(["git", "status", "--porcelain"],
                                     cwd=ws_path, capture_output=True, text=True, timeout=3)
        return {
            "is_repo": True,
            "branch": branch_proc.stdout.strip() or "HEAD",
            "dirty_files": len([l for l in status_proc.stdout.splitlines() if l.strip()])
        }
    except Exception:
        return {"is_repo": False}


@route("GET", "/api/system/manifest")
def get_system_manifest(h, query, tail):
    """Returns the comprehensive CEO Cockpit capability manifest."""
    ws = active_workspace()
    routes = registered_routes()
    clicker = _check_clicker()

    litellm_ok = _check_tcp("127.0.0.1", 4000)
    openclaw_ok = _check_tcp("127.0.0.1", 18789)
    redis_ok = _check_tcp("127.0.0.1", 6379)
    qdrant_ok = _check_tcp("127.0.0.1", 6333)

    manifest = {
        "success": True,
        "app": "Antigravity CEO Cockpit",
        "tagline": "Executive AI OS & Mission Control Architecture",
        "version": VERSION,
        "uptime_sec": int(time.time() - START_TIME),
        "platform": {
            "system": platform.system(),
            "release": platform.release(),
            "python": platform.python_version()
        },
        "roadmap": {
            "completed_rounds": 10,
            "total_rounds": 10,
            "status": "100% Operational",
            "rounds": ROADMAP_ROUNDS
        },
        "modules": [
            {"id": "ops", "name": "Operations Console", "icon": "🛠️", "shortcut": "Alt+1"},
            {"id": "swarm", "name": "Swarm Mission Control", "icon": "🐝", "shortcut": "Alt+2"},
            {"id": "voice", "name": "Executive Voice Engine", "icon": "🎙️", "shortcut": "Alt+3"},
            {"id": "playbooks", "name": "Strategic Playbooks", "icon": "📚", "shortcut": "Alt+4"},
            {"id": "analytics", "name": "Spend Intelligence", "icon": "📊", "shortcut": "Alt+5"},
            {"id": "git", "name": "Git Hygiene & Release", "icon": "🌿", "shortcut": "Alt+6"},
            {"id": "activity", "name": "Activity Stream & Audit", "icon": "📡", "shortcut": "Alt+7"},
            {"id": "system", "name": "System Hardening & Manifest", "icon": "🛡️", "shortcut": "Ctrl+J"}
        ],
        "registered_routes": {
            "count": len(routes),
            "endpoints": routes
        },
        "subsystems": {
            "litellm": {"port": 4000, "online": litellm_ok},
            "openclaw": {"port": 18789, "online": openclaw_ok},
            "redis": {"port": 6379, "online": redis_ok},
            "qdrant": {"port": 6333, "online": qdrant_ok},
            "autoclicker": clicker
        },
        "workspace": {
            "path": ws,
            "git": _get_git_summary(ws)
        },
        "budget": governor.get_summary()
    }
    send_json(h, 200, manifest)


@route("GET", "/api/system/health")
def get_system_health(h, query, tail):
    """Fast aggregate health report."""
    clicker = _check_clicker()
    litellm_ok = _check_tcp("127.0.0.1", 4000)
    openclaw_ok = _check_tcp("127.0.0.1", 18789)
    redis_ok = _check_tcp("127.0.0.1", 6379)
    qdrant_ok = _check_tcp("127.0.0.1", 6333)

    healthy = bool(litellm_ok and openclaw_ok and redis_ok and clicker.get("online"))
    send_json(h, 200, {
        "success": True,
        "healthy": healthy,
        "status": "healthy" if healthy else "degraded",
        "subsystems": {
            "litellm": litellm_ok,
            "openclaw": openclaw_ok,
            "redis": redis_ok,
            "qdrant": qdrant_ok,
            "autoclicker": clicker.get("online")
        },
        "timestamp": time.time()
    })


@route("POST", "/api/system/ping")
def post_system_ping(h, query, tail):
    """Heartbeat ping that logs an event and validates responsiveness."""
    t0 = time.time()
    client_ip = h.client_address[0] if hasattr(h, "client_address") else "loopback"
    emit_event("system", "CEO System Heartbeat Verified", f"Heartbeat received from {client_ip}", "info", {"client": client_ip})
    send_json(h, 200, {
        "success": True,
        "pong": True,
        "latency_ms": round((time.time() - t0) * 1000, 2),
        "timestamp": time.time()
    })
