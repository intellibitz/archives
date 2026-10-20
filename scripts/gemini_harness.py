#!/usr/bin/env python3
"""
Gemini API Pay-As-You-Go Unified Management Harness.

Automates:
1. Health checks & model verification against Google AI Studio.
2. LiteLLM Proxy Gateway lifecycle management (Spend Guard, Fallback routing).
3. Promptfoo benchmark evaluations for quality, cost, and latency verification.
4. Aider pair-programming launches with Gemini PAYG configurations.
"""

import argparse
import json
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

WORKSPACE_ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = WORKSPACE_ROOT / ".env"
CONFIG_FILE = WORKSPACE_ROOT / "orchestrator" / "litellm_config.yaml"
EVAL_CONFIG = WORKSPACE_ROOT / "eval" / "gemini_payg_eval.yaml"
AIDER_CONFIG = WORKSPACE_ROOT / ".aider.gemini.conf.yml"
CACHE_DIR = WORKSPACE_ROOT / ".cache" / "litellm"


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


def get_gemini_key():
    load_env()
    key = os.getenv("GEMINI_API_KEY")
    if not key:
        print("[ERROR] GEMINI_API_KEY is not defined in .env", file=sys.stderr)
        sys.exit(1)
    return key


def get_gcp_project():
    """Retrieve GCP Project ID dynamically from environment or gcloud config."""
    load_env()
    project = os.getenv("GCP_PROJECT_ID") or os.getenv("GOOGLE_CLOUD_PROJECT")
    if not project:
        gcloud = shutil.which("gcloud") or "gcloud"
        try:
            res = subprocess.run(
                [gcloud, "config", "get-value", "project"],
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


def action_healthcheck():
    """Verify Google AI Studio API connectivity and list active models."""
    key = get_gemini_key()
    print("===> [Gemini PAYG] Verifying Google AI Studio API Connectivity...")
    url = f"https://generativelanguage.googleapis.com/v1beta/models?key={key}"
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Gemini-PAYG-Harness/1.0"})
        with urllib.request.urlopen(req, timeout=10) as response:
            data = json.loads(response.read().decode("utf-8"))
            models = [
                m for m in data.get("models", [])
                if "generateContent" in m.get("supportedGenerationMethods", [])
            ]
            print(f"[SUCCESS] Authenticated with Google AI Studio.")
            print(f"[STATUS]  Total active generateContent models: {len(models)}")
            print("\nKey Production Models for Pay-As-You-Go Architecture:")
            print(f"{'Model Identifier':<42} | {'Display Name'}")
            print("-" * 75)
            for m in models:
                name = m.get("name", "")
                if any(x in name for x in ["gemini-3.8-flash", "gemini-flash-latest", "gemini-pro-latest", "gemini-2.5-flash"]):
                    print(f"{name:<42} | {m.get('displayName', '')}")
    except Exception as e:
        print(f"[FAIL] Health check failed: {e}", file=sys.stderr)
        sys.exit(1)


def find_litellm():
    """Locate the litellm executable."""
    cmd = shutil.which("litellm")
    if cmd:
        return cmd
    appdata = os.getenv("APPDATA", "")
    if appdata:
        candidate = Path(appdata) / "uv" / "tools" / "litellm" / "Scripts" / "litellm.exe"
        if candidate.exists():
            return str(candidate)
    return "litellm"


def find_aider():
    """Locate the aider executable."""
    cmd = shutil.which("aider")
    if cmd:
        return cmd
    appdata = os.getenv("APPDATA", "")
    if appdata:
        candidate = Path(appdata) / "uv" / "tools" / "aider-chat" / "Scripts" / "aider.exe"
        if candidate.exists():
            return str(candidate)
    return "aider"


def action_start_proxy():
    """Launch LiteLLM Proxy in the background with configured spend guard."""
    load_env()
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    log_file = CACHE_DIR / "proxy.log"

    print("===> [LiteLLM Proxy] Checking proxy status on http://127.0.0.1:4000...")
    try:
        with urllib.request.urlopen("http://127.0.0.1:4000/health", timeout=2) as resp:
            if resp.status == 200:
                print("[INFO] LiteLLM Proxy is already running and healthy.")
                return
    except Exception:
        pass

    litellm_exe = find_litellm()
    print(f"===> [LiteLLM Proxy] Launching via {litellm_exe}...")
    proxy_env = os.environ.copy()
    proxy_env["PYTHONUTF8"] = "1"
    proxy_env["PYTHONIOENCODING"] = "utf-8"

    with open(log_file, "a", encoding="utf-8") as out:
        flags = 0
        if sys.platform == "win32":
            flags = subprocess.CREATE_NEW_PROCESS_GROUP | subprocess.DETACHED_PROCESS
        proc = subprocess.Popen(
            [
                litellm_exe,
                "--config", str(CONFIG_FILE),
                "--host", "127.0.0.1",
                "--port", "4000"
            ],
            stdout=out,
            stderr=out,
            creationflags=flags,
            env=proxy_env
        )

    print(f"[INFO] Process launched (PID: {proc.pid}). Waiting for health check...")
    for _ in range(10):
        time.sleep(1)
        try:
            with urllib.request.urlopen("http://127.0.0.1:4000/health", timeout=2) as resp:
                if resp.status == 200:
                    print("[SUCCESS] LiteLLM Proxy is LIVE on http://127.0.0.1:4000")
                    return
        except Exception:
            pass
    print(f"[WARNING] Proxy started. Check log file at: {log_file}")


def action_stop_proxy():
    """Stop running LiteLLM background processes."""
    print("===> [LiteLLM Proxy] Stopping proxy processes...")
    if sys.platform == "win32":
        subprocess.run(["taskkill", "/F", "/IM", "litellm.exe"], capture_output=True)
        subprocess.run(["taskkill", "/F", "/IM", "granian.exe"], capture_output=True)
    else:
        subprocess.run(["pkill", "-f", "litellm"], capture_output=True)
    print("[SUCCESS] LiteLLM Proxy stopped.")


def action_proxy_status():
    """Query health status and available models on LiteLLM Proxy."""
    try:
        req = urllib.request.Request(
            "http://127.0.0.1:4000/models",
            headers={"Authorization": "Bearer orchestrator-local-gateway"}
        )
        with urllib.request.urlopen(req, timeout=3) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            models = [m.get("id") for m in data.get("data", [])]
            print(f"[STATUS] LiteLLM Proxy: ONLINE & HEALTHY (Port 4000)")
            print(f"[MODELS] {len(models)} Models Registered:")
            for m in models:
                prefix = "[*]" if "gemini" in m else "[-]"
                print(f"  {prefix} {m}")
    except Exception as e:
        print(f"[STATUS] LiteLLM Proxy: OFFLINE or UNREACHABLE ({e})")


def action_eval():
    """Execute Promptfoo evaluation suite."""
    load_env()
    print("===> [Promptfoo] Executing Gemini Pay-As-You-Go Benchmark...")
    subprocess.run(["npx", "promptfoo", "eval", "-c", str(EVAL_CONFIG), "--no-cache"], env=os.environ.copy(), check=False)


def action_aider():
    """Launch Aider pair-programming session with Gemini configuration."""
    load_env()
    aider_exe = find_aider()
    print(f"===> [Aider] Launching Gemini 3.8 Flash Autonomous Pair-Programmer via {aider_exe}...")
    subprocess.run([aider_exe, "--config", str(AIDER_CONFIG)], env=os.environ.copy(), check=False)


def action_usage():
    """Query and display Gemini API usage, billing status, API key metadata, and telemetry."""
    print("===> [Gemini Usage & Billing Telemetry] Querying Google Cloud Services...")
    gcloud = shutil.which("gcloud") or "gcloud"
    project = get_gcp_project()
    masked_project = project[:4] + "..." + project[-4:] if len(project) > 8 else "***"

    # 1. Billing Status
    print(f"\n1. Pay-As-You-Go Billing Status ({masked_project}):")
    b_cmd = [gcloud, "billing", "projects", "describe", project, "--format=json"]
    b_res = subprocess.run(b_cmd, capture_output=True, text=True, shell=(sys.platform == "win32"))
    if b_res.returncode == 0:
        b_data = json.loads(b_res.stdout)
        account_name = b_data.get('billingAccountName', 'N/A')
        if account_name and "/" in account_name:
            prefix, acc_id = account_name.split("/", 1)
            masked_id = acc_id[:3] + "..." + acc_id[-4:] if len(acc_id) > 7 else "***"
            account_name = f"{prefix}/{masked_id}"
        print(f"  Account Name:    {account_name}")
        print(f"  Billing Enabled: {b_data.get('billingEnabled', False)}")
    else:
        print(f"  Billing Query:   {b_res.stderr.strip()}")

    # 2. API Key Restrictions & Status
    print("\n2. API Key Governance & Targets:")
    k_cmd = [gcloud, "services", "api-keys", "list", f"--project={project}", "--format=json"]
    k_res = subprocess.run(k_cmd, capture_output=True, text=True, shell=(sys.platform == "win32"))
    if k_res.returncode == 0:
        k_data = json.loads(k_res.stdout)
        for key in k_data:
            display_name = key.get("displayName", "Unnamed Key")
            create_time = key.get("createTime", "N/A")
            restrictions = key.get("restrictions", {}).get("apiTargets", [])
            target_svcs = [t.get("service") for t in restrictions if "service" in t]
            print(f"  Key Name:        {display_name}")
            print(f"  Created Time:    {create_time}")
            print(f"  Allowed Targets: {', '.join(target_svcs) if target_svcs else 'Unrestricted'}")
    else:
        print(f"  Key Query:       {k_res.stderr.strip()}")

    # 3. Request Count Telemetry (Cloud Monitoring)
    print("\n3. Cloud Monitoring (24-Hour Gemini API Request Telemetry):")
    tok_cmd = [gcloud, "auth", "print-access-token"]
    tok_res = subprocess.run(tok_cmd, capture_output=True, text=True, shell=(sys.platform == "win32"))
    if tok_res.returncode == 0:
        token = tok_res.stdout.strip()
        from datetime import datetime, timedelta, timezone
        now = datetime.now(timezone.utc)
        start = (now - timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%SZ")
        end = now.strftime("%Y-%m-%dT%H:%M:%SZ")
        m_url = (
            f"https://monitoring.googleapis.com/v3/projects/{project}/timeSeries"
            f"?filter=metric.type%3D%22serviceruntime.googleapis.com%2Fapi%2Frequest_count%22"
            f"%20AND%20resource.labels.service%3D%22generativelanguage.googleapis.com%22"
            f"&interval.startTime={start}&interval.endTime={end}"
        )
        try:
            req = urllib.request.Request(m_url, headers={"Authorization": f"Bearer {token}"})
            with urllib.request.urlopen(req, timeout=5) as resp:
                m_data = json.loads(resp.read().decode("utf-8"))
                streams = m_data.get("timeSeries", [])
                total_calls = 0
                for s in streams:
                    points = s.get("points", [])
                    for pt in points:
                        total_calls += int(pt.get("value", {}).get("int64Value", 0))
                print(f"  Active Metric Streams: {len(streams)}")
                print(f"  Measured 24h Requests: {total_calls}")
        except Exception as e:
            print(f"  Monitoring Query:      {e}")
    else:
        print("  Could not fetch gcloud access token for monitoring query.")


def main():
    parser = argparse.ArgumentParser(description="Gemini API Pay-As-You-Go Unified Harness")
    parser.add_argument(
        "action",
        choices=["healthcheck", "start-proxy", "stop-proxy", "status-proxy", "eval", "aider", "usage"],
        help="Action to perform"
    )
    args = parser.parse_args()

    actions = {
        "healthcheck": action_healthcheck,
        "start-proxy": action_start_proxy,
        "stop-proxy": action_stop_proxy,
        "status-proxy": action_proxy_status,
        "eval": action_eval,
        "aider": action_aider,
        "usage": action_usage,
    }
    actions[args.action]()


if __name__ == "__main__":
    main()
