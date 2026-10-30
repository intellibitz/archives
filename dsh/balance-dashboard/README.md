# Balances & billing

One CEO dashboard for provider money, rendered as a panel in the Harness Web UI:

- **DeepSeek** — live balance from `https://api.deepseek.com/user/balance`
  (total, granted, topped-up, in the account's own currency).
- **Claude** — no per-key balance endpoint exists; the card shows recorded spend
  and, when `ANTHROPIC_ADMIN_KEY` is set, the organization cost report for the
  last 30 days.
- **Gemini** — Google AI Studio exposes no balance through the Gemini API; the
  card shows recorded spend and says so rather than inventing a number.

Spend comes from the metered ledger this machine already writes for every model
call (micro-USD per record): today, last 7 days, all time, calls, tokens in/out,
the top models, and a 14-day trend stacked by provider.

## Where it lives

- Panel: sidebar icon **Balances** (order 22) → `main` panel keyed `balances`.
- Host route: `/balance-dashboard/data` (the page) and
  `/api/balance-dashboard` (programmatic). Both answer on loopback only and
  refuse any other `Host` with 403. No CORS headers are sent, so another origin
  cannot read them; no credential is read, echoed or logged.
- Keys: read from the process environment first, then `~/.susi/cloud.env`.
  They never leave the Host half.

## Configuration

| Variable | Meaning |
| --- | --- |
| `BALANCE_LEDGER_PATH` | Ledger JSONL to aggregate (default: the susi repo's `out/live/ledger.jsonl`, then `~/.susi/ledger.jsonl`). |
| `ANTHROPIC_ADMIN_KEY` | Optional Anthropic admin key for the organization cost report. |

The page refreshes every 60 seconds and on demand, and the host caches the
payload for 30 seconds so opening the panel does not hammer the provider APIs.
