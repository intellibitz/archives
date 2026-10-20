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

WORKSPACE_ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = WORKSPACE_ROOT / ".env"


def load_env():
    """Load environment variables from .env if present."""
    if ENV_FILE.exists():
        with open(ENV_FILE, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    k = k.strip()
                    v = v.strip().strip('"').strip("'")
                    os.environ[k] = v


def get_gcp_project():
    """Retrieve GCP Project ID dynamically from environment or gcloud config."""
    load_env()
    project = os.getenv("GCP_PROJECT_ID") or os.getenv("GOOGLE_CLOUD_PROJECT")
    if not project:
        gcloud_bin = shutil.which("gcloud") or "gcloud"
        try:
            res = subprocess.run(
                [gcloud_bin, "config", "get-value", "project"],
                capture_output=True,
                text=True,
                shell=(sys.platform == "win32")
            )
            if res.returncode == 0 and res.stdout.strip() and "(unset)" not in res.stdout:
                project = res.stdout.strip()
        except Exception:
            pass
    if not project:
        print("[ERROR] GCP project ID not specified in GCP_PROJECT_ID, GOOGLE_CLOUD_PROJECT, or gcloud config.", file=sys.stderr)
        sys.exit(1)
    return project


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
    project = get_gcp_project()
    masked_project = project[:4] + "..." + project[-4:] if len(project) > 8 else "***"
    print(f"===> Fetching enabled services for project: {masked_project}...")
    gcloud = shutil.which("gcloud") or "gcloud"

    cmd = [gcloud, "services", "list", "--enabled", f"--project={project}", "--format=json"]
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
        dis_cmd = [gcloud, "services", "disable", s, f"--project={project}", "--quiet", "--force"]
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
