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
    assert d.get("roadmap", {}).get("completed_rounds") == 10, "10 rounds not reported"
    assert len(d.get("modules", [])) >= 7, "missing modules in manifest"
    assert "registered_routes" in d and d["registered_routes"]["count"] >= 15, "missing routes"


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
