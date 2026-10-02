# CachyOS & Arch Linux Maintenance, AI Tooling & System Fixes

This document records system maintenance, hook performance optimizations, permission fixes, and AI desktop tooling installations for CachyOS / Arch Linux.

---

## ⚡ 1. Pacman / Cachy-Update Hang Fix (`limine-mkinitcpio-install`)

### Symptom
`cachy-update` or `pacman` appears hung for 10–15+ minutes at **"Updating linux initcpios..."** during system updates involving packages with large file lists (e.g., `linux-firmware`).

### Cause
In `/usr/share/libalpm/scripts/limine-mkinitcpio-install`, `collect_kernels()` receives every target file path from pacman's `NeedsTargets` hook. On line 62, the script executed `add_kernel_package "$line"` for **every single file path** (e.g. ~15,000 files in `/usr/lib/firmware/`), triggering thousands of failing `pacman -Ql` queries sequentially.

### Fix Applied
Modify `/usr/share/libalpm/scripts/limine-mkinitcpio-install` at line 62 so `add_kernel_package` is only executed when `$line` is a valid package name without slashes `/` and `rebuild_all` is false:

```bash
# Before:
elif add_kernel_package "$line"; then

# After:
elif [[ "$line" != */* ]] && ! $rebuild_all && add_kernel_package "$line"; then
```

**Result:** Reduces pacman hook execution time from **15 minutes** down to **50 milliseconds**.

---

## 🔒 2. System Directory Permission Mismatch Fix (`/opt`)

### Symptom
Pacman outputs `warning: directory permissions differ on /opt` during package installations.

### Cause
`/opt` directory had non-standard group permissions (`775 root:ramadoss`), whereas the `filesystem` package expects standard mode `755` (`drwxr-xr-x`) owned by `root:root`.

### Fix Applied
```bash
sudo chmod 755 /opt
sudo chown root:root /opt
```

---

## 🛠️ 3. AUR Package Build Cache Cleanup (`fsearch`)

### Symptom
`fsearch` AUR update fails during `prepare()` with:
`Reversed (or previously applied) patch detected! Skipping patch.`

### Cause
Stale patch file `0001-fix_new_window.patch` sitting in `paru`'s build cache (`~/.cache/paru/clone/fsearch/`). Upstream `fsearch` 0.3.2 already merged the fix, so re-applying the leftover patch failed.

### Fix Applied
Cleaned stale build cache directories and updated `fsearch` to `0.3.2-1`:
```bash
rm -rf ~/.cache/paru/clone/fsearch ~/.cache/yay/fsearch
```

---

## 🤖 4. AI Desktop & CLI Applications Installed

| Application / Tool | Version | Executable / Path | Package / Type |
| :--- | :---: | :--- | :--- |
| **Claude AI Desktop** | `2.9939.4` | `/usr/bin/claude-desktop` | Official `cachyos/claude-desktop` Electron App |
| **Codex Desktop App** | `26.727.51351` | `/usr/bin/codex-app-linux` | `aur/codex-app-unofficial` Standalone App |
| **OpenAI Codex CLI** | `0.160.0` | `/usr/bin/codex` | Official `cachyos-extra-v3/openai-codex` CLI |
| **DeepSeek Harness (`dsh`)** | `0.2.0-rc.2` | `~/.npm-global/bin/dsh` | `@deepseek-ai/dsh` Web & Terminal Harness |
| **Roo Code Desktop** | `v3.54.0` | VS Code Extension (`rooveterinaryinc.roo-cline`) | Autonomous Agent Extension & `roo` CLI |
| **OpenHands Web (Non-Docker)** | `1.34.0` | `~/.local/bin/openhands-web` | Native Standalone Web Engine (`openhands-ai`) |
