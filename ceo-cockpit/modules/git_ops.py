"""
Round 8 — Git Hygiene & Release Mission Control.

Visual git repository topology, uncommitted change inspection,
diff visualizer, autonomous AI semantic commit generation, and
verified atomic release controls.
"""

import os
import subprocess
from typing import Any, Dict, List, Optional

from modules import route
from modules.common import (active_workspace, emit_event, llm_complete,
                            log, read_json_body, send_json)

SAFE_GIT_ENV = dict(os.environ)
SAFE_GIT_ENV["GIT_CONFIG_COUNT"] = "1"
SAFE_GIT_ENV["GIT_CONFIG_KEY_0"] = "safe.directory"
SAFE_GIT_ENV["GIT_CONFIG_VALUE_0"] = "*"
SAFE_GIT_ENV["GIT_TERMINAL_PROMPT"] = "0"


def _run_git(args: List[str], cwd: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        ["git"] + args,
        cwd=cwd,
        capture_output=True,
        text=True,
        env=SAFE_GIT_ENV,
        timeout=15
    )


def inspect_git_state(cwd: str) -> Dict[str, Any]:
    """Inspects detailed git status of the workspace."""
    branch = "unknown"
    ahead = behind = 0

    res_branch = _run_git(["rev-parse", "--abbrev-ref", "HEAD"], cwd)
    if res_branch.returncode == 0:
        branch = res_branch.stdout.strip()

    res_count = _run_git(["rev-list", "--left-right", "--count", f"@{'{u}'}...HEAD"], cwd)
    if res_count.returncode == 0 and res_count.stdout.strip():
        parts = res_count.stdout.strip().split()
        if len(parts) == 2:
            behind, ahead = int(parts[0]), int(parts[1])

    res_status = _run_git(["status", "--porcelain"], cwd)
    staged: List[Dict[str, str]] = []
    unstaged: List[Dict[str, str]] = []
    untracked: List[str] = []

    if res_status.returncode == 0:
        for line in res_status.stdout.splitlines():
            if len(line) < 3:
                continue
            index_code = line[0]
            work_code = line[1]
            path = line[3:].strip()

            if index_code == "?" and work_code == "?":
                untracked.append(path)
            else:
                if index_code not in (" ", "?"):
                    staged.append({"status": index_code, "path": path})
                if work_code not in (" ", "?"):
                    unstaged.append({"status": work_code, "path": path})

    res_log = _run_git(["log", "--oneline", "-n", "8"], cwd)
    recent_commits = [l.strip() for l in res_log.stdout.splitlines() if l.strip()]

    return {
        "success": True,
        "workspace": cwd,
        "is_repo": res_branch.returncode == 0,
        "branch": branch,
        "ahead": ahead,
        "behind": behind,
        "clean": len(staged) == 0 and len(unstaged) == 0 and len(untracked) == 0,
        "staged": staged,
        "unstaged": unstaged,
        "untracked": untracked,
        "staged_count": len(staged),
        "unstaged_count": len(unstaged),
        "untracked_count": len(untracked),
        "recent_commits": recent_commits
    }


# ------------------------------------------------------------------------------
# HTTP Routes
# ------------------------------------------------------------------------------
@route("GET", "/api/git/status")
def get_git_status(h, query, tail):
    ws = active_workspace()
    state = inspect_git_state(ws)
    send_json(h, 200, state)


@route("GET", "/api/git/diff")
def get_git_diff(h, query, tail):
    ws = active_workspace()
    staged_only = query.get("staged", ["false"])[0].lower() == "true"
    target_file = query.get("file", [""])[0]

    args = ["diff"]
    if staged_only:
        args.append("--cached")
    if target_file:
        args.extend(["--", target_file])

    res = _run_git(args, ws)
    diff_text = res.stdout
    if len(diff_text) > 100_000:
        diff_text = diff_text[:100_000] + "\n\n... [Diff truncated for performance]"

    send_json(h, 200, {
        "success": True,
        "staged": staged_only,
        "file": target_file or "all",
        "diff": diff_text
    })


@route("POST", "/api/git/stage")
def post_git_stage(h, query, tail):
    req = read_json_body(h)
    paths = req.get("paths", [])
    ws = active_workspace()

    if not paths or paths == ["*"]:
        res = _run_git(["add", "-A"], ws)
    else:
        res = _run_git(["add", "--"] + paths, ws)

    if res.returncode != 0:
        send_json(h, 500, {"success": False, "error": res.stderr})
        return

    emit_event("git", "Staged workspace changes", f"Files: {len(paths)}", "info")
    send_json(h, 200, {"success": True, "state": inspect_git_state(ws)})


@route("POST", "/api/git/suggest-commit")
def post_suggest_commit(h, query, tail):
    ws = active_workspace()
    res_diff = _run_git(["diff", "--cached", "--stat"], ws)
    diff_stat = res_diff.stdout.strip()

    if not diff_stat:
        # Check unstaged diffstat if nothing staged
        res_diff_un = _run_git(["diff", "--stat"], ws)
        diff_stat = res_diff_un.stdout.strip()

    if not diff_stat:
        send_json(h, 200, {"success": True, "message": "chore: sync workspace state"})
        return

    prompt = (
        f"You are the Release Commander. Write a clean, high-precision Conventional Commit message "
        f"based on this git diff footprint:\n\n{diff_stat}\n\n"
        "Rules:\n"
        "1. First line must follow: <type>(<scope>): <concise functional summary>\n"
        "   Allowed types: feat, fix, refactor, perf, test, chore, docs.\n"
        "2. Keep first line under 72 characters.\n"
        "3. Output ONLY the commit message without Markdown quotes or preamble."
    )

    result = llm_complete(
        model="deepseek-chat",
        prompt=prompt,
        system_prompt="You are an expert Git Release Engineer generating conventional commit messages.",
        temperature=0.1,
        max_tokens=200
    )

    clean_msg = result.get("reply", "feat(cockpit): update workspace capabilities").strip().strip('"').strip('`')
    first_line = clean_msg.splitlines()[0] if clean_msg else "feat(cockpit): update workspace capabilities"
    send_json(h, 200, {"success": True, "message": first_line})


@route("POST", "/api/git/commit")
def post_git_commit(h, query, tail):
    req = read_json_body(h)
    message = req.get("message", "").strip()
    if not message:
        send_json(h, 400, {"success": False, "error": "Commit message is required"})
        return

    ws = active_workspace()
    res = _run_git(["commit", "-m", message], ws)
    if res.returncode != 0:
        send_json(h, 500, {"success": False, "error": res.stderr or res.stdout})
        return

    emit_event("git", f"Committed: {message[:50]}...", "Atomic commit created", "success")
    send_json(h, 200, {"success": True, "output": res.stdout, "state": inspect_git_state(ws)})
