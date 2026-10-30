# DSH harness plugins

Versioned sources for the plugins and profile patch this machine actually runs.
The live copies live under `~/.dsh`, which is not a git repository, so these are
the authoritative backups.

```
dsh/
├── balance-dashboard/          # the Balances page plugin (host + client halves)
│   ├── index.js                #   Host: /api/balance-dashboard + page data route
│   ├── client.js               #   Client: sidebar panel + Balances page
│   ├── cordis.patch.yml        #   Loader row for the plugin
│   ├── package.json            #   dsh.client + dsh.bundle metadata
│   ├── locale/{en,zh}.json
│   └── icon.svg
├── profile/cordis.patch.yml    # the desktop profile's patch layer
└── update_model_catalogue.py   # regenerates the litellm model list
```

## What the balance dashboard shows

- **Budget headroom** from the CEO Cockpit governor (`:8080/api/budget`): daily
  and monthly caps, spend, remaining, usage bars, cached savings, circuit state.
- **Every routed model** from the LiteLLM gateway (`:4000/v1/models`), not just
  the models that happen to have spend: availability, calls, today, all-time.
- **Per-provider spend** from the metered ledger, plus a real balance where a
  provider exposes one (DeepSeek) and an explicit "no balance endpoint" note
  where none exists (Google AI Studio, SiliconFlow, DeepInfra, Moonshot, Zhipu).

No credential is stored here: the host half reads keys from the process
environment or `~/.secrets/.env` at request time, and never echoes them.

## How the profile consumes these

`~/.dsh/profiles/desktop/package.json` links the package by path:

```json
"@local/balance-dashboard": "link:C:/Users/user/.dsh/packages/balance-dashboard"
```

and lists it in `dsh.profile.bundles`. The profile patch layer lives at
`~/.dsh/profiles/desktop/cordis.patch.yml`.

## Sync after editing here

```powershell
# plugin source -> the linked live copy (HMR picks it up; refresh the window for client changes)
Copy-Item .\dsh\balance-dashboard\* "$env:USERPROFILE\.dsh\packages\balance-dashboard" -Recurse -Force

# profile patch -> the live profile (keep a backup; the loader reloads it)
Copy-Item .\dsh\profile\cordis.patch.yml "$env:USERPROFILE\.dsh\profiles\desktop\cordis.patch.yml" -Force
```

## Regenerating the model catalogue

`profile/cordis.patch.yml` declares the models the IDE's `litellm` provider
offers, under `providers.litellm.models`. Regenerate it whenever the gateway's
route list changes:

```powershell
python .\dsh\update_model_catalogue.py
```

It reads the gateway's live `/v1/models` with the master key from
`~/.secrets/.env`, assigns context window / max output per model family, and
rewrites only that block. Back up the profile patch first if you have local
edits.

## Notes

- The Anthropic workspace id in `profile/cordis.patch.yml` is redacted to
  `wrkspc_REPLACE_WITH_YOUR_WORKSPACE_ID`; fill in your own before deploying.
- `apiKeyEnv` values are names, not secrets. The gateway key must exist in the
  environment (or the credential store) the Harness boots with.
- Provider API keys are never part of this directory.
