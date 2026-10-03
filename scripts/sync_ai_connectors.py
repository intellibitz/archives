#!/usr/bin/env python3
"""
Dynamic AI Connector & GitHub Integration Sync Script for IntelliBitz Archives.

This script:
1. Probes local AI inference engines (Ollama, vLLM, Open WebUI, llama-server).
2. Aggregates free & BYOK AI model provider connectors.
3. Fetches live telemetry (stars, descriptions, activity) for top open-source AI repos via GitHub API.
4. Dynamically injects updated tables into project landing pages (README.md & ai/README.md).
"""

import json
import os
import re
import sys
import time
import urllib.request
from datetime import datetime, timezone

# ---------------------------------------------------------------------------
# 1. Configured AI GitHub Repositories
# ---------------------------------------------------------------------------
TOP_AI_REPOS = [
    "deepseek-ai/DeepSeek-R1",
    "deepseek-ai/DeepSeek-V3",
    "QwenLM/Qwen2.5-Coder",
    "vllm-project/vllm",
    "ggml-org/llama.cpp",
    "ollama/ollama",
    "open-webui/open-webui",
    "browser-use/browser-use",
    "RooVetGit/Roo-Cline",
    "crewAIInc/crewAI",
]

# ---------------------------------------------------------------------------
# 2. Local Engine Probing
# ---------------------------------------------------------------------------
def probe_local_engines():
    engines = [
        {"name": "Ollama Service", "url": "http://localhost:11434/api/version", "port": 11434},
        {"name": "Open WebUI", "url": "http://localhost:8080/api/v1/version", "port": 8080},
        {"name": "vLLM Engine", "url": "http://localhost:8000/v1/models", "port": 8000},
        {"name": "Native llama-server", "url": "http://localhost:8081/health", "port": 8081},
    ]

    status_rows = []
    for eng in engines:
        try:
            req = urllib.request.Request(eng["url"], headers={"User-Agent": "IntelliBitz-Sync/1.0"})
            with urllib.request.urlopen(req, timeout=2) as resp:
                if resp.status == 200:
                    status_rows.append(f"| **{eng['name']}** | `http://localhost:{eng['port']}` | 🟢 **ACTIVE / ONLINE** |")
                    continue
        except Exception:
            pass
        status_rows.append(f"| **{eng['name']}** | `http://localhost:{eng['port']}` | ⚪ Offline / Standby |")

    return "\n".join(status_rows)

# ---------------------------------------------------------------------------
# 3. Free & BYOK AI Providers Data
# ---------------------------------------------------------------------------
def get_free_ai_connectors():
    providers = [
        {
            "name": "OpenRouter Free Tier",
            "endpoint": "https://openrouter.ai/api/v1",
            "models": "`deepseek/deepseek-r1:free`, `qwen/qwen-2.5-coder-32b:free`, `meta-llama/llama-3.3-70b-instruct:free`",
            "cost": "**$0.00 / 1M** (Free tier)",
            "key_var": "`OPENROUTER_API_KEY`",
        },
        {
            "name": "Zhipu GLM",
            "endpoint": "https://open.bigmodel.cn/api/paas/v4",
            "models": "`glm-4-flash`",
            "cost": "**$0.00 / 1M** (100% Free API)",
            "key_var": "`ZHIPU_API_KEY`",
        },
        {
            "name": "Google Gemini Free Tier",
            "endpoint": "https://generativelanguage.googleapis.com/v1beta/openai/",
            "models": "`gemini-2.0-flash-exp`, `gemini-1.5-flash`",
            "cost": "**$0.00** (15 Req/Min free in AI Studio)",
            "key_var": "`GEMINI_API_KEY`",
        },
        {
            "name": "Groq Cloud",
            "endpoint": "https://api.groq.com/openai/v1",
            "models": "`llama-3.3-70b-versatile`, `llama-3.1-8b-instant`",
            "cost": "Free Developer Tier (Ultra-Fast LPUs)",
            "key_var": "`GROQ_API_KEY`",
        },
        {
            "name": "DeepSeek API Direct",
            "endpoint": "https://api.deepseek.com/v1",
            "models": "`deepseek-chat` (V3), `deepseek-reasoner` (R1)",
            "cost": "**$0.003 / 1M** (Cache Hit) / **$0.14** (Miss)",
            "key_var": "`DEEPSEEK_API_KEY`",
        },
        {
            "name": "Chutes AI",
            "endpoint": "https://chutes.ai/v1",
            "models": "`deepseek-ai/DeepSeek-R1`, `Qwen/Qwen2.5-Coder-32B`",
            "cost": "Serverless GPU Pay-Per-Token",
            "key_var": "`CHUTES_API_KEY`",
        },
    ]

    rows = []
    for p in providers:
        rows.append(
            f"| **[{p['name']}]({p['endpoint']})** | `{p['endpoint']}` | {p['models']} | {p['cost']} | {p['key_var']} |"
        )
    return "\n".join(rows)

# ---------------------------------------------------------------------------
# 4. Fetch Top AI Repos from GitHub API
# ---------------------------------------------------------------------------
def fetch_github_repo_telemetry():
    token = os.environ.get("GITHUB_TOKEN", "")
    headers = {"User-Agent": "IntelliBitz-Sync/1.0", "Accept": "application/vnd.github.v3+json"}
    if token:
        headers["Authorization"] = f"token {token}"

    rows = []
    for repo_full_name in TOP_AI_REPOS:
        url = f"https://api.github.com/repos/{repo_full_name}"
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=5) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                stars = data.get("stargazers_count", 0)
                stars_fmt = f"⭐ {stars:,}"
                desc = data.get("description", "No description provided.")
                # Truncate description if too long
                if len(desc) > 80:
                    desc = desc[:77] + "..."
                html_url = data.get("html_url", f"https://github.com/{repo_full_name}")
                repo_name = data.get("name", repo_full_name.split("/")[-1])
                rows.append(f"| **[{repo_name}]({html_url})** (`{repo_full_name}`) | **{stars_fmt}** | {desc} |")
        except Exception as e:
            rows.append(f"| **[{repo_full_name}](https://github.com/{repo_full_name})** | ⭐ Tracked | {repo_full_name} repository |")

    return "\n".join(rows)

# ---------------------------------------------------------------------------
# 5. Inject Dynamic Content Into Markdown Anchors
# ---------------------------------------------------------------------------
def inject_dynamic_section(file_path, start_anchor, end_anchor, new_content):
    if not os.path.exists(file_path):
        print(f"Skipping {file_path} (file not found)")
        return False

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    pattern = re.compile(
        f"({re.escape(start_anchor)}).*?({re.escape(end_anchor)})",
        re.DOTALL
    )

    if not pattern.search(content):
        print(f"Anchors not found in {file_path}")
        return False

    replacement = f"\\1\n{new_content}\n\\2"
    updated_content = pattern.sub(replacement, content)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(updated_content)

    print(f"Successfully updated dynamic anchors in {file_path}")
    return True

# ---------------------------------------------------------------------------
# Main Runner
# ---------------------------------------------------------------------------
def main():
    print("🚀 Running Dynamic AI Sync for IntelliBitz Archives...")
    now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")

    local_status = probe_local_engines()
    free_connectors = get_free_ai_connectors()
    github_repos = fetch_github_repo_telemetry()

    local_block = f"""
> *Last Status Check: `{now_utc}`*

| Inference Service / Local Server | Endpoint URL | Status |
| :--- | :--- | :--- |
{local_status}
"""

    connectors_block = f"""
> *Updated: `{now_utc}`*

| AI Provider & Connector | API Base Endpoint | Free / Low-Cost Models | Pricing Tier | Environment Variable |
| :--- | :--- | :--- | :--- | :--- |
{free_connectors}
"""

    github_block = f"""
> *Telemetry Sync: `{now_utc}`*

| Open-Source AI Project | GitHub Stars | Description & Purpose |
| :--- | :--- | :--- |
{github_repos}
"""

    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    readme_path = os.path.join(root_dir, "README.md")

    inject_dynamic_section(readme_path, "<!-- DYNAMIC_LOCAL_ENGINES_START -->", "<!-- DYNAMIC_LOCAL_ENGINES_END -->", local_block.strip())
    inject_dynamic_section(readme_path, "<!-- DYNAMIC_AI_CONNECTORS_START -->", "<!-- DYNAMIC_AI_CONNECTORS_END -->", connectors_block.strip())
    inject_dynamic_section(readme_path, "<!-- DYNAMIC_GITHUB_INTEGRATIONS_START -->", "<!-- DYNAMIC_GITHUB_INTEGRATIONS_END -->", github_block.strip())

    print("✨ Dynamic AI Sync completed successfully!")

if __name__ == "__main__":
    main()
