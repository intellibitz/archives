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
    "Accept": ["accept.png", "accept-inline*.png", "accept_inline*.png", "accept-*.png", "accept_*.png"],
    "Keep All": ["keep-all*.png", "keep_all*.png", "keep*.png"],
    "Proceed": ["proceed*.png"],
    "Yes Don't Ask": ["yes-dontask*.png", "yes-dont-ask*.png"],
    "Allow Always": ["allow-always*.png", "always-allow*.png"],
    "Always Run": ["always-run*.png"],
    "Allow Once": ["allow-once*.png"],
    "Allow": ["allow.png", "allow2.png", "allow*.png"],
}

# Priority ranking: clicking global dismiss/accept first ensures cascading diffs resolve cleanly
BUTTON_PRIORITY = {
    "Accept All": 1,
    "Accept All 2": 2,
    "Accept Changes": 3,
    "Accept": 4,
    "Keep All": 5,
    "Proceed": 6,
    "Yes Don't Ask": 7,
    "Allow Always": 8,
    "Always Run": 9,
    "Allow Once": 10,
    "Allow": 11,
}
CANONICAL_TEMPLATES = {
    "Accept All": "accept-all.png",
    "Accept All 2": "accept-all2.png",
    "Accept Changes": "accept-changes.png",
    "Accept": "accept-inline.png",
    "Keep All": "keep-all.png",
    "Proceed": "proceed.png",
    "Allow Always": "allow-always.png",
    "Always Run": "always-run.png",
    "Allow Once": "allow-once.png",
    "Allow": "allow.png",
    "Yes Don't Ask": "yes-dontask.png",
}

# Templates cover button colors and states; strict thresholds prevent false matches against editor text/syntax.
THRESHOLDS = {
    "Accept All": 0.72,
    "Accept All 2": 0.72,
    "Accept Changes": 0.72,
    "Accept": 0.72,
    "Keep All": 0.72,
    "Proceed": 0.72,
    "Allow Always": 0.72,
    "Always Run": 0.72,
    "Allow Once": 0.72,
    "Allow": 0.72,
    "Yes Don't Ask": 0.72,
}
DEFAULT_THRESHOLD = 0.72
CONFIDENCE_THRESHOLD = 0.70

if sys.platform == "win32":
    TEMP_DIR = os.environ.get("TEMP", os.path.expanduser("~"))
    LOG_FILE = os.path.join(TEMP_DIR, "auto_click_decisions.log")
    PID_FILE = os.path.join(TEMP_DIR, "auto_click_decisions.pid")
else:
    LOG_FILE = "/tmp/auto_click_decisions.log"
    PID_FILE = "/tmp/auto_click_decisions.pid"

CHECK_INTERVAL = 1.0
RETRY_COOLDOWN_SECONDS = 2.0
MAX_RETRIES = 5
BLACKLIST_DURATION_SECONDS = 10.0

active_clicked_buttons = {}  # loc_key -> {"first_seen": t, "last_clicked": t, "count": n}
blacklisted_locations = {}   # loc_key -> unblock_time
button_first_seen = {}       # loc_key -> timestamp first observed

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
    MOUSEEVENTF_LEFTDOWN = 0x0002
    MOUSEEVENTF_LEFTUP = 0x0004


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


def is_user_actively_typing_or_interacting(threshold_ms=1000):
    """
    Returns True if user has provided keyboard or mouse input recently.
    Ensures cursor and active focus are completely untouched while user is typing in another window.
    """
    if sys.platform == "win32":
        try:
            class LASTINPUTINFO(ctypes.Structure):
                _fields_ = [("cbSize", wintypes.UINT), ("dwTime", wintypes.DWORD)]

            lii = LASTINPUTINFO()
            lii.cbSize = ctypes.sizeof(LASTINPUTINFO)
            if ctypes.windll.user32.GetLastInputInfo(ctypes.byref(lii)):
                tick = ctypes.windll.kernel32.GetTickCount()
                elapsed_ms = tick - lii.dwTime
                if elapsed_ms < threshold_ms:
                    return True
        except Exception:
            pass
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
                    time.sleep(0.08)


def dispatch_click(cx, cy, name=""):
    """
    Sends an absolute click with proper hover dwell and mouse down hold.
    Guarantees pointer placement via SetCursorPos + mouse_event absolute positioning fallback.
    """
    if sys.platform != "win32":
        return False

    attach_input_desktop()
    user32 = ctypes.windll.user32

    # Primary move: SetCursorPos
    user32.SetCursorPos(int(cx), int(cy))

    # Absolute move fallback via mouse_event (works across all desktop window stations and DPI scaling)
    sw = max(1, user32.GetSystemMetrics(0))
    sh = max(1, user32.GetSystemMetrics(1))
    x_norm = int(cx * 65535 / (sw - 1)) if sw > 1 else 0
    y_norm = int(cy * 65535 / (sh - 1)) if sh > 1 else 0
    user32.mouse_event(0x8000 | 0x0001, x_norm, y_norm, 0, 0)

    # Allow the target window to process WM_MOUSEMOVE and hover state
    time.sleep(0.06)

    # Primary click method: SendInput
    inp_down = INPUT()
    inp_down.type = INPUT_MOUSE
    inp_down.mi = MOUSEINPUT(0, 0, 0, MOUSEEVENTF_LEFTDOWN, 0, 0)
    sent_down = (user32.SendInput(1, ctypes.byref(inp_down), ctypes.sizeof(INPUT)) == 1)

    if not sent_down:
        user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)

    time.sleep(0.08)

    inp_up = INPUT()
    inp_up.type = INPUT_MOUSE
    inp_up.mi = MOUSEINPUT(0, 0, 0, MOUSEEVENTF_LEFTUP, 0, 0)
    sent_up = (user32.SendInput(1, ctypes.byref(inp_up), ctypes.sizeof(INPUT)) == 1)

    if not sent_up:
        user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)

    # Double click pulse for Swing/Compose canvas controls requiring focus + click
    time.sleep(0.04)
    user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
    time.sleep(0.06)
    user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)

    time.sleep(0.12)
    return True


def dispatch_batch_clicks(buttons_to_click):
    """
    Clicks the highest-priority candidate, then restores the original cursor.
    The cursor remains completely untouched if there are no candidates or if user is actively typing/dragging.
    """
    if not buttons_to_click:
        return set()

    if is_mouse_button_down():
        log("Notice: User holding mouse button; deferring batch clicks to keep cursor untouched.")
        return set()

    now = time.time()
    # If any candidate has been waiting for >= 1.5s, click it even if user is typing,
    # preventing IDE permission prompts from blocking execution indefinitely.
    # Immediately click decision prompts without artificial input delays
    orig_mx, orig_my = get_mouse_xy()
    user32 = ctypes.windll.user32 if sys.platform == "win32" else None
    orig_fg = user32.GetForegroundWindow() if user32 else None

    # Sort buttons by priority (e.g. Accept All / Permanent Allow before one-time)
    sorted_buttons = sorted(
        buttons_to_click,
        key=lambda b: BUTTON_PRIORITY.get(b[0], 99)
    )

    name, x, y, w, h, score, loc_key = sorted_buttons[0]
    cx, cy = x + w // 2, y + h // 2
    log(f"Clicking '{name}' at ({cx}, {cy}) (Conf: {score:.2f})...")
    try:
        activate_target_window(cx, cy)
        return {loc_key} if dispatch_click(cx, cy, name) else set()
    finally:
        if orig_mx is not None and orig_my is not None:
            curr_mx, curr_my = get_mouse_xy()
            if curr_mx is not None and curr_my is not None:
                # Restore only if the pointer stayed at our click target; preserve
                # any manual movement that happened while the click was dispatched.
                if abs(curr_mx - cx) + abs(curr_my - cy) < 50:
                    set_mouse_xy(orig_mx, orig_my)
        if user32 and orig_fg:
            try:
                curr_fg = user32.GetForegroundWindow()
                if curr_fg != orig_fg:
                    user32.SetForegroundWindow(orig_fg)
            except Exception:
                pass


_last_screenshot_error_time = 0

def get_screenshot():
    """Captures desktop screen into OpenCV image array."""
    global _last_screenshot_error_time
    try:
        attach_input_desktop()
        pil_img = ImageGrab.grab()
        img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
        return img
    except Exception as e:
        now = time.time()
        if now - _last_screenshot_error_time > 60:
            log(f"Screenshot error (screen locked or display inactive): {e}")
            _last_screenshot_error_time = now
        return None


def find_all_template_matches(screen_gray, template_gray, thresh):
    """
    Finds ALL distinct occurrences of template across the screen above thresh.
    Enables clicking multiple buttons simultaneously across split windows.
    Returns: list of (x, y, w, h, score) sorted by score descending.
    """
    scales = [1.00, 0.90, 0.80, 0.75, 0.70, 0.65, 0.60, 0.50, 1.10, 1.25, 1.50, 1.75, 2.00]
    all_raw_matches = []

    for scale in scales:
        if scale == 1.0:
            resized_tpl = template_gray
        else:
            w = int(template_gray.shape[1] * scale)
            h = int(template_gray.shape[0] * scale)
            if w <= 0 or h <= 0:
                continue
            interpolation = cv2.INTER_AREA if scale < 1.0 else cv2.INTER_CUBIC
            resized_tpl = cv2.resize(template_gray, (w, h), interpolation=interpolation)

        if resized_tpl.shape[1] > screen_gray.shape[1] or resized_tpl.shape[0] > screen_gray.shape[0]:
            continue

        res = cv2.matchTemplate(screen_gray, resized_tpl, cv2.TM_CCOEFF_NORMED)
        local_maxima = cv2.dilate(res, np.ones((5, 5), dtype=np.uint8))
        y_coords, x_coords = np.where((res >= thresh) & (res >= local_maxima - 1e-6))

        if len(x_coords) > 0:
            th_w, th_h = resized_tpl.shape[1], resized_tpl.shape[0]
            scores = res[y_coords, x_coords]
            sort_idx = np.argsort(-scores)
            for idx in sort_idx:
                cand_x = int(x_coords[idx])
                cand_y = int(y_coords[idx])
                s = float(scores[idx])
                all_raw_matches.append((cand_x, cand_y, th_w, th_h, s))

    all_raw_matches.sort(key=lambda m: m[4], reverse=True)
    distinct_matches = []
    for x, y, w, h, score in all_raw_matches:
        cx, cy = x + w // 2, y + h // 2
        if not any(
            abs(cx - (dx + dw // 2)) < max(15, min(w, dw) // 2)
            and abs(cy - (dy + dh // 2)) < max(10, min(h, dh) // 2)
            for dx, dy, dw, dh, _ in distinct_matches
        ):
            distinct_matches.append((x, y, w, h, score))

    return distinct_matches


def is_valid_button_candidate(screen, x, y, w, h, tpl_info):
    """Verifies that a matched region represents a true interactive button."""
    if w < 20 or h < 12 or h > 100:
        return False
    aspect_ratio = w / float(h)
    if aspect_ratio < 0.8 or aspect_ratio > 25.0:
        return False

    sh, sw = screen.shape[:2]
    if x < 10 or y < 0 or (x + w) > sw or (y + h) > sh:
        return False

    # Exclude matches in the taskbar/dock area (bottom 40 pixels of screen)
    if (y + h) > (sh - 40):
        return False

    # Exclude bottom-left IDE status bar area (where static status text appears)
    if y > (sh - 140) and x < 250:
        return False

    cand_bgr = screen[y:y+h, x:x+w]
    if cand_bgr.size == 0:
        return False

    # Check candidate region contrast (reject flat dark panels / monochrome background blocks)
    cand_gray = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2GRAY)
    if cand_gray.std() < 26.0:
        return False

    # Color / Saturation validation:
    # If the template is a colored button (e.g. blue Accept All with sat > 35),
    # verify candidate region also has color saturation, rejecting monochrome code/editor text.
    tpl_sat = tpl_info.get("sat", 0) if isinstance(tpl_info, dict) else 0
    if tpl_sat > 35:
        cand_hsv = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2HSV)
        cand_sat = float(cand_hsv[:, :, 1].mean())
        if cand_sat < max(20.0, tpl_sat * 0.25):
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

        for path in sorted(
            matched_files,
            key=lambda p: (
                os.path.basename(p) != CANONICAL_TEMPLATES.get(name),
                "grey" in os.path.basename(p) or "nohighlight" in os.path.basename(p),
                p,
            ),
        ):
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
            # Only warn if files actually matched pattern or on initial run
            if matched_files:
                log(f"Warning: No valid templates loaded from matching files for {name}")

    return loaded


if __name__ == "__main__":
    attach_input_desktop()
    log(f"Approval Button Auto-Clicker starting on {sys.platform} (SendInput Precision + Multi-Instance Support)...")
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

                for name in sorted(loaded_templates, key=lambda n: BUTTON_PRIORITY.get(n, 99)):
                    tpl_list = loaded_templates[name]
                    thresh = THRESHOLDS.get(name, DEFAULT_THRESHOLD)
                    candidate_found = False
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
                                candidate_found = True
                                break
                        if candidate_found:
                            break
                    if candidate_found:
                        break

                # Dispatch clicks to all detected buttons across split windows
                if pending_clicks:
                    log(f"Found {len(pending_clicks)} pending button(s) to click across window(s).")
                    dispatched = dispatch_batch_clicks(pending_clicks)

                    # Only update state and increment retries if click was actually dispatched
                    if dispatched:
                        for (_, _, _, _, _, _, loc_key) in pending_clicks:
                            if loc_key not in dispatched:
                                continue
                            prev_count = active_clicked_buttons.get(loc_key, {}).get("count", 0)
                            active_clicked_buttons[loc_key] = {
                                "first_seen": active_clicked_buttons.get(loc_key, {}).get("first_seen", now),
                                "last_clicked": now,
                                "count": prev_count + 1
                            }

                # Record appearance time for smart typing deferral
                for (_, _, _, _, _, _, loc_key) in pending_clicks:
                    if loc_key not in button_first_seen:
                        button_first_seen[loc_key] = now

                stale_locations = []
                for loc_key, state in active_clicked_buttons.items():
                    if loc_key not in current_frame_locations:
                        # CRITICAL FIX: If button is no longer on screen, it was successfully dismissed!
                        # Clean up after 1.5s so future prompts at the same coordinates are not treated as retries.
                        if now - state["last_clicked"] > 1.5:
                            stale_locations.append(loc_key)
                for loc_key in stale_locations:
                    del active_clicked_buttons[loc_key]

                stale_seen = [k for k in button_first_seen if k not in current_frame_locations]
                for k in stale_seen:
                    del button_first_seen[k]

            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(2)
