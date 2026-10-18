# Approval Button Auto-Clicker

This utility watches the desktop and clicks the configured decision buttons when they appear. It includes `Accept All`, `Accept All 2`, `Accept Changes`, `Accept`, `Keep All`, `Proceed`, `Allow Always`, `Always Run`, `Allow Once`, `Allow`, and `Yes Don't Ask`.

It uses the screenshot templates in `../assets`, grayscale matching, and multiple scales to cover themes, hover states, and display scaling. It searches the full captured screen, retries visible buttons, and logs failed capture or input attempts. The Linux and Windows runners use the matching platform implementation.

## Linux

Install `opencv-python`, `numpy`, and the system screenshot and input tools available for your desktop (`spectacle` or ImageMagick `import`, plus `ydotool` or `xdotool`). `evdev` is optional.

Run in the current session:

```bash
./run_auto_click_decisions.sh
```

For a user systemd service, copy `auto-click-decisions.service` into `~/.config/systemd/user/`. Update its `WorkingDirectory` and `ExecStart` to the absolute path of this checkout, then run:

```bash
systemctl --user daemon-reload
systemctl --user enable --now auto-click-decisions.service
```

The unit requires access to the active graphical session and its screenshot/input tools.

## Windows

Install Python packages:

```powershell
python -m pip install opencv-python numpy pillow
```

Run with `./run_auto_click_decisions.ps1` or `run_auto_click_decisions.bat`. To register or refresh the per-user logon task, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\install_auto_click_decisions_service.ps1
```

The installer replaces the previous task with `ApprovalButtonAutoClicker` when Windows permits task registration. If the old task belongs to another account or Windows blocks task changes, the compatibility launcher at `run_auto_keep.ps1` still starts the renamed clicker; the installer also starts it in the current desktop session.

## Logs

- Windows: `%TEMP%\auto_click_decisions.log`
- Linux: `/tmp/auto_click_decisions.log`
