# AI Orchestrator & LiteLLM Gateway

The LiteLLM gateway runs as a Windows service (`litellm`, NSSM, LocalSystem) on
**http://127.0.0.1:4000** and fronts every model provider used by
`orchestrator/` and `ceo-cockpit/`.

## One config, deployed from this repo

```
orchestrator/litellm_config.yaml          <- source of truth (edit this, commit it)
C:\ProgramData\litellm\config\config.yaml <- generated; never hand-edit
```

Deploy after any change:

```powershell
powershell -ExecutionPolicy Bypass -File orchestrator\deploy_litellm_config.ps1
```

The deploy script validates the YAML, backs up the previous deployed copy,
copies the new one, restarts the service and waits for `/health/liveliness`.
A cold start takes ~50 s (litellm validates every deployment at boot).

Verify every route actually answers:

```powershell
powershell -ExecutionPolicy Bypass -File orchestrator\verify_litellm_routes.ps1
# include the local-* routes too (needs ollama / lm-studio running)
powershell -ExecutionPolicy Bypass -File orchestrator\verify_litellm_routes.ps1 -IncludeLocal
```

## Route naming

The same config serves two conventions, so both are present:

| Convention | Used by | Examples |
| --- | --- | --- |
| Flat provider names | `ceo-cockpit` | `gemini-flash`, `gemini-pro`, `deepseek-flash`, `deepseek-reasoner`, `siliconflow-qwen`, `siliconflow-qwen-coder` |
| Role names | `orchestrator` | `antigravity-librarian`, `antigravity-pro`, `claude-architect`, `deepseek-coder`, `kimi-coder`, `kimi-reasoner`, `kimi-architect`, `glm-utility`, `codex-specialist`, `openai-gpt4o` |

Repeated `model_name` entries are fallbacks within the group: litellm tries them
in order. Role routes deliberately degrade to a working provider so an
unfunded provider does not take the role down (e.g. `glm-utility` → zhipu
`glm-4.5-air`, then `deepseek-flash`).

## Credentials

All keys are referenced as `os.environ/NAME`, never literals:

| Env var | Provider | Scope |
| --- | --- | --- |
| `SILICONFLOW_API_KEY` | SiliconFlow (`.com` platform) | user + machine |
| `DEEPINFRA_API_KEY` | DeepInfra | user + machine |
| `DEEPSEEK_API_KEY` | DeepSeek | user + machine |
| `MOONSHOT_API_KEY` / `KIMI_API_KEY` | Moonshot / Kimi | user + machine |
| `GEMINI_API_KEY` / `GOOGLE_API_KEY` | Google AI Studio | user + machine |
| `ANTHROPIC_API_KEY` | Anthropic | machine |
| `ZHIPU_API_KEY` / `ZAI_API_KEY` | Zhipu / Z.ai | user + machine |
| `OPENAI_API_KEY` | OpenAI (dormant until funded) | user |

The service runs as **LocalSystem**, so it only sees **machine**-scope
variables. Anything a route needs must exist at machine scope, not just user
scope. `ANTHROPIC_WORKSPACE_ID` is baked into the anthropic entries as a header
because this account's key is not workspace-scoped.

## Adding a route

```yaml
- model_name: my-new-route
  litellm_params:
    model: openai/<provider-model>     # keep the openai/ prefix for OpenAI-compatible endpoints
    api_base: https://api.example.com/v1
    api_key: os.environ/MY_API_KEY
    caching: true
```

Then deploy and run the verification script. Known current gaps:

- `local-*` routes need ollama (`:11434`) and lm-studio (`:1234`) running; they
  are the only routes that fail today.
- `dashscope-*` and `vertex-*` were removed (no credentials).
- Zhipu balance is exhausted (code 1113), so `glm-utility` degrades to DeepSeek.

## Service management

```powershell
Get-Service litellm
Restart-Service litellm
Get-Content C:\ProgramData\litellm\logs\litellm.log -Tail 50
```

NSSM is configured with `AppKillProcessTree=1` and `AppStopMethodSkip=0` so a
restart reliably replaces the worker; without it a stop could hang for a minute
and leave the old process holding port 4000.
