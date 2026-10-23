"""
Round 14 — Automated Architecture & Code Compliance Reviewer.

Performs static AST validation, secret leakage detection, binary sprawl
detection, and compliance scoring across active workspace files.
"""

import ast
import os
import re
import time
from typing import Any, Dict, List
from modules import route
from modules.common import active_workspace, read_json_body, emit_event, send_json

SECRET_PATTERNS = [
    ("OPENAI_KEY", re.compile(r"sk-[a-zA-Z0-9]{24,}")),
    ("ANTHROPIC_KEY", re.compile(r"sk-ant-[a-zA-Z0-9\-]{24,}")),
    ("GITHUB_TOKEN", re.compile(r"gh[pousr]_[a-zA-Z0-9]{36,}")),
    ("AWS_KEY", re.compile(r"AKIA[0-9A-Z]{16}")),
    ("PRIVATE_KEY", re.compile(r"-----BEGIN (RSA|EC|DSA|OPENSSH) PRIVATE KEY-----")),
]


def _scan_directory(root_dir: str, max_files: int = 150) -> Dict[str, Any]:
    findings = []
    scanned_count = 0
    clean_count = 0
    syntax_errors = 0

    ignore_dirs = {".git", "__pycache__", "node_modules", ".venv", "venv", ".idea", ".vscode"}

    for dirpath, dirnames, filenames in os.walk(root_dir):
        dirnames[:] = [d for d in dirnames if d not in ignore_dirs]
        for f in filenames:
            if scanned_count >= max_files:
                break
            filepath = os.path.join(dirpath, f)
            rel_path = os.path.relpath(filepath, root_dir)

            try:
                st = os.stat(filepath)
                size = st.st_size
            except Exception:
                continue

            # Check for large binary
            if size > 10 * 1024 * 1024 and not f.endswith((".pack", ".idx")):
                findings.append({
                    "severity": "WARNING",
                    "category": "Binary Sprawl",
                    "file": rel_path,
                    "message": f"Large file detected ({round(size / (1024*1024), 1)} MB). Consider Git LFS."
                })

            if f.endswith((".py", ".js", ".html", ".css", ".json", ".md", ".yaml", ".yml", ".env")):
                scanned_count += 1
                try:
                    with open(filepath, "r", encoding="utf-8", errors="ignore") as fp:
                        content = fp.read(150_000)

                    # Check secrets
                    for name, pat in SECRET_PATTERNS:
                        if pat.search(content):
                            # Ensure it's not a documentation or template string
                            if "your_api_key" not in content and "sk-..." not in content:
                                findings.append({
                                    "severity": "CRITICAL",
                                    "category": "Secret Leakage",
                                    "file": rel_path,
                                    "message": f"Potential live {name} pattern discovered in plain text."
                                })

                    # AST check for python
                    if f.endswith(".py"):
                        try:
                            ast.parse(content, filename=rel_path)
                        except SyntaxError as se:
                            syntax_errors += 1
                            findings.append({
                                "severity": "ERROR",
                                "category": "Syntax/AST",
                                "file": rel_path,
                                "message": f"Python AST SyntaxError at line {se.lineno}: {se.msg}"
                            })

                    clean_count += 1
                except Exception:
                    pass

    # Score calculation (100 down)
    critical_count = sum(1 for f in findings if f["severity"] == "CRITICAL")
    error_count = sum(1 for f in findings if f["severity"] == "ERROR")
    warning_count = sum(1 for f in findings if f["severity"] == "WARNING")

    score = max(0, 100 - (critical_count * 25) - (error_count * 10) - (warning_count * 3))
    if score >= 95:
        grade = "GRADE_A_PLUS"
    elif score >= 85:
        grade = "GRADE_A"
    elif score >= 75:
        grade = "GRADE_B"
    elif score >= 60:
        grade = "GRADE_C"
    else:
        grade = "FAIL"

    return {
        "score": score,
        "grade": grade,
        "scanned_files": scanned_count,
        "clean_files": clean_count,
        "syntax_errors": syntax_errors,
        "findings": findings,
        "timestamp": time.time()
    }


@route("GET", "/api/compliance/scan")
def handle_compliance_scan(handler, query, tail):
    ws = active_workspace()
    result = _scan_directory(ws)
    send_json(handler, 200, {
        "success": True,
        "workspace": ws,
        "compliance": result
    })


@route("POST", "/api/compliance/audit")
def handle_compliance_audit(handler, query, tail):
    body = read_json_body(handler) or {}
    target_path = body.get("path")
    ws = active_workspace()
    scan_target = os.path.join(ws, target_path) if target_path else ws

    if not os.path.exists(scan_target):
        send_json(handler, 404, {"success": False, "error": f"Path not found: {scan_target}"})
        return

    result = _scan_directory(scan_target)
    emit_event("compliance.audited", {"target": target_path, "score": result["score"]})

    send_json(handler, 200, {
        "success": True,
        "target": target_path or "root",
        "compliance": result
    })


@route("POST", "/api/compliance/redact")
def handle_compliance_redact(handler, query, tail):
    body = read_json_body(handler) or {}
    text = body.get("text", "")
    redacted = text
    matches_found = 0

    for name, pat in SECRET_PATTERNS:
        matches = pat.findall(redacted)
        if matches:
            matches_found += len(matches)
            redacted = pat.sub(f"[REDACTED_{name}]", redacted)

    send_json(handler, 200, {
        "success": True,
        "matches_found": matches_found,
        "redacted_text": redacted
    })
