#!/usr/bin/env python3
"""
Disable all Google Cloud services on the active project except Gemini API
and its critical infrastructure dependencies.
"""

import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

PROJECT = "gen-lang-client-0333910863"

# Whitelist: Critical services required for Gemini API & Pay-As-You-Go Billing
WHITELIST = {
    "generativelanguage.googleapis.com",   # Gemini API
    "apikeys.googleapis.com",              # Gemini API Key Authentication
    "cloudbilling.googleapis.com",         # Pay-As-You-Go Billing
    "iam.googleapis.com",                  # IAM Identity
    "iamcredentials.googleapis.com",       # IAM Credentials
    "cloudresourcemanager.googleapis.com", # Project Resource Management
    "serviceusage.googleapis.com",         # Service Usage & Enablement
    "logging.googleapis.com",              # Core Logging
    "monitoring.googleapis.com",           # Core Monitoring
}


def main():
    print(f"===> Fetching enabled services for project: {PROJECT}...")
    gcloud = shutil.which("gcloud") or "gcloud"

    cmd = [gcloud, "services", "list", "--enabled", f"--project={PROJECT}", "--format=json"]
    proc = subprocess.run(cmd, capture_output=True, text=True, shell=(sys.platform == "win32"))
    if proc.returncode != 0:
        print(f"[ERROR] Failed to list services: {proc.stderr}", file=sys.stderr)
        sys.exit(1)

    services = json.loads(proc.stdout)
    enabled_names = [s["config"]["name"] for s in services]

    to_disable = [s for s in enabled_names if s not in WHITELIST]

    print(f"Total services currently enabled: {len(enabled_names)}")
    print(f"Critical services protected in whitelist: {len(WHITELIST)}")
    print(f"Candidate services to disable: {len(to_disable)}\n")

    disabled_count = 0
    skipped_count = 0

    for s in to_disable:
        print(f"Disabling {s:<45} ... ", end="", flush=True)
        dis_cmd = [gcloud, "services", "disable", s, f"--project={PROJECT}", "--quiet", "--force"]
        res = subprocess.run(dis_cmd, capture_output=True, text=True, shell=(sys.platform == "win32"))
        if res.returncode == 0:
            print("[DISABLED]")
            disabled_count += 1
        else:
            err_line = res.stderr.strip().splitlines()[-1] if res.stderr else "in-use / dependent"
            print(f"[SKIPPED] ({err_line})")
            skipped_count += 1

    print("\n" + "=" * 60)
    print("Disablement Summary:")
    print(f"  Successfully Disabled:        {disabled_count}")
    print(f"  Retained / In-use / Dependent: {skipped_count}")
    print(f"  Whitelisted Critical:          {len(WHITELIST)}")
    print("=" * 60)

    # Health check verification
    print("\n===> Verifying Gemini API Health after service pruning...")
    harness = Path(__file__).resolve().parent / "gemini_harness.py"
    subprocess.run([sys.executable, str(harness), "healthcheck"])


if __name__ == "__main__":
    main()
