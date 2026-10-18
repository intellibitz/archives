# 🤖 Auto Keep All & Dialog Decision Auto-Clicker Service

The **Auto Keep All** service is an automated, background decision-making daemon designed for IDEs, terminal prompts, and AI assistant permission popups. It performs real-time multi-scale OpenCV template matching against screen captures to automatically click approval and confirmation buttons ("Keep All", "Proceed", "Allow Always", "Accept All", "Accept Changes", "Always Run", "Allow Once", "Allow", "Yes Don't Ask").

---

## 🛠️ Architecture & Matching Logic

1. **Multi-Instance Split-Window Matching**:
   - Replaced single-point matching (`minMaxLoc`) with multi-instance Non-Maximum Suppression (NMS) peak extraction (`find_all_template_matches`).
   - When multiple "Accept All" or confirmation buttons appear simultaneously (e.g. across split editor windows, multi-pane diffs, or multiple pending popups), **every single instance is discovered and clicked in a single batch sweep**, followed by natural cursor restoration.
2. **Color & Highlight Agnostic Matching**:
   - Matches buttons regardless of whether they are vibrant blue, green, purple, unhighlighted neutral, grey, or dark-mode themed.
   - Removed artificial color saturation gates so buttons with low or zero saturation (e.g. dark grey or neutral unhighlighted buttons in dark themes) are never dropped.
   - Calibrated matching threshold to **`0.78`**, ensuring resilient matching across font antialiasing (ClearType / FreeType) and subpixel rendering differences while geometric validation prevents text false-positives.
3. **Multi-Scale DPI Coverage**:
   - Tests across native and standard display scaling factors (`1.0x`, `1.25x`, `1.50x`, `0.90x`, `1.10x`, `1.20x`, `1.35x`, `1.75x`, `2.00x`, `0.80x`), ensuring full compatibility with fractional scaling on 1080p, 1440p, and 4K displays.
4. **Dynamic Template Discovery**:
   - Automatically loads all button variants in `../scripts/assets/` using wildcard patterns (`accept-all*.png`, `accept_all*.png`, `accept-changes*.png`, etc.). All 15+ "Accept All" variants are loaded.
5. **Retry State Machine & Cooldown**:
   - Tracks button presence via coordinate hashes.
   - Retries click after `3.5s` cooldown up to 3 times if the dialog requires multiple clicks or initial click only focused the window.
   - Automatically blacklists static elements after 3 failed dismissals to avoid infinite loops.

---

## 🖱️ Natural Cursor Handling & Restoration

The service uses a **Zero-Disturbance Natural Cursor Restoration** model:

1. **Active Drag & Typing Protection**:
   - If the user is holding down the left mouse button (dragging, selecting text, resizing windows), automated clicks are **deferred**.
   - If the user typed or moved the mouse within the last 350ms, clicks are **deferred** until a natural pause.
2. **In-Place Clicking**:
   - If the user's cursor is already positioned over or near the button, the click is dispatched in place without moving or snapping the cursor.
3. **Natural Position Restoration**:
   - The user's active cursor coordinate `(orig_x, orig_y)` and foreground window handle are recorded before any movement.
   - The cursor moves to each detected button in sequence, executes a crisp 35ms hold click, and immediately returns to `(orig_x, orig_y)`.
   - If the user moved the mouse during the click sequence, their manual movement is respected and the cursor is left in place.
   - Keyboard window focus is restored back to the user's original application if shifted.

---

## 🐧 Linux Service Setup

### 1. Requirements
```bash
sudo apt-get install xdotool ydotool spectacle
# or on Arch/CachyOS: sudo pacman -S xdotool ydotool spectacle
pip install opencv-python numpy pillow
```

### 2. Manual Background Launch
```bash
./run_auto_keep.sh
```

### 3. Systemd User Service Setup
Copy `auto-keep-all.service` to systemd user services:
```bash
mkdir -p ~/.config/systemd/user/
cp auto-keep-all.service ~/.config/systemd/user/
systemctl --user daemon-reload
systemctl --user enable --now auto-keep-all.service
```

---

## 🪟 Windows Service & Background Setup

### 1. Requirements
```powershell
pip install opencv-python numpy pillow
```

### 2. Manual Background Launch
Run via PowerShell or Batch:
```powershell
# PowerShell
.\run_auto_keep.ps1

# Batch
run_auto_keep.bat
```

### 3. Register as Windows Background Service (Scheduled Task)
To start automatically at system boot/logon and run silently in the background:
```powershell
powershell -ExecutionPolicy Bypass -File .\install_auto_keep_service.ps1
```

### 4. Logging & Diagnostics
- **Windows Log File**: `%TEMP%\keep_all_auto.log`
- **Linux Log File**: `/tmp/keep_all_auto.log`
