"""
Round 6 — Strategic Executive Playbooks & Prompt Blueprints.

Curated, board-level technical and operational playbooks designed to execute
end-to-end strategic assessments, architectural reviews, security audits,
and release planning with workspace context injection.
"""

import json
import time
import uuid
from typing import Any, Dict, List, Optional

from modules import route
from modules.common import (active_workspace, append_jsonl, data_path,
                            emit_event, llm_complete, log, read_json_body,
                            read_jsonl, send_json)

PLAYBOOKS_LOG = data_path("playbooks_history.jsonl")

CURATED_PLAYBOOKS: List[Dict[str, Any]] = [
    {
        "id": "sec-defense-audit",
        "title": "Zero-Trust Security & Defense Audit",
        "category": "Security & Governance",
        "icon": "🛡️",
        "model": "deepseek-reasoner",
        "desc": "Audits access boundaries, credential leakage risks, elevated privileges, and sensitive assets.",
        "template": (
            "Perform a rigorous Zero-Trust Security & Defense Audit for the active workspace: {workspace}.\n\n"
            "Focus Areas:\n"
            "1. Credential & Secret Protection: Inspect for exposed API keys, tokens, or plaintext credentials.\n"
            "2. Destructive Command Gates: Verify barriers against irreversible file/system deletions.\n"
            "3. Privilege Boundaries: Audit system/administrator elevation requirements and privilege separation.\n"
            "4. Attack Surface: Evaluate HTTP endpoints, exposed sockets, and CORS configurations.\n\n"
            "Deliver an executive-level risk matrix with Severity, Risk Description, and Immediate Remediation Actions."
        )
    },
    {
        "id": "arch-modernization",
        "title": "System Architecture & Decoupling RFC",
        "category": "Architecture & Engineering",
        "icon": "🏛️",
        "model": "deepseek-reasoner",
        "desc": "Reviews system boundaries, modular extension points, contract stability, and technical debt.",
        "template": (
            "Formulate an Architectural RFC and Decoupling Plan for workspace: {workspace}.\n\n"
            "Strategic Assessment:\n"
            "1. Architectural Cohesion: Evaluate separation of concerns between core server, modules, and web client.\n"
            "2. Contract Stability: Verify public REST/SSE API contract stability across version upgrades.\n"
            "3. Extensibility: Outline how new capabilities can be hot-loaded without modifying core monoliths.\n"
            "4. Scalability & Resilience: Identify potential memory leaks, thread contention, or unhandled exceptions.\n\n"
            "Provide a concrete 3-phase Modernization Blueprint for executive alignment."
        )
    },
    {
        "id": "perf-cache-tuning",
        "title": "Latency & Cache Optimization Sprint",
        "category": "Performance & Scale",
        "icon": "⚡",
        "model": "deepseek-chat",
        "desc": "Analyzes TTFT, Redis caching strategies, subprocess overhead, and response streaming efficiency.",
        "template": (
            "Develop a Performance Tuning & Cache Optimization Strategy for workspace: {workspace}.\n\n"
            "Analysis Dimensions:\n"
            "1. Cache Hit Acceleration: Maximize Redis key hit rates for repeated prompts and analytical summaries.\n"
            "2. Subprocess Overhead: Minimize process-spawning latency during tool execution and telemetry collection.\n"
            "3. Memory Footprint: Assess bounded deques, log file rotation, and client DOM node limits.\n"
            "4. Time-to-First-Token (TTFT): Recommend routing strategies between DeepSeek (high reasoning) and Gemini (fast synthesis).\n\n"
            "Summarize projected latency reductions and measurable throughput gains."
        )
    },
    {
        "id": "git-hygiene-release",
        "title": "Git Hygiene & Atomic Release Plan",
        "category": "Operations & Release",
        "icon": "🚀",
        "model": "gemini-flash",
        "desc": "Reviews working copy state, uncommitted changes, commit structure, and semantic tagging.",
        "template": (
            "Create an Executive Release Verification Plan for workspace: {workspace}.\n\n"
            "Verification Checkpoints:\n"
            "1. Working Tree Cleanliness: Audit untracked scratch files, temp artifacts, and test logs.\n"
            "2. Atomic Semantic Commits: Review recent commit history for adherence to conventional commit standards.\n"
            "3. Verification Gates: Outline mandatory automated smoke tests before release sign-off.\n"
            "4. Release Changelog: Draft an executive release bulletin highlighting functional enhancements and business value.\n\n"
            "Conclude with a go/no-go recommendation for the CEO."
        )
    },
    {
        "id": "cost-governance-audit",
        "title": "Token Spend & Budget Governor Review",
        "category": "Financial & Spend",
        "icon": "💰",
        "model": "gemini-flash",
        "desc": "Audits daily/monthly API burn rates, circuit breaker trips, and model cost efficiencies.",
        "template": (
            "Conduct a Token Spend & Financial Governance Review for workspace: {workspace}.\n\n"
            "Financial Auditing Dimensions:\n"
            "1. Daily / Monthly Burn Analysis: Evaluate current expenditure against configured budget caps.\n"
            "2. Model Selection ROI: Calculate cost savings achieved by delegating broad synthesis to Gemini Flash vs DeepSeek.\n"
            "3. Cache Cost Elimination: Quantify API dollars saved via local Redis caching.\n"
            "4. Governor Policies: Recommend tightening or expanding daily circuit breaker limits based on operational velocity.\n\n"
            "Provide executive recommendations for financial efficiency."
        )
    }
]


@route("GET", "/api/playbooks")
def get_playbooks(h, query, tail):
    send_json(h, 200, {
        "success": True,
        "workspace": active_workspace(),
        "playbooks": CURATED_PLAYBOOKS
    })


@route("POST", "/api/playbooks/run")
def post_run_playbook(h, query, tail):
    req = read_json_body(h)
    pb_id = req.get("playbook_id", "").strip()
    custom_params = req.get("parameters", {})

    matched = next((p for p in CURATED_PLAYBOOKS if p["id"] == pb_id), None)
    if not matched:
        send_json(h, 404, {"success": False, "error": f"Playbook '{pb_id}' not found"})
        return

    ws = active_workspace()
    prompt = matched["template"].format(workspace=ws, **custom_params)

    emit_event("playbooks", f"Executing Playbook: {matched['title']}",
               f"Model: {matched['model']} | Workspace: {ws}", "info",
               {"playbook_id": pb_id})

    t0 = time.time()
    result = llm_complete(
        model=matched["model"],
        prompt=prompt,
        system_prompt=f"You are the Executive Chief of Staff executing the {matched['title']} playbook.",
        temperature=0.2,
        max_tokens=2500
    )
    elapsed_ms = int((time.time() - t0) * 1000)

    run_record = {
        "id": f"pb_{uuid.uuid4().hex[:10]}",
        "playbook_id": pb_id,
        "title": matched["title"],
        "icon": matched["icon"],
        "category": matched["category"],
        "workspace": ws,
        "timestamp": time.time(),
        "duration_ms": elapsed_ms,
        "model": matched["model"],
        "report": result.get("reply", "Playbook execution completed."),
        "cached": result.get("cached", False),
        "cost_usd": result.get("cost_usd", 0.0)
    }

    append_jsonl(PLAYBOOKS_LOG, run_record, max_lines=200)

    send_json(h, 200, {
        "success": True,
        "run": run_record
    })


@route("GET", "/api/playbooks/history")
def get_playbooks_history(h, query, tail):
    limit = int(query.get("limit", ["10"])[0])
    history = read_jsonl(PLAYBOOKS_LOG, limit=limit)
    send_json(h, 200, {"success": True, "history": history})
