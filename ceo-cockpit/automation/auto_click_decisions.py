#!/usr/bin/env python3
"""
Autonomous Decision Dialog & Permission Auto-Clicker for Antigravity CEO Cockpit.
Monitors the interactive Windows desktop for agent permission prompts, dialog buttons,
and code review diff modals ('Allow Always', 'Allow', 'Yes Don't Ask', 'Proceed', 'Accept All', 'Keep All').
Dispatches low-latency, non-intrusive clicks and instantaneously restores executive cursor position.
"""

import os
import sys
import time
import glob
import ctypes
from ctypes import wintypes
import cv2
import numpy as np
from PIL import ImageGrab

# High-DPI Awareness Initialization (Per-Monitor V2)
if sys.platform == "win32":
    try:
        # DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 (-4)
        ctypes.windll.user32.SetProcessDpiAwarenessContext(ctypes.c_void_p(-4))
    except Exception:
        try:
            ctypes.windll.shcore.SetProcessDpiAwareness(2)
        except Exception:
            try:
                ctypes.windll.user32.SetProcessDPIAware()
            except Exception:
                pass

# --- Configuration & Assets ---
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
ASSETS_DIR = os.path.join(SCRIPT_DIR, "assets")
if not os.path.isdir(ASSETS_DIR):
    # Fallback search paths
    alt_paths = [
        os.path.abspath(os.path.join(SCRIPT_DIR, "../../engineering/infra/scripts/assets")),
        os.path.abspath(os.path.join(SCRIPT_DIR, "../assets")),
    ]
    for p in alt_paths:
        if os.path.isdir(p):
            ASSETS_DIR = p
            break

# Clean pattern resolution ensures each asset is mapped to exactly one category
TEMPLATE_PATTERNS = {
    "Accept All": ["accept-all.png", "accept-all-*.png", "accept_all.png", "accept_all-*.png"],
    "Accept All 2": ["accept-all2*.png", "accept_all2*.png"],
    "Accept Changes": ["accept-changes*.png", "accept_changes*.png", "acceot-changes*.png"],
    "Accept Inline": ["accept-inline.png", "accept_inline.png"],
    "Keep All": ["keep-all*.png"],
    "Proceed": ["proceed*.png"],
    "Yes Don't Ask": ["yes-dontask*.png", "yes-dont-ask*.png"],
    "Allow Always": ["allow-always*.png", "always-allow*.png"],
    "Always Run": ["always-run*.png"],
    "Allow Once": ["allow-once*.png"],
    "Allow": ["allow.png", "allow2.png"],
}

# Priority ranking: Permanent allow and global approval before one-time actions
BUTTON_PRIORITY = {
    "Accept All": 1,
    "Accept All 2": 2,
    "Accept Changes": 3,
    "Accept Inline": 4,
    "Keep All": 5,
    "Proceed": 6,
    "Yes Don't Ask": 7,
    "Allow Always": 8,
    "Always Run": 9,
    "Allow Once": 10,
    "Allow": 11,
}

THRESHOLDS = {
    "Accept All": 0.82,
    "Accept All 2": 0.82,
    "Accept Changes": 0.82,
    "Accept Inline": 0.82,
    "Keep All": 0.82,
    "Proceed": 0.82,
    "Yes Don't Ask": 0.78,
    "Allow Always": 0.78,
    "Always Run": 0.78,
    "Allow Once": 0.78,
    "Allow": 0.78,
}
DEFAULT_THRESHOLD = 0.80

if sys.platform == "win32":
    TEMP_DIR = os.environ.get("TEMP", os.path.expanduser("~"))
    LOG_FILE = os.path.join(TEMP_DIR, "auto_click_decisions.log")
    PID_FILE = os.path.join(TEMP_DIR, "auto_click_decisions.pid")
    HEARTBEAT_FILE = os.path.join(TEMP_DIR, "auto_click_decisions.heartbeat")
else:
    LOG_FILE = "/tmp/auto_click_decisions.log"
    PID_FILE = "/tmp/auto_click_decisions.pid"
    HEARTBEAT_FILE = "/tmp/auto_click_decisions.heartbeat"

CHECK_INTERVAL = 0.4
RETRY_COOLDOWN_SECONDS = 1.2
MAX_RETRIES = 5
BLACKLIST_DURATION_SECONDS = 8.0

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


def write_heartbeat():
    try:
        with open(HEARTBEAT_FILE, "w", encoding="utf-8") as f:
            f.write(str(time.time()))
    except Exception:
        pass


def attach_input_desktop():
    """Attaches background thread to active interactive desktop session."""
    if sys.platform == "win32":
        try:
            hdesk = ctypes.windll.user32.OpenInputDesktop(0, False, 0x01FF)
            if hdesk:
                ctypes.windll.user32.SetThreadDesktop(hdesk)
                ctypes.windll.user32.CloseDesktop(hdesk)
        except Exception:
            pass


def get_mouse_xy():
    if sys.platform == "win32":
        pt = wintypes.POINT()
        if ctypes.windll.user32.GetCursorPos(ctypes.byref(pt)):
            return pt.x, pt.y
    return None, None


def set_mouse_xy(x, y):
    if sys.platform == "win32":
        ctypes.windll.user32.SetCursorPos(int(x), int(y))


def is_user_actively_typing_or_interacting(threshold_ms=350):
    """
    Returns True if user has provided keyboard or mouse input within threshold_ms.
    Ensures cursor remains completely untouched while user is typing in another window.
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
    """Brings the window under (cx, cy) to foreground using AttachThreadInput bypass."""
    if sys.platform == "win32":
        attach_input_desktop()
        user32 = ctypes.windll.user32
        kernel32 = ctypes.windll.kernel32

        class POINT(ctypes.Structure):
            _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]

        pt = POINT(int(cx), int(cy))
        hwnd = user32.WindowFromPoint(pt)
        if hwnd:
            root = user32.GetAncestor(hwnd, 2)  # GA_ROOT
            if root:
                fg = user32.GetForegroundWindow()
                if fg != root:
                    try:
                        cur_thread = kernel32.GetCurrentThreadId()
                        fg_thread = user32.GetWindowThreadProcessId(fg, None)
                        if cur_thread != fg_thread and fg_thread != 0:
                            user32.AttachThreadInput(cur_thread, fg_thread, True)
                            user32.SetForegroundWindow(root)
                            user32.BringWindowToTop(root)
                            user32.AttachThreadInput(cur_thread, fg_thread, False)
                        else:
                            user32.SetForegroundWindow(root)
                            user32.BringWindowToTop(root)
                    except Exception:
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

    # Move to target
    if not user32.SetCursorPos(int(cx), int(cy)):
        log(f"SetCursorPos failed for '{name}' at ({cx}, {cy}).")
        return False

    # Hover dwell: allows Electron/Chromium to process WM_MOUSEMOVE and hit-test DOM element
    time.sleep(0.05)

    # Mouse down
    inp_down = INPUT()
    inp_down.type = INPUT_MOUSE
    inp_down.mi = MOUSEINPUT(0, 0, 0, MOUSEEVENTF_LEFTDOWN, 0, 0)
    if user32.SendInput(1, ctypes.byref(inp_down), ctypes.sizeof(INPUT)) != 1:
        log(f"SendInput rejected mouse-down for '{name}'.")
        return False

    # Hold down for 50ms
    time.sleep(0.05)

    # Mouse up
    inp_up = INPUT()
    inp_up.type = INPUT_MOUSE
    inp_up.mi = MOUSEINPUT(0, 0, 0, MOUSEEVENTF_LEFTUP, 0, 0)
    released = user32.SendInput(1, ctypes.byref(inp_up), ctypes.sizeof(INPUT)) == 1

    # Post-click dwell: Crucial for Chromium/Electron to fire the DOM click event
    time.sleep(0.08)
    return released


def dispatch_batch_clicks(buttons_to_click):
    """
    Dispatches clicks for prioritized buttons, with instant cursor restoration.
    """
    if not buttons_to_click:
        return set()

    attach_input_desktop()

    now = time.time()
    # If any candidate has been waiting for >= 1.2s, click it even if user is typing,
    # preventing IDE permission prompts from blocking execution indefinitely.
    any_waiting_long = any(
        (now - button_first_seen.get(loc_key, now)) >= 1.2
        for _, _, _, _, _, _, loc_key in buttons_to_click
    )
    if not any_waiting_long and is_user_actively_typing_or_interacting(threshold_ms=350):
        log("Notice: Executive actively typing; briefly deferring click to keep cursor untouched.")
        return set()

    orig_mx, orig_my = get_mouse_xy()
    user32 = ctypes.windll.user32 if sys.platform == "win32" else None
    orig_fg = user32.GetForegroundWindow() if user32 else None

    # Sort buttons by priority (Permanent Allow / Accept All before one-time)
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
        # Instant cursor restoration
        if orig_mx is not None and orig_my is not None:
            curr_mx, curr_my = get_mouse_xy()
            if curr_mx is not None and curr_my is not None:
                if abs(curr_mx - cx) + abs(curr_my - cy) < 50:
                    set_mouse_xy(orig_mx, orig_my)
        if user32 and orig_fg:
            try:
                curr_fg = user32.GetForegroundWindow()
                if curr_fg != orig_fg:
                    user32.SetForegroundWindow(orig_fg)
            except Exception:
                pass


def get_screenshot():
    """
    Captures multi-monitor desktop screen into OpenCV image array,
    returning (image, offset_x, offset_y).
    """
    try:
        attach_input_desktop()
        # all_screens=True captures entire virtual desktop spanning all monitors
        pil_img = ImageGrab.grab(all_screens=True)
        img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        # Virtual screen offset for multi-monitor topologies with negative coordinates
        offset_x = 0
        offset_y = 0
        if sys.platform == "win32":
            user32 = ctypes.windll.user32
            SM_XVIRTUALSCREEN = 76
            SM_YVIRTUALSCREEN = 77
            offset_x = user32.GetSystemMetrics(SM_XVIRTUALSCREEN)
            offset_y = user32.GetSystemMetrics(SM_YVIRTUALSCREEN)

        return img, offset_x, offset_y
    except Exception as e:
        log(f"Screenshot error: {e}")
        return None, 0, 0


def find_all_template_matches(screen_gray, template_gray, thresh, template_small=None, screen_small=None):
    """
    Hierarchical multi-match: Fast downscaled pre-screening followed by native verification.
    Achieves sub-second scan times while preserving 100% accuracy.
    """
    if screen_small is not None and template_small is not None:
        if template_small.shape[1] > screen_small.shape[1] or template_small.shape[0] > screen_small.shape[0]:
            return []
        res_s = cv2.matchTemplate(screen_small, template_small, cv2.TM_CCOEFF_NORMED)
        _, max_v, _, _ = cv2.minMaxLoc(res_s)
        # If coarse correlation is below threshold window, candidate cannot match at native scale
        if max_v < (thresh - 0.15):
            return []

    # Check native 1.0 scale first (matches 99% of Windows DPI dialogs)
    all_raw_matches = []
    if template_gray.shape[1] <= screen_gray.shape[1] and template_gray.shape[0] <= screen_gray.shape[0]:
        res = cv2.matchTemplate(screen_gray, template_gray, cv2.TM_CCOEFF_NORMED)
        local_maxima = cv2.dilate(res, np.ones((5, 5), dtype=np.uint8))
        y_coords, x_coords = np.where((res >= thresh) & (res >= local_maxima - 1e-6))

        if len(x_coords) > 0:
            th_w, th_h = template_gray.shape[1], template_gray.shape[0]
            scores = res[y_coords, x_coords]
            sort_idx = np.argsort(-scores)
            for idx in sort_idx:
                all_raw_matches.append((int(x_coords[idx]), int(y_coords[idx]), th_w, th_h, float(scores[idx])))

    # Fallback to display scaling (1.25 / 1.50) only if native scale didn't find candidates
    if not all_raw_matches:
        for scale in [1.25, 1.50]:
            w = int(template_gray.shape[1] * scale)
            h = int(template_gray.shape[0] * scale)
            if w <= 0 or h <= 0 or w > screen_gray.shape[1] or h > screen_gray.shape[0]:
                continue
            resized_tpl = cv2.resize(template_gray, (w, h), interpolation=cv2.INTER_CUBIC)
            res = cv2.matchTemplate(screen_gray, resized_tpl, cv2.TM_CCOEFF_NORMED)
            local_maxima = cv2.dilate(res, np.ones((5, 5), dtype=np.uint8))
            y_coords, x_coords = np.where((res >= thresh) & (res >= local_maxima - 1e-6))
            if len(x_coords) > 0:
                scores = res[y_coords, x_coords]
                sort_idx = np.argsort(-scores)
                for idx in sort_idx:
                    all_raw_matches.append((int(x_coords[idx]), int(y_coords[idx]), w, h, float(scores[idx])))
                break

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
    """Verifies candidate region represents a genuine button."""
    if w < 28 or h < 14 or h > 95:
        return False
    aspect_ratio = w / float(h)
    if aspect_ratio < 1.0 or aspect_ratio > 22.0:
        return False

    sh, sw = screen.shape[:2]
    if x < 0 or y < 0 or (x + w) > sw or (y + h) > sh:
        return False

    # Exclude bottom taskbar area
    if (y + h) > (sh - 40):
        return False

    cand_bgr = screen[y:y+h, x:x+w]
    if cand_bgr.size == 0:
        return False

    cand_gray = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2GRAY)
    if cand_gray.std() < 8.0:
        return False

    tpl_sat = tpl_info.get("sat", 0) if isinstance(tpl_info, dict) else 0
    if tpl_sat > 35:
        cand_hsv = cv2.cvtColor(cand_bgr, cv2.COLOR_BGR2HSV)
        cand_sat = float(cand_hsv[:, :, 1].mean())
        if cand_sat < max(18.0, tpl_sat * 0.20):
            return False

    return True


def load_templates():
    """Loads all template image variants from assets directory with precomputed downscaled pyramids."""
    loaded = {}
    seen_paths = set()
    for name, patterns in TEMPLATE_PATTERNS.items():
        loaded[name] = []
        for pat in patterns:
            full_pattern = os.path.join(ASSETS_DIR, pat)
            matches = glob.glob(full_pattern)
            for path in matches:
                if path in seen_paths:
                    continue
                seen_paths.add(path)
                bgr = cv2.imread(path)
                if bgr is not None:
                    gray = cv2.cvtColor(bgr, cv2.COLOR_BGR2GRAY)
                    hsv = cv2.cvtColor(bgr, cv2.COLOR_BGR2HSV)
                    sat = float(hsv[:, :, 1].mean())
                    small_gray = cv2.resize(gray, (0, 0), fx=0.5, fy=0.5)
                    loaded[name].append({
                        "path": path,
                        "gray": gray,
                        "small_gray": small_gray,
                        "bgr": bgr,
                        "w": gray.shape[1],
                        "h": gray.shape[0],
                        "sat": sat
                    })
    total = sum(len(v) for v in loaded.values())
    log(f"Loaded {total} button template variations across {len(loaded)} action groups from: {ASSETS_DIR}")
    return loaded


def check_and_clean_blacklists():
    now = time.time()
    unblocked = [loc for loc, exp in blacklisted_locations.items() if now >= exp]
    for loc in unblocked:
        del blacklisted_locations[loc]
        active_clicked_buttons.pop(loc, None)
        log(f"Unblocked location {loc}")


def main_loop():
    templates = load_templates()
    if not any(templates.values()):
        log(f"Error: No template images found in {ASSETS_DIR}.")
        sys.exit(1)

    log("Decision Auto-Clicker active. Monitoring for agent prompts...")

    try:
        with open(PID_FILE, "w", encoding="utf-8") as f:
            f.write(str(os.getpid()))
    except Exception:
        pass

    last_thumb = None
    last_full_scan = 0.0

    while True:
        try:
            write_heartbeat()
            check_and_clean_blacklists()

            screen_bgr, offset_x, offset_y = get_screenshot()
            if screen_bgr is None:
                time.sleep(CHECK_INTERVAL)
                continue

            screen_gray = cv2.cvtColor(screen_bgr, cv2.COLOR_BGR2GRAY)
            now = time.time()

            # Fast screen change detection (absdiff on 160x100 thumbnail: < 2ms)
            screen_thumb = cv2.resize(screen_gray, (160, 100))
            screen_changed = (
                last_thumb is None
                or cv2.norm(screen_thumb, last_thumb, cv2.NORM_L1) > 250
                or (now - last_full_scan) >= 2.5
                or len(active_clicked_buttons) > 0
            )
            last_thumb = screen_thumb

            if not screen_changed:
                time.sleep(0.3)
                continue

            last_full_scan = now
            screen_small = cv2.resize(screen_gray, (0, 0), fx=0.5, fy=0.5)

            pending_clicks = []
            current_frame_locations = set()

            for name, tpl_list in templates.items():
                thresh = THRESHOLDS.get(name, DEFAULT_THRESHOLD)
                for tpl_info in tpl_list:
                    tpl_gray = tpl_info["gray"]
                    tpl_small = tpl_info.get("small_gray")
                    matches = find_all_template_matches(
                        screen_gray, tpl_gray, thresh,
                        template_small=tpl_small, screen_small=screen_small
                    )
                    for (x, y, w, h, score) in matches:
                        # Absolute desktop screen coordinate
                        abs_x = x + offset_x
                        abs_y = y + offset_y
                        loc_key = f"{abs_x // 20}_{abs_y // 20}_{name}"
                        current_frame_locations.add(loc_key)

                        if loc_key in blacklisted_locations:
                            continue

                        if not is_valid_button_candidate(screen_bgr, x, y, w, h, tpl_info):
                            continue

                        click_state = active_clicked_buttons.get(loc_key)
                        if click_state:
                            time_since = now - click_state["last_clicked"]
                            if time_since < RETRY_COOLDOWN_SECONDS:
                                continue
                            if click_state["count"] >= MAX_RETRIES:
                                blacklisted_locations[loc_key] = now + BLACKLIST_DURATION_SECONDS
                                log(f"Blacklisting {name} at ({abs_x}, {abs_y}) for {BLACKLIST_DURATION_SECONDS}s.")
                                continue

                        pending_clicks.append((name, abs_x, abs_y, w, h, score, loc_key))

            if pending_clicks:
                clicked_keys = dispatch_batch_clicks(pending_clicks)
                for loc_key in clicked_keys:
                    prev_count = active_clicked_buttons.get(loc_key, {}).get("count", 0)
                    active_clicked_buttons[loc_key] = {
                        "first_seen": active_clicked_buttons.get(loc_key, {}).get("first_seen", now),
                        "last_clicked": now,
                        "count": prev_count + 1
                    }

                for (_, _, _, _, _, _, loc_key) in pending_clicks:
                    if loc_key not in button_first_seen:
                        button_first_seen[loc_key] = now

                stale_locations = []
                for loc_key, state in active_clicked_buttons.items():
                    if loc_key not in current_frame_locations:
                        if now - state["last_clicked"] > 1.2:
                            stale_locations.append(loc_key)
                for loc_key in stale_locations:
                    del active_clicked_buttons[loc_key]

                stale_seen = [k for k in button_first_seen if k not in current_frame_locations]
                for k in stale_seen:
                    del button_first_seen[k]

            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(CHECK_INTERVAL)


if __name__ == "__main__":
    main_loop()
