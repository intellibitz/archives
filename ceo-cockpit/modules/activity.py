"""
Round 9 — Real-Time Activity Event Stream & Audit Trail.

Consolidated operational event bus aggregating multi-agent turns,
runbook executions, safety barriers, financial trips, and auto-clicker
approvals into a live SSE stream and persistent audit ledger.
"""

import time
from typing import Any, Dict, List

from modules import route
from modules.common import (ACTIVITY_FILE, EVENT_SUBSCRIBERS,
                            _event_lock, append_jsonl, data_path,
                            log, read_jsonl, send_json, start_sse, write_sse)


@route("GET", "/api/activity/recent")
def get_recent_activity(h, query, tail):
    limit = int(query.get("limit", ["50"])[0])
    severity_filter = query.get("severity", [""])[0].lower()

    events = read_jsonl(ACTIVITY_FILE, limit=limit * 2)
    if severity_filter:
        events = [e for e in events if e.get("level", "").lower() == severity_filter]

    send_json(h, 200, {
        "success": True,
        "count": len(events[-limit:]),
        "events": events[-limit:]
    })


@route("GET", "/api/activity/stream")
def get_activity_stream(h, query, tail):
    """Streams live events over SSE to connected clients."""
    start_sse(h)

    # Initial snapshot
    recent = read_jsonl(ACTIVITY_FILE, limit=10)
    if not write_sse(h, {"type": "snapshot", "events": recent}):
        return

    import queue
    q: queue.Queue = queue.Queue(maxsize=100)

    with _event_lock:
        EVENT_SUBSCRIBERS.append(q)

    try:
        while True:
            try:
                evt = q.get(timeout=15.0)
                if not write_sse(h, {"type": "event", "event": evt}):
                    break
            except queue.Empty:
                if not write_sse(h, {"type": "heartbeat", "time": time.time()}):
                    break
    finally:
        with _event_lock:
            if q in EVENT_SUBSCRIBERS:
                EVENT_SUBSCRIBERS.remove(q)
        try:
            h.close_connection = True
        except Exception:
            pass
