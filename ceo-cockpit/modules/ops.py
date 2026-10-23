"""
Round 3 — Ops Console: Autonomous Tool Execution Engine.

Executes curated runbooks or guarded custom commands inside the active
workspace and streams output live over Server-Sent Events. Every run is
persisted to a bounded history ledger and published to the activity bus.

Safety model:
  * Destructive patterns (recursive deletes, force pushes, hard resets, DROP /
    TRUNCATE, disk formatting, power control, cloud project/bucket deletion,
    stopping the cockpit itself) are refused before execution.
  * Hard timeout per run with full process-tree termination.
  * Output is bounded in memory and on disk.
"""

import os
import re
import shutil
import subprocess
import sys
import threading
import time
import uuid
from collections import deque
from typing import Any, Dict, List, Optional

from modules import route
from modules.common import (BASE_DIR, active_workspace, append_jsonl, data_path,
                            emit_event, log, read_json_body, read_jsonl,
                            send_json, start_sse, write_sse)

HISTORY_FILE = data_path("ops_history.jsonl")
POWERSHELL = r"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"
if not os.path.exists(POWERSHELL):
    POWERSHELL = shutil.which("powershell") or shutil.which("pwsh") or "powershell"

DEFAULT_TIMEOUT = 300
MAX_TIMEOUT = 1800
MAX_BUFFER_LINES = 5000
NODE = shutil.which("node") or r"C:\Program Files\nodejs\node.exe"

# ------------------------------------------------------------------------------
# Runbook catalog
# ------------------------------------------------------------------------------
RUNBOOKS: List[Dict[str, Any]] = [
    {"id": "git-status", "group": "Repository", "icon": "🌿", "title": "Repository Status",
     "desc": "Branch, ahead/behind and uncommitted changes", "cmd": "git status -sb"},
    {"id": "git-log", "group": "Repository", "icon": "📜", "title": "Recent Commits",
     "desc": "Last 15 commits with branch decorations", "cmd": "git log --oneline --decorate -n 15"},
    {"id": "git-diffstat", "group": "Repository", "icon": "📊", "title": "Pending Change Footprint",
     "desc": "Size of uncommitted changes per file", "cmd": "git diff --stat; git diff --cached --stat"},
    {"id": "health", "group": "Infrastructure", "icon": "🩺", "title": "Full Infrastructure Health Check",
     "desc": "Ports, gateways, cache and auto-clicker status",
     "cmd": f"& '{os.path.join(BASE_DIR, 'scripts', 'verify_health.ps1')}'"},
    {"id": "ports", "group": "Infrastructure", "icon": "🔌", "title": "AI Stack Port Listeners",
     "desc": "Which processes own the cockpit service ports",
     "cmd": "Get-NetTCPConnection -State Listen | Where-Object { $_.LocalPort -in 4000,6333,6379,8080,18789 } | "
            "Select-Object LocalAddress,LocalPort,OwningProcess,@{n='Process';e={(Get-Process -Id $_.OwningProcess -EA SilentlyContinue).ProcessName}} | "
            "Sort-Object LocalPort | Format-Table -AutoSize | Out-String -Width 200"},
    {"id": "top-procs", "group": "Infrastructure", "icon": "🔥", "title": "Top Resource Consumers",
     "desc": "15 heaviest processes by memory",
     "cmd": "Get-Process | Sort-Object WorkingSet64 -Descending | Select-Object -First 15 Name,Id,"
            "@{n='CPU(s)';e={[math]::Round($_.CPU,1)}},@{n='MemMB';e={[math]::Round($_.WorkingSet64/1MB)}} | "
            "Format-Table -AutoSize | Out-String -Width 200"},
    {"id": "disk", "group": "Infrastructure", "icon": "💽", "title": "Disk Capacity",
     "desc": "Free and used space on every drive",
     "cmd": "Get-PSDrive -PSProvider FileSystem | Select-Object Name,@{n='UsedGB';e={[math]::Round($_.Used/1GB,1)}},"
            "@{n='FreeGB';e={[math]::Round($_.Free/1GB,1)}},Root | Format-Table -AutoSize | Out-String -Width 200"},
    {"id": "py-check", "group": "Quality Gates", "icon": "🐍", "title": "Cockpit Python Integrity",
     "desc": "Byte-compile every cockpit backend module",
     "cmd": f"& '{sys.executable}' -m compileall -q '{BASE_DIR}' -x '(data|automation)'; if ($LASTEXITCODE -eq 0) {{ 'All Python modules compiled cleanly.' }}"},
    {"id": "js-check", "group": "Quality Gates", "icon": "🟨", "title": "Cockpit Frontend Integrity",
     "desc": "Syntax-check every cockpit JavaScript file",
     "cmd": f"$fail=0; Get-ChildItem -Recurse -Filter *.js '{os.path.join(BASE_DIR, 'web')}' | ForEach-Object {{ & '{NODE}' --check $_.FullName; if ($LASTEXITCODE -ne 0) {{ $fail++ }} else {{ \"OK  $($_.Name)\" }} }}; if ($fail) {{ exit 1 }}"},
    {"id": "smoke", "group": "Quality Gates", "icon": "🧪", "title": "Cockpit API Smoke Suite",
     "desc": "End-to-end endpoint verification suite",
     "cmd": f"& '{sys.executable}' '{os.path.join(BASE_DIR, 'tests', 'smoke_test.py')}'"},
    {"id": "cockpit-log", "group": "Diagnostics", "icon": "🧾", "title": "Cockpit Server Log Tail",
     "desc": "Last 80 lines of the cockpit service log",
     "cmd": r"Get-Content 'C:\ProgramData\ceo_cockpit\logs\cockpit.log' -Tail 80 -EA SilentlyContinue"},
    {"id": "cockpit-errors", "group": "Diagnostics", "icon": "🚨", "title": "Cockpit Error Log Tail",
     "desc": "Last 80 lines of the cockpit error stream",
     "cmd": r"Get-Content 'C:\ProgramData\ceo_cockpit\logs\cockpit-error.log' -Tail 80 -EA SilentlyContinue"},
]
RUNBOOK_INDEX = {r["id"]: r for r in RUNBOOKS}

# ------------------------------------------------------------------------------
# Destructive command guard
# ------------------------------------------------------------------------------
_BLOCK_RULES = [
    (r"\brm\s+-[a-z]*r[a-z]*f|\brm\s+-[a-z]*f[a-z]*r", "Recursive forced delete"),
    (r"remove-item\b[^\n|;]*-recurse", "Recursive delete"),
    (r"\b(rd|rmdir)\b[^\n]*\s/s", "Recursive directory delete"),
    (r"\bdel\b[^\n]*\s/[sq]", "Bulk file delete"),
    (r"\bformat(-volume)?\s+[a-z]:?", "Disk formatting"),
    (r"\b(clear-disk|initialize-disk|diskpart)\b", "Disk partition control"),
    (r"\bgit\s+push\b[^\n]*(--force|\s-f\b|--delete|\s:\S)", "Force push / remote branch deletion"),
    (r"\bgit\s+reset\s+--hard", "Hard reset discards work"),
    (r"\bgit\s+clean\s+-[a-z]*f", "Untracked file purge"),
    (r"\bgit\s+branch\s+-D\b", "Forced branch deletion"),
    (r"\bdrop\s+(table|database|schema)\b", "SQL DROP"),
    (r"\btruncate\s+table\b", "SQL TRUNCATE"),
    (r"\b(shutdown|restart-computer|stop-computer)\b", "Machine power control"),
    (r"\bgcloud\s+projects\s+delete\b", "Cloud project deletion"),
    (r"\b(gsutil\s+(-m\s+)?rm|gcloud\s+storage\s+rm)\b", "Cloud storage deletion"),
    (r"\bgcloud\s+(kms|secrets)\b[^\n]*\b(destroy|delete)\b", "Key / secret destruction"),
    (r"\breg(\.exe)?\s+delete\b|remove-itemproperty[^\n]*hklm", "Registry deletion"),
    (r"\b(bcdedit|cipher\s+/w|vssadmin\s+delete)\b", "System boot / shadow-copy tampering"),
    (r"(stop|remove)-service[^\n]*ceo-cockpit|nssm[^\n]*(stop|remove)\s+ceo-cockpit", "Stopping the cockpit itself"),
]
_BLOCK_COMPILED = [(re.compile(p, re.IGNORECASE), why) for p, why in _BLOCK_RULES]


def assess_command(cmd: str) -> Optional[str]:
    """Return a refusal reason if the command matches a destructive pattern."""
    for rx, why in _BLOCK_COMPILED:
        if rx.search(cmd):
            return why
    return None


# ------------------------------------------------------------------------------
# Job registry
# ------------------------------------------------------------------------------
class Job:
    def __init__(self, title: str, command: str, cwd: str, timeout: int, runbook_id: Optional[str]):
        self.id = "job_" + uuid.uuid4().hex[:10]
        self.title = title
        self.command = command
        self.cwd = cwd
        self.timeout = timeout
        self.runbook_id = runbook_id
        self.lines: deque = deque(maxlen=MAX_BUFFER_LINES)
        self.total_lines = 0
        self.status = "queued"
        self.exit_code: Optional[int] = None
        self.started = time.time()
        self.finished: Optional[float] = None
        self.proc: Optional[subprocess.Popen] = None
        self.cond = threading.Condition()

    def summary(self) -> Dict[str, Any]:
        return {
            "id": self.id, "title": self.title, "command": self.command, "cwd": self.cwd,
            "runbook_id": self.runbook_id, "status": self.status, "exit_code": self.exit_code,
            "started": self.started, "finished": self.finished,
            "duration_ms": int(((self.finished or time.time()) - self.started) * 1000),
            "line_count": self.total_lines,
        }

    def push(self, line: str) -> None:
        with self.cond:
            self.lines.append(line)
            self.total_lines += 1
            self.cond.notify_all()


JOBS: Dict[str, Job] = {}
_jobs_lock = threading.Lock()


def _kill_tree(pid: int) -> None:
    try:
        subprocess.run(["taskkill", "/T", "/F", "/PID", str(pid)],
                       capture_output=True, timeout=15)
    except Exception as e:
        log("ops", f"taskkill failed for {pid}: {e}")


def _child_env() -> Dict[str, str]:
    env = dict(os.environ)
    # The service runs as LocalSystem; trust the user's repositories for git.
    env["GIT_CONFIG_COUNT"] = "1"
    env["GIT_CONFIG_KEY_0"] = "safe.directory"
    env["GIT_CONFIG_VALUE_0"] = "*"
    env["GIT_TERMINAL_PROMPT"] = "0"
    env["PYTHONIOENCODING"] = "utf-8"
    env["NO_COLOR"] = "1"
    return env


def _run_job(job: Job) -> None:
    script = "[Console]::OutputEncoding=[System.Text.Encoding]::UTF8; $ProgressPreference='SilentlyContinue'; " + job.command
    try:
        job.proc = subprocess.Popen(
            [POWERSHELL, "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command", script],
            cwd=job.cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL,
            env=_child_env(), creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
    except Exception as e:
        job.push(f"[launch failed] {e}")
        job.status, job.exit_code, job.finished = "failed", -1, time.time()
        _finalize(job)
        return

    job.status = "running"
    deadline = job.started + job.timeout

    def watchdog():
        while job.proc and job.proc.poll() is None:
            if time.time() > deadline:
                job.push(f"[timeout] Exceeded {job.timeout}s limit — terminating process tree.")
                job.status = "timeout"
                _kill_tree(job.proc.pid)
                return
            time.sleep(0.5)

    threading.Thread(target=watchdog, daemon=True).start()

    assert job.proc.stdout is not None
    for raw in iter(job.proc.stdout.readline, b""):
        job.push(raw.decode("utf-8", errors="replace").rstrip("\r\n"))
    job.proc.wait()
    job.exit_code = job.proc.returncode
    if job.status == "running":
        job.status = "succeeded" if job.exit_code == 0 else "failed"
    job.finished = time.time()
    _finalize(job)


def _finalize(job: Job) -> None:
    with job.cond:
        job.cond.notify_all()
    record = job.summary()
    record["output_tail"] = list(job.lines)[-200:]
    try:
        append_jsonl(HISTORY_FILE, record, max_lines=400)
    except Exception as e:
        log("ops", f"history write failed: {e}")
    level = "success" if job.status == "succeeded" else "error"
    emit_event("ops", f"Ops run {job.status}: {job.title}",
               f"exit={job.exit_code} in {record['duration_ms']}ms", level,
               {"job_id": job.id, "runbook_id": job.runbook_id})
    log("ops", f"{job.id} '{job.title}' -> {job.status} (exit {job.exit_code}, {record['duration_ms']}ms)")


def launch_job(title: str, command: str, cwd: str, timeout: int, runbook_id: Optional[str]) -> Job:
    job = Job(title, command, cwd, timeout, runbook_id)
    with _jobs_lock:
        JOBS[job.id] = job
        # Keep the in-memory registry bounded.
        if len(JOBS) > 60:
            for old_id in sorted(JOBS, key=lambda k: JOBS[k].started)[:len(JOBS) - 60]:
                if JOBS[old_id].status not in ("running", "queued"):
                    JOBS.pop(old_id, None)
    threading.Thread(target=_run_job, args=(job,), daemon=True).start()
    log("ops", f"Launched {job.id} '{title}' in {cwd}")
    return job


def resolve_request(req: Dict[str, Any]) -> Dict[str, Any]:
    """Validate a run request; returns {title, command, cwd, timeout, runbook_id}."""
    runbook_id = req.get("runbook_id")
    if runbook_id:
        rb = RUNBOOK_INDEX.get(runbook_id)
        if not rb:
            raise ValueError(f"Unknown runbook '{runbook_id}'")
        title, command = rb["title"], rb["cmd"]
    else:
        command = (req.get("command") or "").strip()
        if not command:
            raise ValueError("Provide either a runbook_id or a command")
        if len(command) > 4000:
            raise ValueError("Command exceeds 4000 characters")
        reason = assess_command(command)
        if reason:
            raise PermissionError(reason)
        title = req.get("title") or (command if len(command) <= 60 else command[:57] + "...")

    cwd = req.get("cwd") or active_workspace()
    if not os.path.isdir(cwd):
        raise ValueError(f"Working directory not found: {cwd}")
    timeout = int(req.get("timeout") or DEFAULT_TIMEOUT)
    timeout = max(5, min(timeout, MAX_TIMEOUT))
    return {"title": title, "command": command, "cwd": cwd, "timeout": timeout, "runbook_id": runbook_id}


# ------------------------------------------------------------------------------
# HTTP routes
# ------------------------------------------------------------------------------
@route("GET", "/api/ops/runbooks")
def get_runbooks(h, query, tail):
    send_json(h, 200, {"success": True, "workspace": active_workspace(),
                       "runbooks": [{k: v for k, v in r.items() if k != "cmd"} | {"command": r["cmd"]} for r in RUNBOOKS]})


@route("POST", "/api/ops/assess")
def post_assess(h, query, tail):
    req = read_json_body(h)
    reason = assess_command(req.get("command", ""))
    send_json(h, 200, {"success": True, "allowed": reason is None, "reason": reason})


@route("POST", "/api/ops/run")
def post_run(h, query, tail):
    """Launch a job and stream its output as SSE until completion."""
    req = read_json_body(h)
    try:
        spec = resolve_request(req)
    except PermissionError as e:
        emit_event("ops", "Blocked destructive command", f"{e}: {req.get('command', '')[:300]}", "warning")
        send_json(h, 403, {"success": False, "blocked": True, "error": f"Blocked by safety gate: {e}"})
        return

    job = launch_job(**spec)
    start_sse(h)
    if not write_sse(h, {"type": "start", "job": job.summary()}):
        return

    sent = 0
    while True:
        with job.cond:
            if job.total_lines == sent and job.finished is None:
                job.cond.wait(timeout=10)
            total = job.total_lines
            buffered = list(job.lines)
        new_count = total - sent
        if new_count > 0:
            fresh = buffered[-new_count:] if new_count <= len(buffered) else buffered
            if not write_sse(h, {"type": "lines", "lines": fresh}):
                return  # client left; job keeps running and lands in history
            sent = total
        elif job.finished is None:
            if not write_sse(h, {"type": "heartbeat", "elapsed_ms": job.summary()["duration_ms"]}):
                return
        if job.finished is not None and sent >= job.total_lines:
            write_sse(h, {"type": "exit", "job": job.summary()})
            try:
                h.close_connection = True
            except Exception:
                pass
            return


@route("POST", "/api/ops/cancel")
def post_cancel(h, query, tail):
    req = read_json_body(h)
    job = JOBS.get(req.get("job_id", ""))
    if not job:
        send_json(h, 404, {"success": False, "error": "Job not found"})
        return
    if job.proc and job.proc.poll() is None:
        job.status = "cancelled"
        job.push("[cancelled] Terminated by executive command.")
        _kill_tree(job.proc.pid)
    send_json(h, 200, {"success": True, "job": job.summary()})


@route("GET", "/api/ops/jobs")
def get_jobs(h, query, tail):
    live = sorted((j.summary() for j in JOBS.values()), key=lambda j: j["started"], reverse=True)
    send_json(h, 200, {"success": True, "jobs": live})


@route("GET", "/api/ops/jobs/", prefix=True)
def get_job_detail(h, query, tail):
    job = JOBS.get(tail.strip("/"))
    if job:
        send_json(h, 200, {"success": True, "job": job.summary(), "output": list(job.lines)})
        return
    for rec in reversed(read_jsonl(HISTORY_FILE, limit=400)):
        if rec.get("id") == tail.strip("/"):
            send_json(h, 200, {"success": True, "job": rec, "output": rec.get("output_tail", [])})
            return
    send_json(h, 404, {"success": False, "error": "Job not found"})


@route("GET", "/api/ops/history")
def get_history(h, query, tail):
    limit = int((query.get("limit") or ["40"])[0])
    records = read_jsonl(HISTORY_FILE, limit=limit)
    for r in records:
        r.pop("output_tail", None)
    send_json(h, 200, {"success": True, "history": list(reversed(records))})
