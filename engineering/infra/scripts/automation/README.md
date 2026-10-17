# 🤖 Auto Keep All & Dialog Decision Auto-Clicker Service

The **Auto Keep All** service is an automated, background decision-making daemon designed for IDEs, terminal prompts, and AI assistant permission popups. It performs real-time multi-scale OpenCV template matching against screen captures to automatically click approval and confirmation buttons ("Keep All", "Proceed", "Allow Always", "Accept All", "Accept Changes", "Always Run", "Allow Once", "Allow", "Yes Don't Ask").

---

## 🛠️ Architecture & Matching Logic

1. **Multi-Scale Template Matching**: OpenCV (`cv2.matchTemplate`) compares active display screenshots against stored button variants in `../assets/`.
2. **Confidence Thresholding**: Set to `0.88` to eliminate false positive button triggers while capturing greyed-out, dark-mode, or hovered buttons.
3. **Cursor Conflict Guard**: Before moving the mouse or triggering clicks, the service checks if the user is actively moving the cursor. If user activity is detected elsewhere, the automated click is deferred to avoid interfering with manual input.
4. **Adaptive Button Variants**: Includes 35+ asset variants across neutral, grey, hover, dark mode, and unhighlighted states.

---

## 🐧 Linux Service Setup

### 1. Requirements
```bash
sudo apt-get install xdotool ydotool spectacle
pip install opencv-python numpy pillow
```

### 2. Manual Background Launch
```bash
./run_auto_keep.sh
```

### 3. Systemd Service Setup
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
```bash
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
