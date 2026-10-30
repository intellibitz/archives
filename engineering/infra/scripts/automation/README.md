# Approval Button Auto-Clicker

One implementation: `ceo-cockpit/automation/AutoClickDecisionsNative.exe`
(UI Automation, Windows). It watches the **foreground window** and clicks
approval/decision buttons when they appear:

`Accept All`, `Accept All 2`, `Accept Changes`, `Accept edits`, `Accept`,
`Keep All`, `Proceed`, `Yes, Don't Ask`, `Allow Always`, `Always Allow`,
`Always Run`, `Allow Once`, `Allow`.

The previous OpenCV template engine and its captures were removed — two engines
watching the same dialog produced double clicks, competing focus changes, and
two different ideas of what a "button" was.

## Behaviour guarantees

| Rule | Why |
| --- | --- |
| Only the **foreground window** is inspected | The old build scanned the whole desktop and clicked in background windows, stealing focus |
| **Exact label match** (normalized: whitespace, `&`, trailing `…`/`.`, case) | The old build used `StartsWith`, so a chat bubble reading "Acceptance failed on the merged main state…" or a notification "Allow connections from other machines and vscode.dev/agents" counted as a button |
| Names longer than 40 characters are never buttons | Keeps prose out of the target set |
| **One click per element, 4 s cooldown, max 3 attempts, then a 120 s blacklist** | The old build re-clicked the same stubborn element every poll, forever |
| No click while the user typed/clicked within 800 ms, or is holding the mouse button | Nothing fires while you are working |
| The mouse pointer is **never moved** | The old mouse fallback teleported the pointer to the element's bounding-box centre |
| Per-Monitor-V2 DPI awareness | The old build was DPI-unaware, so logical/physical coordinates diverged and clicks landed off-target on scaled displays |
| Single instance (named mutex) | Two launchers used to start two clickers |

## How a click is delivered

It tries these in order and stops at the first that works:

1. `InvokePattern` — native buttons and most web buttons.
2. `TogglePattern` / `SelectionItemPattern` — checkboxes, list rows.
3. **Posted click** — `WM_LBUTTONDOWN`/`WM_LBUTTONUP` sent to the element's own
   HWND with client coordinates. Works for Chromium/Electron buttons that expose
   no `InvokePattern`, and the pointer never moves.
4. **Focus + Space** — only if focus provably lands on that exact element
   (`AutomationElement.FocusedElement` must match by runtime id), so a stray
   Space can never be typed into an editor.
5. Physical mouse click with cursor save/restore — only with
   `--allow-mouse-fallback`.

Anything else is logged as "no action pattern available; not clicking." — the
clicker would rather do nothing than click blind.

## Start / stop

```powershell
cd ceo-cockpit\automation

# Live (also stops any previous instance)
powershell -ExecutionPolicy Bypass -File .\run_auto_click_decisions.ps1

# See decisions without touching input
powershell -ExecutionPolicy Bypass -File .\run_auto_click_decisions.ps1 -DryRun

# Stop
Get-Process AutoClickDecisionsNative | Stop-Process
```

Register the per-user logon task (`ApprovalButtonAutoClicker`):

```powershell
powershell -ExecutionPolicy Bypass -File .\install_service.ps1
```

## Rebuild

In-box .NET Framework compiler, C# 5 only:

```powershell
csc /nologo /target:exe /platform:anycpu /optimize+ /debug- `
    /r:UIAutomationClient.dll /r:UIAutomationTypes.dll /r:WindowsBase.dll `
    /out:AutoClickDecisionsNative.exe AutoClickDecisionsNative.cs
```

## Verifying it

```powershell
# Exact-match table + normalisation rules, offline
.\AutoClickDecisionsNative.exe --self-test

# Per-scan detail: how many Button elements the foreground window exposes and
# why each one was skipped
.\AutoClickDecisionsNative.exe --once --debug
```

Diagnostics for the two delivery paths: `--force-posted-click`, `--probe-focus`,
`--no-post-click`, `--no-keyboard`. `AUTOCLICK_IDLE_GUARD_MS=0` disables the
"user is active" guard for short controlled tests.

Health: `ceo-cockpit/scripts/verify_health.ps1` reports `ACTIVE (Live Scanning)`
from `%TEMP%\auto_click_decisions.heartbeat`, which the clicker rewrites every
5 s.

## Logs

- Log: `%TEMP%\auto_click_decisions.log`
- PID: `%TEMP%\auto_click_decisions.pid`
- Heartbeat: `%TEMP%\auto_click_decisions.heartbeat`

Every click line states the label, the window title, the target point, the
attempt number, and the delivery method. A `mouse fallback` line means the
opt-in path was enabled.

## Tuning

Constants at the top of `AutoClickDecisionsNative.cs`:
`PollIntervalMs`, `DefaultUserIdleGuardMs`, `SameElementCooldownMs`,
`MaxAttempts`, `BlacklistMs`, `MaxLabelLength`.
