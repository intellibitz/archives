"""
Round 15 — Cross-Harness Interop & Gateway Matrix.

Continuous multi-gateway probing across LiteLLM (4000), OpenClaw (18789),
Redis/Memurai (6379), Qdrant (6333), and Local LLM/Ollama (11434).
"""

import concurrent.futures
import socket
import time
import urllib.request
import urllib.error
from typing import Any, Dict, List
from modules import route
from modules.common import emit_event, send_json

GATEWAYS = [
    {
        "id": "litellm",
        "name": "LiteLLM Unified Proxy",
        "host": "127.0.0.1",
        "port": 4000,
        "http_check": "http://127.0.0.1:4000/v1/models",
        "protocol": "HTTP/REST",
        "role": "Model Routing & Cost Metering",
        "failover_rank": 1
    },
    {
        "id": "openclaw",
        "name": "OpenClaw Multi-Agent Gateway",
        "host": "127.0.0.1",
        "port": 18789,
        "http_check": "http://127.0.0.1:18789/",
        "protocol": "HTTP/SSE",
        "role": "Autonomous Swarm Orchestration",
        "failover_rank": 2
    },
    {
        "id": "redis",
        "name": "Redis / Memurai In-Memory Cache",
        "host": "127.0.0.1",
        "port": 6379,
        "http_check": None,
        "protocol": "RESP",
        "role": "Exact-Match Zero-Cost Inference Cache",
        "failover_rank": 0
    },
    {
        "id": "qdrant",
        "name": "Qdrant Vector Database",
        "host": "127.0.0.1",
        "port": 6333,
        "http_check": "http://127.0.0.1:6333/dashboard",
        "protocol": "HTTP/gRPC",
        "role": "Semantic Long-Term Memory & ADRs",
        "failover_rank": 3
    },
    {
        "id": "ollama",
        "name": "Local LLM / Ollama Engine",
        "host": "127.0.0.1",
        "port": 11434,
        "http_check": "http://127.0.0.1:11434/api/version",
        "protocol": "HTTP/REST",
        "role": "Offline Zero-Cost Local Inference",
        "failover_rank": 4
    }
]


def _probe_single(gw: Dict[str, Any]) -> Dict[str, Any]:
    host = gw["host"]
    port = gw["port"]
    t0 = time.time()
    tcp_ok = False

    try:
        with socket.create_connection((host, port), timeout=0.8):
            tcp_ok = True
    except (socket.timeout, ConnectionRefusedError, OSError):
        tcp_ok = False

    elapsed_ms = round((time.time() - t0) * 1000, 2)
    http_status = None

    if tcp_ok and gw.get("http_check"):
        t_http = time.time()
        try:
            req = urllib.request.Request(gw["http_check"], headers={"User-Agent": "Gateway-Matrix/1.0"})
            with urllib.request.urlopen(req, timeout=1.0) as resp:
                http_status = resp.status
                elapsed_ms = round((time.time() - t_http) * 1000, 2)
        except urllib.error.HTTPError as he:
            http_status = he.code
            elapsed_ms = round((time.time() - t_http) * 1000, 2)
        except Exception:
            http_status = 503

    status = "HEALTHY" if tcp_ok else "OFFLINE"
    if tcp_ok and elapsed_ms > 350:
        status = "DEGRADED"

    return {
        **gw,
        "status": status,
        "tcp_connected": tcp_ok,
        "http_status": http_status,
        "latency_ms": elapsed_ms,
        "checked_at": time.time()
    }


def _probe_all() -> List[Dict[str, Any]]:
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as ex:
        results = list(ex.map(_probe_single, GATEWAYS))
    return results


@route("GET", "/api/gateways/status")
def handle_gateways_status(handler, query, tail):
    results = _probe_all()
    healthy_count = sum(1 for r in results if r["status"] == "HEALTHY")
    send_json(handler, 200, {
        "success": True,
        "healthy_count": healthy_count,
        "total_count": len(results),
        "gateways": results,
        "timestamp": time.time()
    })


@route("POST", "/api/gateways/ping-all")
def handle_gateways_ping(handler, query, tail):
    results = _probe_all()
    healthy_count = sum(1 for r in results if r["status"] == "HEALTHY")
    emit_event("gateways.probed", {"healthy": healthy_count, "total": len(results)})
    send_json(handler, 200, {
        "success": True,
        "healthy_count": healthy_count,
        "total_count": len(results),
        "gateways": results,
        "timestamp": time.time()
    })


@route("GET", "/api/gateways/routing-matrix")
def handle_gateways_routing(handler, query, tail):
    send_json(handler, 200, {
        "success": True,
        "failover_chain": [
            {"tier": 0, "name": "Exact-Match Cache", "target": "Redis :6379", "cost": "$0.00", "latency": "< 2ms"},
            {"tier": 1, "name": "Primary Cognitive Tier", "target": "LiteLLM (DeepSeek R1 / Gemini)", "cost": "Metered", "latency": "~500ms"},
            {"tier": 2, "name": "Multi-Agent Swarm Tier", "target": "OpenClaw :18789", "cost": "Autonomous", "latency": "~800ms"},
            {"tier": 3, "name": "Local Offline Backup", "target": "Ollama :11434", "cost": "$0.00", "latency": "~2000ms"}
        ]
    })
