"""
Round 4 — Multi-Agent Swarm Mission Control.

Manages autonomous multi-agent swarms across LiteLLM (Port 4000). Provides topology discovery, role specialization,
and concurrent swarm directive orchestration with executive synthesis.
"""

import concurrent.futures
import json
import os
import time
import uuid
from typing import Any, Dict, List, Optional

from modules import route
from modules.common import (active_workspace, append_jsonl, data_path,
                            emit_event, llm_complete, log, read_json_body,
                            read_jsonl, send_json, start_sse, write_sse)

SWARM_LOG_FILE = data_path("swarm_history.jsonl")

# Standard Swarm Archetypes & Roles
AGENT_ROLES: List[Dict[str, Any]] = [
    {
        "id": "arch-lead",
        "name": "System Architect",
        "icon": "🏛️",
        "role": "Architecture & System Design",
        "model": "deepseek-reasoner",
        "effort": "high",
        "system_prompt": (
            "You are the Principal Systems Architect. Analyze the objective for modularity, "
            "contract stability, scalability, and clean system boundaries. Provide concrete architectural directives."
        ),
        "status": "online"
    },
    {
        "id": "sec-auditor",
        "name": "Security Sentinel",
        "icon": "🛡️",
        "role": "Security & Defense Audit",
        "model": "deepseek-reasoner",
        "effort": "high",
        "system_prompt": (
            "You are the Chief Information Security Officer & Defense Auditor. Inspect the proposal "
            "for data leakage, injection risks, destructive operations, privilege elevation issues, and secret exposure."
        ),
        "status": "online"
    },
    {
        "id": "eng-coder",
        "name": "Lead Implementation Engineer",
        "icon": "⚡",
        "role": "Code Synthesis & Engineering",
        "model": "deepseek-chat",
        "effort": "max",
        "system_prompt": (
            "You are the Principal Software Engineer. Provide high-density, reliable implementation plans, "
            "concrete technical steps, and file modifications needed to achieve the directive flawlessly."
        ),
        "status": "online"
    },
    {
        "id": "qa-evaluator",
        "name": "Quality & Verification Evaluator",
        "icon": "🧪",
        "role": "Verification & Quality Gates",
        "model": "gemini-flash",
        "effort": "medium",
        "system_prompt": (
            "You are the Quality Director. Outline automated verification suites, smoke tests, "
            "edge-case scenarios, and empirical proof required to confirm zero regression."
        ),
        "status": "online"
    },
    {
        "id": "ops-director",
        "name": "Operations & Release Commander",
        "icon": "🚀",
        "role": "Deployment & Observability",
        "model": "gemini-flash",
        "effort": "medium",
        "system_prompt": (
            "You are the VP of Infrastructure and Site Reliability. Focus on operational health, "
            "service uptime, logging, telemetry, rollback strategies, and zero-downtime execution."
        ),
        "status": "online"
    }
]


def get_swarm_topology() -> Dict[str, Any]:
    """Returns active swarm topology, agent statuses, and capabilities."""
    ws = active_workspace()
    return {
        "success": True,
        "swarm_name": "Antigravity CEO Council Swarm",
        "workspace": ws,
        "agents": AGENT_ROLES,
        "active_threads": 0,
        "supported_strategies": ["parallel-council", "pipeline-staged", "adversarial-review"]
    }


def execute_agent_perspective(agent: Dict[str, Any], prompt: str, workspace: str) -> Dict[str, Any]:
    """Executes a single agent's strategic analysis."""
    t0 = time.time()
    agent_id = agent["id"]
    model = agent["model"]
    system_prompt = agent["system_prompt"]

    full_prompt = (
        f"Context Workspace: {workspace}\n"
        f"Role: {agent['name']} ({agent['role']})\n\n"
        f"Executive Directive:\n{prompt}\n\n"
        f"Provide your specialized executive analysis and recommendations concisely."
    )

    result = llm_complete(
        model=model,
        prompt=full_prompt,
        system_prompt=system_prompt,
        temperature=0.2,
        max_tokens=1500
    )

    elapsed_ms = int((time.time() - t0) * 1000)
    return {
        "agent_id": agent_id,
        "name": agent["name"],
        "icon": agent["icon"],
        "role": agent["role"],
        "model": model,
        "reply": result.get("reply", "No response generated."),
        "cached": result.get("cached", False),
        "cost_usd": result.get("cost_usd", 0.0),
        "latency_ms": elapsed_ms,
        "error": result.get("error")
    }


def synthesize_swarm_findings(prompt: str, agent_results: List[Dict[str, Any]]) -> str:
    """Synthesizes all individual agent perspectives into an executive consensus."""
    combined_inputs = []
    for res in agent_results:
        combined_inputs.append(f"### Perspective: {res['name']} ({res['role']})\n{res['reply']}\n")

    synthesis_prompt = (
        f"Executive Directive: {prompt}\n\n"
        f"Specialized Agent Inputs:\n{''.join(combined_inputs)}\n\n"
        "Synthesize these specialized findings into a unified, board-level Executive Summary. "
        "Highlight: (1) Strategic Consensus, (2) Critical Risks & Mitigations, (3) Recommended Action Plan."
    )

    syn = llm_complete(
        model="deepseek-reasoner",
        prompt=synthesis_prompt,
        system_prompt="You are the Executive Chief of Staff synthesizing multi-agent findings for the CEO.",
        temperature=0.15,
        max_tokens=2000
    )
    return syn.get("reply", "Executive synthesis complete.")


# ------------------------------------------------------------------------------
# Swarm HTTP Routes
# ------------------------------------------------------------------------------
@route("GET", "/api/swarm/topology")
def get_topology(h, query, tail):
    send_json(h, 200, get_swarm_topology())


@route("POST", "/api/swarm/dispatch")
def post_dispatch(h, query, tail):
    """
    Dispatches an executive directive to the multi-agent swarm.
    Streams live progress of each agent via SSE, followed by synthesized consensus.
    """
    req = read_json_body(h)
    prompt = req.get("prompt", "").strip()
    if not prompt:
        send_json(h, 400, {"success": False, "error": "Prompt is required"})
        return

    selected_ids = req.get("agent_ids") or [a["id"] for a in AGENT_ROLES]
    active_agents = [a for a in AGENT_ROLES if a["id"] in selected_ids]
    if not active_agents:
        send_json(h, 400, {"success": False, "error": "No valid agents selected"})
        return

    swarm_run_id = f"swarm_{uuid.uuid4().hex[:10]}"
    ws = active_workspace()

    emit_event("swarm", f"Swarm Dispatch: {prompt[:60]}...",
               f"Running {len(active_agents)} concurrent agent roles", "info",
               {"swarm_run_id": swarm_run_id, "agent_count": len(active_agents)})

    start_sse(h)
    write_sse(h, {
        "type": "swarm_start",
        "swarm_run_id": swarm_run_id,
        "agents": [{"id": a["id"], "name": a["name"], "icon": a["icon"], "role": a["role"]} for a in active_agents],
        "prompt": prompt
    })

    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=len(active_agents)) as executor:
        future_map = {
            executor.submit(execute_agent_perspective, agent, prompt, ws): agent
            for agent in active_agents
        }

        for future in concurrent.futures.as_completed(future_map):
            agent = future_map[future]
            try:
                agent_res = future.result()
                results.append(agent_res)
                write_sse(h, {
                    "type": "agent_complete",
                    "swarm_run_id": swarm_run_id,
                    "result": agent_res
                })
            except Exception as e:
                err_res = {
                    "agent_id": agent["id"],
                    "name": agent["name"],
                    "icon": agent["icon"],
                    "role": agent["role"],
                    "error": str(e),
                    "reply": f"Agent evaluation failed: {e}"
                }
                results.append(err_res)
                write_sse(h, {
                    "type": "agent_error",
                    "swarm_run_id": swarm_run_id,
                    "result": err_res
                })

    # Generate unified executive consensus
    write_sse(h, {"type": "synthesizing", "swarm_run_id": swarm_run_id})
    synthesis = synthesize_swarm_findings(prompt, results)

    record = {
        "id": swarm_run_id,
        "timestamp": time.time(),
        "prompt": prompt,
        "agent_count": len(active_agents),
        "results": results,
        "synthesis": synthesis
    }
    append_jsonl(SWARM_LOG_FILE, record, max_lines=200)

    write_sse(h, {
        "type": "swarm_exit",
        "swarm_run_id": swarm_run_id,
        "synthesis": synthesis,
        "record": record
    })

    try:
        h.close_connection = True
    except Exception:
        pass


@route("GET", "/api/swarm/history")
def get_swarm_history(h, query, tail):
    limit = int(query.get("limit", ["10"])[0])
    history = read_jsonl(SWARM_LOG_FILE, limit=limit)
    send_json(h, 200, {"success": True, "history": history})
