"""Generate the single canonical LiteLLM config from a declarative route table.

Writes orchestrator/litellm_config.yaml (source of truth). The deployed copy at
C:\\ProgramData\\litellm\\config\\config.yaml is produced from this by
orchestrator/deploy_litellm_config.ps1.

Route names cover both naming schemes used in this repo:
  * flat provider names  (ceo-cockpit: gemini-flash, siliconflow-qwen, ...)
  * role names           (orchestrator: antigravity-librarian, claude-architect, ...)
"""

import os

import yaml

LIVE = r"C:\ProgramData\litellm\config\config.yaml"
OUT = r"F:\src\github.com\intellibitz\archives\orchestrator\litellm_config.yaml"

# This account's Anthropic key is not workspace-scoped, so every Anthropic call
# must carry the workspace header. It is an identifier, not a credential.
ANTHROPIC_WS = os.environ.get("ANTHROPIC_WORKSPACE_ID", "")
if not ANTHROPIC_WS:
    raise SystemExit("ANTHROPIC_WORKSPACE_ID is not set; cannot build anthropic routes")

SF = {"api_base": "https://api.siliconflow.com/v1", "api_key": "os.environ/SILICONFLOW_API_KEY"}
DI = {"api_base": "https://api.deepinfra.com/v1/openai", "api_key": "os.environ/DEEPINFRA_API_KEY"}
DS = {"api_key": "os.environ/DEEPSEEK_API_KEY"}
MS = {"api_key": "os.environ/MOONSHOT_API_KEY"}
GM = {"api_key": "os.environ/GEMINI_API_KEY"}
AN = {"api_key": "os.environ/ANTHROPIC_API_KEY", "extra_headers": {"anthropic-workspace-id": ANTHROPIC_WS}}
ZP = {"api_base": "https://open.bigmodel.cn/api/paas/v4", "api_key": "os.environ/ZHIPU_API_KEY"}
OA = {"api_key": "os.environ/OPENAI_API_KEY"}
OLLAMA = {"api_base": "http://127.0.0.1:11434"}
LMSTUDIO = {"api_base": "http://127.0.0.1:1234/v1", "api_key": "lm-studio"}

# (name, model, provider params, caching)
ROUTES = [
    # --- SiliconFlow (international platform; .cn rejects this key) ---
    ("siliconflow-qwen", "openai/Qwen/Qwen3.8-27B", SF),
    ("siliconflow-qwen-coder", "openai/Qwen/Qwen3-Coder-30B-A3B-Instruct", SF),
    ("siliconflow-deepseek", "openai/deepseek-ai/DeepSeek-V3.2", SF),

    # --- DeepInfra ---
    ("deepinfra-qwen-coder", "openai/Qwen/Qwen2.5-Coder-32B-Instruct", DI),
    ("deepinfra-llama3-70b", "openai/meta-llama/Meta-Llama-3.1-70B-Instruct", DI),

    # --- DeepSeek ---
    ("deepseek-chat", "deepseek/deepseek-chat", DS),
    ("deepseek-flash", "deepseek/deepseek-flash", DS),
    ("deepseek-reasoner", "deepseek/deepseek-reasoner", DS),
    ("deepseek-v4-pro", "deepseek/deepseek-v4-pro", DS),
    # role alias used across agent examples
    ("deepseek-coder", "deepseek/deepseek-chat", DS),

    # --- Moonshot / Kimi ---
    ("kimi-k3", "moonshot/kimi-k3", MS),
    ("kimi-k2-7-code", "moonshot/kimi-k2.7-code", MS),
    ("kimi-k3-1-pro", "moonshot/kimi-k3.1-pro", MS),
    # role aliases used by the orchestrator
    ("kimi-reasoner", "moonshot/kimi-k3", MS),
    ("kimi-reasoner", "deepseek/deepseek-reasoner", DS),
    ("kimi-coder", "moonshot/kimi-k2.7-code", MS),
    ("kimi-coder", "deepseek/deepseek-chat", DS),
    ("kimi-architect", "moonshot/kimi-k3.1-pro", MS),
    ("kimi-architect", "gemini/gemini-flash-latest", GM),

    # --- Gemini ---
    ("gemini-flash", "gemini/gemini-3.8-flash", GM),
    ("gemini-pro", "gemini/gemini-3.1-pro-preview", GM),
    ("gemini-3.8-flash", "gemini/gemini-3.8-flash", GM),
    ("gemini-payg", "gemini/gemini-3.8-flash", GM),
    ("gemini-payg", "gemini/gemini-flash-latest", GM),
    # role aliases
    ("antigravity-librarian", "gemini/gemini-flash-latest", GM),
    ("antigravity-flash-latest", "gemini/gemini-flash-latest", GM),
    ("antigravity-pro", "gemini/gemini-3.1-pro-preview", GM),
    # embeddings
    ("gemini-embedding", "gemini/gemini-embedding-001", GM),
    ("text-embedding-004", "gemini/gemini-embedding-001", GM),
    ("text-embedding-3-small", "gemini/gemini-embedding-001", GM),

    # --- Anthropic (current model ids for this account) ---
    ("claude-3-5-haiku", "anthropic/claude-haiku-4-5-20251001", AN),
    ("claude-3-5-sonnet", "anthropic/claude-sonnet-4-5-20250929", AN),
    ("claude-3-7-sonnet", "anthropic/claude-sonnet-5", AN),
    ("claude-architect", "anthropic/claude-sonnet-5", AN),
    ("claude-architect", "gemini/gemini-flash-latest", GM),

    # --- Zhipu / GLM (glm-4-flash is retired; glm-4.5-air is the cheap tier) ---
    # Balance on this account is currently exhausted (code 1113), so the route
    # degrades to deepseek-flash instead of failing outright.
    ("glm-utility", "openai/glm-4.5-air", ZP),
    ("glm-utility", "deepseek/deepseek-flash", DS),

    # --- OpenAI (dormant until topped up; falls back to a working provider) ---
    ("openai-gpt4o", "openai/gpt-4o", OA),
    ("openai-gpt4o", "gemini/gemini-3.8-flash", GM),
    ("codex-specialist", "openai/o3-mini", OA),
    ("codex-specialist", "deepseek/deepseek-reasoner", DS),

    # --- Local (ollama; models actually installed on this machine) ---
    # Pulled: qwen2.5-coder:7b (~31 tok/s on the RTX 2000 Ada), llama3.2:1b,
    # deepseek-r1:7b.  Bigger models would run on CPU, so the old local-32b /
    # local-70b / local-lmstudio-120b routes were dropped.
    ("local-ollama", "ollama/qwen2.5-coder:7b", OLLAMA),
    ("local-coder", "ollama/qwen2.5-coder:7b", OLLAMA),
    ("local-reasoner", "ollama/deepseek-r1:7b", OLLAMA),
    ("local-llama", "ollama/llama3.2:1b", OLLAMA),
]

with open(LIVE, encoding="utf-8-sig") as fh:
    live = yaml.safe_load(fh)

model_list = []
for name, model, extra in ROUTES:
    params = {"model": model}
    params.update(extra)
    params["caching"] = True
    model_list.append({"model_name": name, "litellm_params": params})

data = {"model_list": model_list}
for key in ("router_settings", "litellm_settings", "general_settings"):
    if key in live:
        data[key] = live[key]

header = (
    "# SINGLE SOURCE OF TRUTH for the LiteLLM gateway.\n"
    "# Edit here, then run: powershell -ExecutionPolicy Bypass -File orchestrator/deploy_litellm_config.ps1\n"
    "# Do not hand-edit C:\\ProgramData\\litellm\\config\\config.yaml - it is generated.\n"
    "#\n"
    "# Route names cover both conventions in this repo:\n"
    "#   flat provider names (ceo-cockpit: gemini-flash, siliconflow-qwen, deepseek-flash, ...)\n"
    "#   role names         (orchestrator: antigravity-librarian, claude-architect, kimi-coder, ...)\n"
    "#\n"
    "# Removed 2026-10-30: dashscope-* (no DASHSCOPE_API_KEY), vertex-* (no GCP credentials).\n"
    "#\n"
)

body = yaml.safe_dump(data, sort_keys=False, allow_unicode=True, default_flow_style=False, width=120)
with open(OUT, "w", encoding="utf-8", newline="\n") as fh:
    fh.write(header + body)

names = {}
for e in model_list:
    names[e["model_name"]] = names.get(e["model_name"], 0) + 1
print(f"routes (model groups): {len(names)}   entries: {len(model_list)}")
print("dashscope present:", any("dashscope" in n for n in names))
print("vertex present:", any("vertex" in n for n in names))
print("sample:", ", ".join(sorted(names)[:8]), "...")
