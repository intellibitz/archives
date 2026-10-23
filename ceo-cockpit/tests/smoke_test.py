#!/usr/bin/env python3
"""
CEO Cockpit end-to-end API smoke suite.

Runs against the live service (default http://127.0.0.1:8080). Each round adds
checks here; the suite gates every commit. Exit code 0 = all checks passed.

Usage:  python tests/smoke_test.py [--base http://127.0.0.1:8080] [--only ops]
"""

import argparse
import http.client
import json
import sys
import time
import urllib.parse
from datetime import datetime

BASE = "http://127.0.0.1:8080"
RESULTS = []


def stamp() -> str:
    return datetime.now().strftime("%H:%M:%S")


def request(method, path, body=None, headers=None, timeout=60, raw=False):
    u = urllib.parse.urlparse(BASE)
    conn = http.client.HTTPConnection(u.hostname, u.port or 80, timeout=timeout)
    hdrs = {"Content-Type": "application/json"} if body is not None else {}
    hdrs.update(headers or {})
    conn.request(method, path, body=json.dumps(body) if body is not None else None, headers=hdrs)
    resp = conn.getresponse()
    data = resp.read().decode("utf-8", errors="replace")
    conn.close()
    if raw:
        return resp.status, data, resp
    try:
        return resp.status, json.loads(data) if data else {}
    except json.JSONDecodeError:
        return resp.status, {"_raw": data[:500]}


def read_sse(path, body=None, timeout=30):
    u = urllib.parse.urlparse(BASE)
    conn = http.client.HTTPConnection(u.hostname, u.port or 80, timeout=timeout)
    conn.request("POST", path, body=json.dumps(body) if body else None,
                 headers={"Content-Type": "application/json"})
    resp = conn.getresponse()
    if resp.status != 200:
        data = resp.read().decode("utf-8", errors="replace")
        conn.close()
        return resp.status, []
    events = []
    start = time.time()
    while time.time() - start < timeout:
        line = resp.fp.readline()
        if not line:
            break
        decoded = line.decode("utf-8", errors="replace").strip()
        if decoded.startswith("data: "):
            try:
                evt = json.loads(decoded[6:])
                events.append(evt)
                if evt.get("type") == "exit":
                    break
            except Exception:
                pass
    conn.close()
    return resp.status, events


def sse_events(text):
    out = []
    for frame in text.split("\n\n"):
        for line in frame.split("\n"):
            if line.startswith("data: "):
                try:
                    out.append(json.loads(line[6:]))
                except json.JSONDecodeError:
                    pass
    return out


def check(group):
    def deco(fn):
        fn._group = group
        TESTS.append(fn)
        return fn
    return deco


TESTS = []


# ------------------------------------------------------------------ core
@check("core")
def t_index_served():
    s, body, _ = request("GET", "/", raw=True)
    assert s == 200 and "Command Deck" in body, f"index status={s}"


@check("core")
def t_health():
    s, d = request("GET", "/api/health")
    assert s == 200, f"status={s}"


@check("core")
def t_deck_assets():
    for p in ("/deck.css", "/modules/deck.js", "/modules/ops.js"):
        s, _, _ = request("GET", p, raw=True)
        assert s == 200, f"{p} -> {s}"


# ------------------------------------------------------------- budget & pricing
@check("budget")
def t_models_catalog_with_pricing():
    s, d = request("GET", "/api/models")
    assert s == 200, f"status={s} body={d}"
    assert "models" in d and len(d["models"]) > 0, "no models returned"
    assert "total_count" in d and "active_count" in d and "inactive_count" in d, "missing count telemetry"
    for m in d["models"]:
        assert "id" in m and "name" in m, f"malformed model entry: {m}"
        assert m.get("status") in ("active", "inactive"), f"invalid status in model: {m}"
        assert "prompt_price_per_m" in m and "completion_price_per_m" in m, f"missing pricing fields in: {m}"
        assert "cost_tier" in m, f"missing cost_tier in: {m}"


@check("budget")
def t_models_pricing_matrix():
    s, d = request("GET", "/api/models/pricing")
    assert s == 200, f"status={s} body={d}"
    assert "models" in d and "budget" in d, "missing models or budget in pricing matrix"
    b = d["budget"]
    assert "daily_budget_usd" in b and "remaining_daily_usd" in b, "missing daily budget fields"
    assert "circuit_breaker_state" in b, "missing circuit breaker state"
    assert d.get("count", 0) >= len(d["models"]), "pricing count mismatch"


@check("budget")
def t_budget_governor_telemetry():
    s, d = request("GET", "/api/budget")
    assert s == 200, f"status={s} body={d}"
    assert "daily_budget_usd" in d and "today_spend_usd" in d, "missing budget fields"
    assert "circuit_breaker_state" in d, "missing circuit breaker state"


# -------------------------------------------------------------- security
@check("security")
def t_foreign_origin_rejected():
    s, d = request("POST", "/api/ops/assess", {"command": "git status"}, {"Origin": "https://evil.example"})
    assert s == 403, f"expected 403 got {s}"


@check("security")
def t_foreign_host_rejected():
    s, d = request("GET", "/api/health", headers={"Host": "attacker.example:8080"})
    assert s == 403, f"expected 403 got {s}"


@check("security")
def t_same_origin_allowed():
    s, d = request("POST", "/api/ops/assess", {"command": "git status"}, {"Origin": BASE})
    assert s == 200 and d.get("allowed") is True, f"status={s} body={d}"


# ------------------------------------------------------------------- ops
@check("ops")
def t_runbooks_listed():
    s, d = request("GET", "/api/ops/runbooks")
    assert s == 200 and len(d.get("runbooks", [])) >= 10, f"status={s} count={len(d.get('runbooks', []))}"


@check("ops")
def t_destructive_patterns_blocked():
    for cmd in ("git push --force origin main", "Remove-Item -Recurse -Force C:\\x", "rm -rf /",
                "git reset --hard HEAD~3", "DROP TABLE users", "Stop-Service ceo-cockpit", "format C:"):
        s, d = request("POST", "/api/ops/assess", {"command": cmd})
        assert s == 200 and d.get("allowed") is False, f"NOT blocked: {cmd}"


@check("ops")
def t_blocked_run_returns_403():
    s, d = request("POST", "/api/ops/run", {"command": "git clean -fdx"})
    assert s == 403 and d.get("blocked"), f"status={s}"


@check("ops")
def t_runbook_streams_and_succeeds():
    s, events = read_sse("/api/ops/run", {"runbook_id": "git-status"}, timeout=30)
    exits = [e for e in events if e.get("type") == "exit"]
    assert s == 200 and exits, f"no exit event (status={s}, events={len(events)})"
    job = exits[-1]["job"]
    assert job["status"] == "succeeded", f"job status={job['status']} exit={job['exit_code']}"
    lines = [l for e in events if e.get("type") == "lines" for l in e["lines"]]
    assert any("##" in l for l in lines), "git status -sb header missing"


@check("ops")
def t_custom_command_and_history():
    s, events = read_sse("/api/ops/run", {"command": "Write-Output 'cockpit-smoke-ok'"}, timeout=15)
    lines = [l for e in events if e.get("type") == "lines" for l in e["lines"]]
    assert "cockpit-smoke-ok" in lines, f"output missing: {lines[:5]}"
    job_id = next(e["job"]["id"] for e in events if e.get("type") == "exit")
    s, d = request("GET", f"/api/ops/jobs/{job_id}")
    assert s == 200 and "cockpit-smoke-ok" in d.get("output", []), "job detail missing output"
    s, d = request("GET", "/api/ops/history?limit=5")
    assert s == 200 and any(r["id"] == job_id for r in d.get("history", [])), "history missing job"


@check("ops")
def t_timeout_enforced():
    s, events = read_sse("/api/ops/run", {"command": "Start-Sleep -Seconds 30", "timeout": 3}, timeout=15)
    job = next(e["job"] for e in events if e.get("type") == "exit")
    assert job["status"] == "timeout" and job["duration_ms"] < 15000, f"status={job['status']} {job['duration_ms']}ms"


# ------------------------------------------------------------------- swarm
@check("swarm")
def t_swarm_topology_listed():
    s, d = request("GET", "/api/swarm/topology")
    assert s == 200 and len(d.get("agents", [])) >= 5, f"status={s} agents={len(d.get('agents', []))}"
    assert "Parallel Council" in str(d) or "parallel-council" in str(d), "missing council strategy"


@check("swarm")
def t_swarm_assets():
    s, _, _ = request("GET", "/modules/swarm.js", raw=True)
    assert s == 200, f"swarm.js status={s}"


@check("swarm")
def t_swarm_history():
    s, d = request("GET", "/api/swarm/history?limit=5")
    assert s == 200 and "history" in d, f"status={s}"


# ------------------------------------------------------------------- voice
@check("voice")
def t_voice_assets():
    s, _, _ = request("GET", "/modules/voice.js", raw=True)
    assert s == 200, f"voice.js status={s}"


# --------------------------------------------------------------- playbooks
@check("playbooks")
def t_playbooks_catalog_listed():
    s, d = request("GET", "/api/playbooks")
    assert s == 200 and len(d.get("playbooks", [])) >= 5, f"status={s} playbooks={len(d.get('playbooks', []))}"
    titles = [p["title"] for p in d.get("playbooks", [])]
    assert any("Security" in t for t in titles), "Security playbook missing"


@check("playbooks")
def t_playbooks_assets():
    s, _, _ = request("GET", "/modules/playbooks.js", raw=True)
    assert s == 200, f"playbooks.js status={s}"


@check("playbooks")
def t_playbooks_history():
    s, d = request("GET", "/api/playbooks/history?limit=5")
    assert s == 200 and "history" in d, f"status={s}"


# -------------------------------------------------------------- analytics
@check("analytics")
def t_analytics_summary():
    s, d = request("GET", "/api/analytics/summary")
    assert s == 200 and "financials" in d and "performance" in d, f"status={s} body={d}"
    assert "daily_burn_usd" in d["financials"], "missing daily_burn_usd"


@check("analytics")
def t_analytics_ledger():
    s, d = request("GET", "/api/analytics/ledger?limit=10")
    assert s == 200 and "records" in d, f"status={s}"


@check("analytics")
def t_analytics_assets():
    s, _, _ = request("GET", "/modules/analytics.js", raw=True)
    assert s == 200, f"analytics.js status={s}"


# ------------------------------------------------------------------- git
@check("git")
def t_git_status():
    s, d = request("GET", "/api/git/status")
    assert s == 200 and d.get("is_repo") is True, f"status={s} body={d}"
    assert "branch" in d and "recent_commits" in d, "missing branch or commits"


@check("git")
def t_git_diff():
    s, d = request("GET", "/api/git/diff")
    assert s == 200 and "diff" in d, f"status={s}"


@check("git")
def t_git_assets():
    s, _, _ = request("GET", "/modules/git_ops.js", raw=True)
    assert s == 200, f"git_ops.js status={s}"


# ------------------------------------------------------------- activity
@check("activity")
def t_activity_recent():
    s, d = request("GET", "/api/activity/recent?limit=10")
    assert s == 200 and "events" in d, f"status={s}"


@check("activity")
def t_activity_assets():
    s, _, _ = request("GET", "/modules/activity.js", raw=True)
    assert s == 200, f"activity.js status={s}"


# --------------------------------------------------------------- system
@check("system")
def t_system_manifest():
    s, d = request("GET", "/api/system/manifest")
    assert s == 200 and d.get("success") is True, f"manifest status={s} body={d}"
    assert d.get("roadmap", {}).get("completed_rounds") == 20, "20 rounds not reported"
    assert len(d.get("modules", [])) >= 15, "missing modules in manifest"
    assert "registered_routes" in d and d["registered_routes"]["count"] >= 30, "missing routes"


@check("system")
def t_system_health():
    s, d = request("GET", "/api/system/health")
    assert s == 200 and d.get("success") is True, f"health status={s}"
    assert "subsystems" in d, "missing subsystems in health check"


@check("system")
def t_system_ping():
    s, d = request("POST", "/api/system/ping", body={})
    assert s == 200 and d.get("pong") is True, f"ping status={s}"


@check("system")
def t_system_deck_assets():
    s, content, _ = request("GET", "/modules/deck.js", raw=True)
    assert s == 200, f"deck.js status={s}"
    assert "registerView" in content and "registerPanel" in content, "registerView missing in deck.js"


# ----------------------------------------------------------- forecaster
@check("forecaster")
def t_forecast_runway():
    s, d = request("GET", "/api/forecast/runway")
    assert s == 200 and "runway" in d, f"runway status={s}"
    assert "hours_to_daily_cap" in d["runway"], "missing hours_to_daily_cap"


@check("forecaster")
def t_forecast_simulate():
    s, d = request("POST", "/api/forecast/simulate", body={"model": "deepseek-reasoner", "queries_per_day": 120})
    assert s == 200 and "simulation" in d, f"simulation status={s}"
    assert d["simulation"]["queries_per_day"] == 120, "queries_per_day mismatch"


@check("forecaster")
def t_forecast_assets():
    s, _, _ = request("GET", "/modules/forecaster.js", raw=True)
    assert s == 200, f"forecaster.js status={s}"


# ------------------------------------------------------------ scheduler
@check("scheduler")
def t_scheduler_jobs():
    s, d = request("GET", "/api/scheduler/jobs")
    assert s == 200 and "jobs" in d, f"jobs status={s}"
    assert len(d["jobs"]) >= 5, "default jobs missing"


@check("scheduler")
def t_scheduler_run():
    s, d = request("POST", "/api/scheduler/run", body={"id": "job-git-hygiene"})
    assert s == 200 and d.get("success") is True, f"scheduler run status={s}"
    assert d.get("job", {}).get("last_status") == "SUCCESS", "job last_status not SUCCESS"


@check("scheduler")
def t_scheduler_toggle():
    s, d = request("POST", "/api/scheduler/toggle", body={"id": "job-git-hygiene"})
    assert s == 200 and "enabled" in d, f"scheduler toggle status={s}"


@check("scheduler")
def t_scheduler_assets():
    s, _, _ = request("GET", "/modules/scheduler.js", raw=True)
    assert s == 200, f"scheduler.js status={s}"


# --------------------------------------------------------------- memory
@check("memory")
def t_memory_stats():
    s, d = request("GET", "/api/memory/stats")
    assert s == 200 and "total_memories" in d, f"memory stats status={s}"


@check("memory")
def t_memory_search():
    s, d = request("GET", "/api/memory/search?q=routing")
    assert s == 200 and "results" in d, f"memory search status={s}"
    assert len(d["results"]) > 0, "no memory search results"


@check("memory")
def t_memory_index():
    s, d = request("POST", "/api/memory/index", body={
        "title": "Smoke Test ADR",
        "category": "Architecture",
        "content": "Automated verification assertion for memory vault."
    })
    assert s == 200 and d.get("success") is True, f"memory index status={s}"


@check("memory")
def t_memory_assets():
    s, _, _ = request("GET", "/modules/memory_vault.js", raw=True)
    assert s == 200, f"memory_vault.js status={s}"


# ----------------------------------------------------------- compliance
@check("compliance")
def t_compliance_scan():
    s, d = request("GET", "/api/compliance/scan")
    assert s == 200 and "compliance" in d, f"compliance scan status={s}"
    assert "score" in d["compliance"] and "grade" in d["compliance"], "missing score/grade"


@check("compliance")
def t_compliance_redact():
    s, d = request("POST", "/api/compliance/redact", body={"text": "User key is sk-123456789012345678901234"})
    assert s == 200 and d.get("matches_found", 0) >= 1, f"compliance redact status={s}"
    assert "[REDACTED_OPENAI_KEY]" in d.get("redacted_text", ""), "redaction text mismatch"


@check("compliance")
def t_compliance_assets():
    s, _, _ = request("GET", "/modules/compliance.js", raw=True)
    assert s == 200, f"compliance.js status={s}"


# ------------------------------------------------------------- gateways
@check("gateways")
def t_gateways_status():
    s, d = request("GET", "/api/gateways/status")
    assert s == 200 and "gateways" in d, f"gateways status={s}"
    assert len(d["gateways"]) == 5, f"expected 5 gateways, got {len(d['gateways'])}"


@check("gateways")
def t_gateways_ping():
    s, d = request("POST", "/api/gateways/ping-all", body={})
    assert s == 200 and "healthy_count" in d, f"gateways ping status={s}"


@check("gateways")
def t_gateways_routing():
    s, d = request("GET", "/api/gateways/routing-matrix")
    assert s == 200 and "failover_chain" in d, f"gateways routing status={s}"


@check("gateways")
def t_gateways_assets():
    s, _, _ = request("GET", "/modules/gateway_matrix.js", raw=True)
    assert s == 200, f"gateway_matrix.js status={s}"


# ------------------------------------------------------------- war_room
@check("war_room")
def t_war_room_incidents():
    s, d = request("GET", "/api/war-room/incidents")
    assert s == 200 and "incidents" in d, f"war room incidents status={s}"


@check("war_room")
def t_war_room_create_and_triage():
    s, d = request("POST", "/api/war-room/create", body={
        "title": "Smoke Test Incident",
        "severity": "P3",
        "affected_component": "Test Suite"
    })
    assert s == 200 and d.get("success") is True, f"war room create status={s}"
    inc_id = d["incident"]["id"]

    s2, d2 = request("POST", "/api/war-room/triage", body={"id": inc_id})
    assert s2 == 200 and d2.get("success") is True, f"war room triage status={s2}"
    assert "Automated RCA" in d2["incident"].get("rca", ""), "RCA generation failed"

    s3, d3 = request("POST", "/api/war-room/resolve", body={"id": inc_id})
    assert s3 == 200 and d3["incident"]["status"] == "RESOLVED", "resolve failed"


@check("war_room")
def t_war_room_assets():
    s, _, _ = request("GET", "/modules/war_room.js", raw=True)
    assert s == 200, f"war_room.js status={s}"


# ------------------------------------------------------------ benchmark
@check("benchmark")
def t_benchmark_history():
    s, d = request("GET", "/api/benchmark/history")
    assert s == 200 and "runs" in d, f"benchmark history status={s}"


@check("benchmark")
def t_benchmark_run_tests():
    s, d = request("POST", "/api/benchmark/run-tests", body={})
    assert s == 200 and d.get("success") is True, f"run-tests status={s}"
    assert d.get("run", {}).get("pass_rate_pct") == 100.0, "pass rate not 100%"


@check("benchmark")
def t_benchmark_leaderboard():
    s, d = request("GET", "/api/benchmark/leaderboard")
    assert s == 200 and "leaderboard" in d, f"leaderboard status={s}"


@check("benchmark")
def t_benchmark_assets():
    s, _, _ = request("GET", "/modules/benchmark.js", raw=True)
    assert s == 200, f"benchmark.js status={s}"


# -------------------------------------------------------------- reports
@check("reports")
def t_reports_templates():
    s, d = request("GET", "/api/reports/templates")
    assert s == 200 and "templates" in d, f"reports templates status={s}"


@check("reports")
def t_reports_generate():
    s, d = request("POST", "/api/reports/generate", body={"template_id": "tpl-board-brief"})
    assert s == 200 and d.get("success") is True, f"reports generate status={s}"
    assert "Board of Directors" in d.get("report", {}).get("markdown", ""), "markdown content mismatch"


@check("reports")
def t_reports_assets():
    s, _, _ = request("GET", "/modules/reports.js", raw=True)
    assert s == 200, f"reports.js status={s}"


# -------------------------------------------------------------- secrets
@check("secrets")
def t_secrets_audit():
    s, d = request("GET", "/api/secrets/audit")
    assert s == 200 and "audit" in d, f"secrets audit status={s}"
    assert "credentials" in d["audit"], "missing credentials list"


@check("secrets")
def t_secrets_verify():
    s, d = request("POST", "/api/secrets/verify", body={"provider_id": "deepseek"})
    assert s == 200 and d.get("status") == "VERIFIED", f"secrets verify status={s}"


@check("secrets")
def t_secrets_assets():
    s, _, _ = request("GET", "/modules/secrets_guardian.js", raw=True)
    assert s == 200, f"secrets_guardian.js status={s}"


def main():
    global BASE
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default=BASE)
    ap.add_argument("--only", default="", help="comma-separated groups")
    args = ap.parse_args()
    BASE = args.base.rstrip("/")
    only = {g.strip() for g in args.only.split(",") if g.strip()}

    print(f"[{stamp()}] CEO Cockpit smoke suite -> {BASE}")
    passed = failed = 0
    t_all = time.time()
    for fn in TESTS:
        if only and fn._group not in only:
            continue
        t0 = time.time()
        name = f"{fn._group}:{fn.__name__[2:]}"
        try:
            fn()
            passed += 1
            print(f"[{stamp()}] PASS  {name:<45} {int((time.time() - t0) * 1000)}ms")
        except Exception as e:
            failed += 1
            print(f"[{stamp()}] FAIL  {name:<45} {type(e).__name__}: {e}")
    print(f"[{stamp()}] {passed} passed, {failed} failed in {time.time() - t_all:.1f}s")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
