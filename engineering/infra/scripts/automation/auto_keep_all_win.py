import ctypes
from ctypes import wintypes
import glob
import os
import sys
import time
import numpy as np
import cv2
from PIL import ImageGrab

# Enforce Per-Monitor High DPI Awareness on Windows
if sys.platform == "win32":
    try:
        ctypes.windll.shcore.SetProcessDpiAwareness(2)
    except Exception:
        try:
            ctypes.windll.user32.SetProcessDPIAware()
        except Exception:
            pass

# --- Configuration & Assets ---
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
if os.path.isdir(os.path.abspath(os.path.join(SCRIPT_DIR, "../assets"))):
    ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../assets"))
elif os.path.isdir(os.path.abspath(os.path.join(SCRIPT_DIR, "assets"))):
    ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "assets"))
else:
    ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../scripts/assets"))

# Dynamic pattern resolution ensures every button variation is loaded
TEMPLATE_PATTERNS = {
    "Accept All": ["accept-all.png", "accept-all-*.png", "accept_all.png", "accept_all-*.png"],
    "Accept All 2": ["accept-all2*.png", "accept_all2*.png"],
    "Accept Changes": ["accept-changes*.png", "accept_changes*.png", "acceot-changes*.png"],
    "Accept": ["accept-inline*.png", "accept_inline*.png"],
    "Keep All": ["keep-all*.png"],
    "Proceed": ["proceed*.png"],
    "Allow Always": ["allow-always*.png"],
    "Always Run": ["always-run*.png"],
    "Allow Once": ["allow-once*.png"],
    "Allow": ["allow.png", "allow2.png"],
    "Yes Don't Ask": ["yes-dontask*.png"],
}

# Priority ranking: clicking global dismiss/accept first ensures cascading diffs resolve cleanly
BUTTON_PRIORITY = {
    "Accept All": 1,
    "Accept All 2": 2,
    "Accept Changes": 3,
    "Accept": 4,
    "Keep All": 5,
    "Proceed": 6,
    "Allow Always": 7,
    "Always Run": 8,
    "Allow Once": 9,
    "Allow": 10,
    "Yes Don't Ask": 11,
}

# Matching threshold: 0.86 matches true interactive buttons across slight antialiasing
# differences while candidate geometric & saturation filters reject false positives.
THRESHOLDS = {
    "Accept All": 0.86,
    "Accept All 2": 0.86,
    "Accept Changes": 0.86,
    "Accept": 0.86,
    "Keep All": 0.86,
    "Proceed": 0.86,
    "Allow Always": 0.86,
    "Always Run": 0.86,
    "Allow Once": 0.86,
    "Allow": 0.86,
    "Yes Don't Ask": 0.86,
}
DEFAULT_THRESHOLD = 0.86
CONFIDENCE_THRESHOLD = 0.86

if sys.platform == "win32":
    TEMP_DIR = os.environ.get("TEMP", os.path.expanduser("~"))
    LOG_FILE = os.path.join(TEMP_DIR, "keep_all_auto.log")
    PID_FILE = os.path.join(TEMP_DIR, "keep_all_auto.pid")
else:
    LOG_FILE = "/tmp/keep_all_auto.log"
    PID_FILE = "/tmp/keep_all_auto.pid"

CHECK_INTERVAL = 1.0
RETRY_COOLDOWN_SECONDS = 2.0
MAX_RETRIES = 5
BLACKLIST_DURATION_SECONDS = 10.0

active_clicked_buttons = {}  # loc_key -> {"first_seen": t, "last_clicked": t, "count": n}
blacklisted_locations = {}   # loc_key -> unblock_time

# Win32 SendInput Structures (64-bit compliant)
if sys.platform == "win32":
    class MOUSEINPUT(ctypes.Structure):
        _fields_ = [
            ("dx", wintypes.LONG),
            ("dy", wintypes.LONG),
            ("mouseData", wintypes.DWORD),
            ("dwFlags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_size_t),
        ]

    class INPUT(ctypes.Structure):
        class _INPUT(ctypes.Union):
            _fields_ = [("mi", MOUSEINPUT)]
        _anonymous_ = ("_input",)
        _fields_ = [
            ("type", wintypes.DWORD),
            ("_input", _INPUT),
        ]

    INPUT_MOUSE = 0
    MOUSEEVENTF_MOVE = 0x0001
    MOUSEEVENTF_LEFTDOWN = 0x0002
    MOUSEEVENTF_LEFTUP = 0x0004
    MOUSEEVENTF_ABSOLUTE = 0x8000


def log(msg):
    t = time.strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{t}] {msg}\n"
    try:
        with open(LOG_FILE, "a", encoding="utf-8", buffering=1) as f:
            f.write(line)
    except Exception:
        pass
    try:
        print(line.strip(), flush=True)
    except Exception:
        pass


def attach_input_desktop():
    """Attaches background thread to the active user interactive desktop."""
    if sys.platform == "win32":
        try:
            hdesk = ctypes.windll.user32.OpenInputDesktop(0, False, 0x01FF)
            if hdesk:
                ctypes.windll.user32.SetThreadDesktop(hdesk)
        except Exception:
            pass


def get_mouse_xy():
    """Returns (x, y) tuple of current mouse cursor position."""
    if sys.platform == "win32":
        attach_input_desktop()
        class POINT(ctypes.Structure):
            _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]

        pt = POINT()
        try:
            ctypes.windll.user32.GetCursorPos(ctypes.byref(pt))
            return pt.x, pt.y
        except Exception:
            return None, None
    return None, None


def set_mouse_xy(x, y):
    """Position mouse cursor at (x, y)."""
    if sys.platform == "win32":
        attach_input_desktop()
        ctypes.windll.user32.SetCursorPos(int(x), int(y))


def is_mouse_button_down():
    """Returns True if user is currently holding down the left mouse button (e.g. dragging/selecting)."""
    if sys.platform == "win32":
        try:
            state = ctypes.windll.user32.GetAsyncKeyState(0x01)
            return (state & 0x8000) != 0
        except Exception:
            return False
    return False


def activate_target_window(cx, cy):
    """Brings the window under (cx, cy) to foreground if not already focused."""
    if sys.platform == "win32":
        attach_input_desktop()
        user32 = ctypes.windll.user32
        class POINT(ctypes.Structure):
            _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]

        pt = POINT(int(cx), int(cy))
        hwnd = user32.WindowFromPoint(pt)
        if hwnd:
            root = user32.GetAncestor(hwnd, 2)  # GA_ROOT
            if root:
                fg = user32.GetForegroundWindow()
                if fg != root:
                    user32.SetForegroundWindow(root)
                    time.sleep(0.04)


def dispatch_click(cx, cy, name=""):
    """
    Sends an absolute click with proper hover dwell and mouse down hold.
    Ensures Chromium/Electron processes hit-testing and DOM click event dispatch.
    """
    if sys.platform != "win32":
        return False

    attach_input_desktop()
    user32 = ctypes.windll.user32

    # 1. Activate target window if needed
    activate_target_window(cx, cy)

    # 2. Position cursor
    user32.SetCursorPos(int(cx), int(cy))

    # 3. Dwell 60ms so Chromium / Electron receives WM_MOUSEMOVE, triggers hover & hit-test
    time.sleep(0.06)

    # 4. Calculate normalized absolute coordinates (0 - 65535)
    sw = max(1, user32.GetSystemMetrics(0))
    sh = max(1, user32.GetSystemMetrics(1))
    norm_x = int(cx * 65535 / (sw - 1))
    norm_y = int(cy * 65535 / (sh - 1))

    # 5. Mouse Down via SendInput
    inp_down = INPUT()
    inp_down.type = INPUT_MOUSE
    inp_down.mi = MOUSEINPUT(
        norm_x, norm_y, 0,
        MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_LEFTDOWN, 0, 0
    )
    user32.SendInput(1, ctypes.byref(inp_down), ctypes.sizeof(INPUT))

    # Hold down for 60ms (realistic button press duration)
    time.sleep(0.06)

    # 6. Mouse Up via SendInput
    inp_up = INPUT()
    inp_up.type = INPUT_MOUSE
    inp_up.mi = MOUSEINPUT(
        norm_x, norm_y, 0,
        MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_LEFTUP, 0, 0
    )
    user32.SendInput(1, ctypes.byref(inp_up), ctypes.sizeof(INPUT))

    # 7. Post-click dwell: Crucial for Chromium/Electron to fire the DOM click event
    time.sleep(0.12)
    return True


def dispatch_batch_clicks(buttons_to_click):
    """
    Executes clicks for all detected buttons in priority order.
    Restores cursor smoothly only after all clicks complete, without stealing window focus.
    """
    if not buttons_to_click:
        return False

    if is_mouse_button_down():
        log("Notice: User holding mouse button; deferring batch clicks.")
        return False

    orig_mx, orig_my = get_mouse_xy()

    # Sort buttons by priority (e.g. Accept All before inline diffs)
    sorted_buttons = sorted(
        buttons_to_click,
        key=lambda b: BUTTON_PRIORITY.get(b[0], 99)
    )

    last_target = None

    for (name, x, y, w, h, score, loc_key) in sorted_buttons:
        cx, cy = x + w // 2, y + h // 2
        log(f"Clicking '{name}' at ({cx}, {cy}) (Conf: {score:.2f})...")
        dispatch_click(cx, cy, name)
        last_target = (cx, cy)
        time.sleep(0.10)

    # Natural Cursor Restoration: only after entire batch has dispatched and settled
    if orig_mx is not None and orig_my is not None and last_target:
        lcx, lcy = last_target
        curr_mx, curr_my = get_mouse_xy()
        if curr_mx is not None and curr_my is not None:
            # Only restore if user hasn't physically moved mouse elsewhere
            dist = abs(curr_mx - lcx) + abs(curr_my - lcy)
            if dist < 30:
                time.sleep(0.05)
                set_mouse_xy(orig_mx, orig_my)

    return True


def get_screenshot():
    """Captures desktop screen into OpenCV image array."""
    try:
        attach_input_desktop()
        pil_img = ImageGrab.grab()
        img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
        return img
    except Exception as e:
        log(f"Screenshot error: {e}")
        return None


def find_all_template_matches(screen_gray, template_gray, thresh):
    """
    Finds ALL distinct occurrences of template across the screen above thresh.
    Enables clicking multiple buttons simultaneously across split windows.
    Returns: list of (x, y, w, h, score) sorted by score descending.
    """
    scales = [1.00, 1.25, 1.50, 0.90, 1.10, 1.20, 1.35, 1.75, 2.00, 0.80]
    all_raw_matches = []

    for scale in scales:
        if scale == 1.0:
            resized_tpl = template_gray
        else:
            w = int(template_gray.shape[1] * scale)
            h = int(template_gray.shape[0] * scale)
            if w <= 0 or h <= 0 or w > screen_gray.shape[1] or h > screen_gray.shape[0]:
                continue
            interpolation = cv2.INTER_AREA if scale < 1.0 else cv2.INTER_CUBIC
            resized_tpl = cv2.resize(template_gray, (w, h), interpolation=interpolation)

        res = cv2.matchTemplate(screen_gray, resized_tpl, cv2.TM_CCOEFF_NORMED)
        y_coords, x_coords = np.where(res >= thresh)

        if len(x_coords) > 0:
            th_w, th_h = resized_tpl.shape[1], resized_tpl.shape[0]
            scores = res[y_coords, x_coords]
            sort_idx = np.argsort(-scores)
            min_dist_x = max(20, th_w // 2)
            min_dist_y = max(15, th_h // 2)

            for idx in sort_idx:
                cand_x = int(x_coords[idx])
                cand_y = int(y_coords[idx])
                s = float(scores[idx])
                if not any(abs(cand_x - rx) < min_dist_x and abs(cand_y - ry) < min_dist_y for rx, ry, _, _, _ in all_raw_matches):
                    all_raw_matches.append((cand_x, cand_y, th_w, th_h, s))

    all_raw_matches.sort(key=lambda m: m[4], reverse=True)
    distinct_matches = []
    for x, y, w, h, score in all_raw_matches:
        cx, cy = x + w // 2, y + h // 2
        if not any(abs(cx - (dx + dw // 2)) < 30 and abs(cy - (dy + dh // 2)) < 20 for dx, dy, dw, dh, _ in distinct_matches):
            distinct_matches.append((x, y, w, h, score))

    return distinct_matches


def is_valid_button_candidate(screen, x, y, w, h, tpl_info):
    """Verifies that a matched region represents a true interactive button."""
    if w < 35 or h < 16 or h > 75:
        return False
    aspect_ratio = w / float(h)
    if aspect_ratio < 1.1 or aspect_ratio > 8.5:
        return False

    sh, sw = screen.shape[:2]
    if x < 0 or y < 0 or (x + w) > sw or (y + h) > sh:
        return False

    # Dialog buttons never appear in the extreme left activity/tree margin
    if x < 100:
        return False

    cand_bgr = screen[y:y+h, x:x+w]
    if cand_bgr.size == 0:
        return False

    # Check candidate region contrast
    cand_gray = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2GRAY)
    if cand_gray.std() < 8.0:
        return False

    # Color / Saturation validation:
    # If the template is a colored button (e.g. blue Accept All with sat > 40),
    # verify candidate region also has color saturation, rejecting grey editor/sidebar highlights.
    tpl_sat = tpl_info.get("sat", 0) if isinstance(tpl_info, dict) else 0
    if tpl_sat > 40:
        cand_hsv = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2HSV)
        cand_sat = float(cand_hsv[:, :, 1].mean())
        min_required_sat = max(35.0, tpl_sat * 0.30)
        if cand_sat < min_required_sat:
            return False

    return True


def load_templates():
    """Dynamically loads all template variants matching group patterns."""
    loaded = {}
    for name, patterns in TEMPLATE_PATTERNS.items():
        tpl_list = []
        seen_hashes = set()

        matched_files = set()
        for p in patterns:
            matched_files.update(glob.glob(os.path.join(ASSETS_DIR, p)))

        for path in sorted(matched_files):
            if os.path.exists(path):
                tpl = cv2.imread(path)
                if tpl is not None:
                    tpl_gray = cv2.cvtColor(tpl, cv2.COLOR_BGR2GRAY)
                    hsv = cv2.cvtColor(tpl, cv2.COLOR_BGR2HSV)
                    sat_mean = float(hsv[:, :, 1].mean())

                    if tpl_gray.std() < 8.0:
                        continue

                    h = tpl_gray.tobytes()
                    if h not in seen_hashes:
                        seen_hashes.add(h)
                        tpl_list.append({
                            "gray": tpl_gray,
                            "sat": sat_mean,
                            "path": path
                        })
                else:
                    log(f"Warning: Could not read template file: {path}")

        if tpl_list:
            loaded[name] = tpl_list
        else:
            log(f"Warning: No valid templates found for {name}")

    return loaded


if __name__ == "__main__":
    attach_input_desktop()
    log(f"Auto Keep All Clicker starting on {sys.platform} (SendInput Precision + Multi-Instance Support)...")
    log(f"Assets Directory: {ASSETS_DIR}")
    log(f"Log File: {LOG_FILE}")
    log(f"PID File: {PID_FILE}")

    try:
        with open(PID_FILE, "w", encoding="utf-8") as pf:
            pf.write(str(os.getpid()))
    except Exception as e:
        log(f"Notice: Could not write PID file: {e}")

    loaded_templates = load_templates()

    for name, tpl_list in loaded_templates.items():
        thresh = THRESHOLDS.get(name, DEFAULT_THRESHOLD)
        log(f"Loaded template: {name} ({len(tpl_list)} variants, threshold={thresh})")

    if not loaded_templates:
        log("No templates found in assets directory. Exiting.")
        sys.exit(1)

    last_reload = time.time()

    while True:
        try:
            now = time.time()
            if now - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = now

            expired_blacklists = [k for k, unblock in blacklisted_locations.items() if now >= unblock]
            for k in expired_blacklists:
                del blacklisted_locations[k]

            screen = get_screenshot()
            if screen is not None:
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)

                current_frame_locations = set()
                pending_clicks = []  # [(name, x, y, w, h, score, loc_key)]

                for name, tpl_list in loaded_templates.items():
                    thresh = THRESHOLDS.get(name, DEFAULT_THRESHOLD)
                    for tpl_info in tpl_list:
                        template_gray = tpl_info["gray"]
                        matches = find_all_template_matches(screen_gray, template_gray, thresh)

                        for (x, y, w, h, score) in matches:
                            cx, cy = x + w // 2, y + h // 2
                            loc_key = (cx // 35, cy // 35)
                            current_frame_locations.add(loc_key)

                            if loc_key in blacklisted_locations:
                                continue

                            if not is_valid_button_candidate(screen, x, y, w, h, tpl_info):
                                continue

                            if loc_key in active_clicked_buttons:
                                state = active_clicked_buttons[loc_key]
                                last_time = state["last_clicked"]
                                count = state["count"]

                                if (now - last_time) < RETRY_COOLDOWN_SECONDS:
                                    continue
                                if count >= MAX_RETRIES:
                                    log(f"Notice: Element at ({cx}, {cy}) did not dismiss after {count} clicks; blacklisting for {BLACKLIST_DURATION_SECONDS}s.")
                                    blacklisted_locations[loc_key] = now + BLACKLIST_DURATION_SECONDS
                                    del active_clicked_buttons[loc_key]
                                    continue

                                log(f"Button '{name}' still present at ({cx}, {cy}) after {now - last_time:.1f}s (retry #{count + 1})...")

                            if not any(abs(cx - (px + pw // 2)) < 30 and abs(cy - (py + ph // 2)) < 25 for _, px, py, pw, ph, _, _ in pending_clicks):
                                pending_clicks.append((name, x, y, w, h, score, loc_key))

                # Dispatch clicks to all detected buttons across split windows
                if pending_clicks:
                    log(f"Found {len(pending_clicks)} pending button(s) to click across window(s).")
                    dispatched = dispatch_batch_clicks(pending_clicks)

                    # Only update state and increment retries if click was actually dispatched
                    if dispatched:
                        for (_, _, _, _, _, _, loc_key) in pending_clicks:
                            prev_count = active_clicked_buttons.get(loc_key, {}).get("count", 0)
                            active_clicked_buttons[loc_key] = {
                                "first_seen": active_clicked_buttons.get(loc_key, {}).get("first_seen", now),
                                "last_clicked": now,
                                "count": prev_count + 1
                            }

                stale_locations = []
                for loc_key, state in active_clicked_buttons.items():
                    if loc_key not in current_frame_locations:
                        if now - state["last_clicked"] > 15.0:
                            stale_locations.append(loc_key)
                for loc_key in stale_locations:
                    del active_clicked_buttons[loc_key]

            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(2)
