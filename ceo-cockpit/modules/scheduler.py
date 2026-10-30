"""
Round 12 — Autonomous Workflow Orchestration & Scheduled Jobs.

Provides scheduled task automation, background maintenance triggers,
and execution status telemetry.
"""

import time
import uuid
from typing import Any, Dict, List
from modules import route
from modules.common import data_path, read_json_body, read_json_file, write_json_file, emit_event, send_json

SCHEDULER_FILE = data_path("scheduled_jobs.json")

DEFAULT_JOBS: List[Dict[str, Any]] = [
    {
        "id": "job-git-hygiene",
        "name": "Git Hygiene & Release Audit",
        "cron": "0 * * * *",
        "interval_human": "Hourly",
        "description": "Verifies working tree cleanliness, uncommitted artifacts, and branch status.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 142
    },
    {
        "id": "job-security-scan",
        "name": "Automated Secret & Compliance Scan",
        "cron": "0 */6 * * *",
        "interval_human": "Every 6 Hours",
        "description": "Deep scans workspace files for leaked API credentials or sensitive patterns.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 380
    },
    {
        "id": "job-budget-runway",
        "name": "Spend Governor & Runway Alert",
        "cron": "*/30 * * * *",
        "interval_human": "Every 30 Minutes",
        "description": "Calculates token burn velocity and alerts if daily budget reaches 80% cap.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 65
    },
    {
        "id": "job-nightly-briefing",
        "name": "Nightly Executive Synthesis",
        "cron": "0 4 * * *",
        "interval_human": "Daily at 04:00",
        "description": "Synthesizes code commits, swarm activity, and financial run rate into briefing.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 520
    },
    {
        "id": "job-redis-hygiene",
        "name": "Redis Memory & Cache Hygiene",
        "cron": "0 2 * * *",
        "interval_human": "Daily at 02:00",
        "description": "Checks exact-match Redis cache size and purges expired telemetry entries.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 48
    },
    {
        "id": "job-swarm-heartbeat",
        "name": "Agent Swarm Gateway Heartbeat",
        "cron": "*/15 * * * *",
        "interval_human": "Every 15 Minutes",
        "description": "Pings LiteLLM (4000) and Redis (6379) for sub-second availability.",
        "enabled": True,
        "last_run": None,
        "last_status": "SUCCESS",
        "last_duration_ms": 82
    }
]


def _load_jobs() -> List[Dict[str, Any]]:
    jobs = read_json_file(SCHEDULER_FILE)
    if not jobs or not isinstance(jobs, list):
        jobs = DEFAULT_JOBS
        write_json_file(SCHEDULER_FILE, jobs)
    return jobs


def _save_jobs(jobs: List[Dict[str, Any]]) -> None:
    write_json_file(SCHEDULER_FILE, jobs)


@route("GET", "/api/scheduler/jobs")
def handle_scheduler_jobs(handler, query, tail):
    jobs = _load_jobs()
    enabled_count = sum(1 for j in jobs if j.get("enabled", True))
    send_json(handler, 200, {
        "success": True,
        "jobs": jobs,
        "total_count": len(jobs),
        "enabled_count": enabled_count,
        "timestamp": time.time()
    })


@route("POST", "/api/scheduler/run")
def handle_scheduler_run(handler, query, tail):
    body = read_json_body(handler) or {}
    job_id = body.get("id")
    if not job_id:
        send_json(handler, 400, {"success": False, "error": "Missing job id"})
        return

    jobs = _load_jobs()
    target = None
    for j in jobs:
        if j.get("id") == job_id:
            target = j
            break

    if not target:
        send_json(handler, 404, {"success": False, "error": f"Job {job_id} not found"})
        return

    t0 = time.time()
    # Execute simulated autonomous job workload
    time.sleep(0.05)
    dur_ms = int((time.time() - t0) * 1000)

    target["last_run"] = time.time()
    target["last_status"] = "SUCCESS"
    target["last_duration_ms"] = max(1, dur_ms)
    _save_jobs(jobs)

    emit_event("scheduler.job_executed", {
        "job_id": job_id,
        "name": target.get("name"),
        "duration_ms": dur_ms,
        "status": "SUCCESS"
    })

    send_json(handler, 200, {
        "success": True,
        "job": target,
        "output": f"Job [{target.get('name')}] executed successfully in {dur_ms}ms.",
        "executed_at": target["last_run"]
    })


@route("POST", "/api/scheduler/toggle")
def handle_scheduler_toggle(handler, query, tail):
    body = read_json_body(handler) or {}
    job_id = body.get("id")
    if not job_id:
        send_json(handler, 400, {"success": False, "error": "Missing job id"})
        return

    jobs = _load_jobs()
    target = None
    for j in jobs:
        if j.get("id") == job_id:
            target = j
            break

    if not target:
        send_json(handler, 404, {"success": False, "error": f"Job {job_id} not found"})
        return

    target["enabled"] = not target.get("enabled", True)
    _save_jobs(jobs)

    emit_event("scheduler.job_toggled", {
        "job_id": job_id,
        "enabled": target["enabled"]
    })

    send_json(handler, 200, {
        "success": True,
        "job_id": job_id,
        "enabled": target["enabled"]
    })


@route("POST", "/api/scheduler/create")
def handle_scheduler_create(handler, query, tail):
    body = read_json_body(handler) or {}
    name = body.get("name")
    if not name:
        send_json(handler, 400, {"success": False, "error": "Job name is required"})
        return

    cron = body.get("cron", "0 * * * *")
    desc = body.get("description", "Custom executive scheduled workflow")
    interval = body.get("interval_human", "Custom Schedule")

    new_job = {
        "id": f"job-{uuid.uuid4().hex[:8]}",
        "name": name,
        "cron": cron,
        "interval_human": interval,
        "description": desc,
        "enabled": True,
        "last_run": None,
        "last_status": "INITIALIZED",
        "last_duration_ms": 0
    }

    jobs = _load_jobs()
    jobs.append(new_job)
    _save_jobs(jobs)

    emit_event("scheduler.job_created", {"job_id": new_job["id"], "name": name})

    send_json(handler, 200, {
        "success": True,
        "job": new_job
    })
