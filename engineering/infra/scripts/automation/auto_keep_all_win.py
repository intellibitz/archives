import ctypes
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

# --- Configuration ---
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../assets"))

TEMPLATES = {
    "Keep All": [os.path.join(ASSETS_DIR, "keep-all.png")],
    "Proceed": [os.path.join(ASSETS_DIR, "proceed.png")],
    "Allow Always": [os.path.join(ASSETS_DIR, "allow-always.png")],
    "Accept All": [
        os.path.join(ASSETS_DIR, "accept-all.png"),
        os.path.join(ASSETS_DIR, "accept-all-nohighlight.png"),
        os.path.join(ASSETS_DIR, "accept-all-grey-50.png"),
        os.path.join(ASSETS_DIR, "accept-all-grey-65.png"),
        os.path.join(ASSETS_DIR, "accept-all-grey.png"),
        os.path.join(ASSETS_DIR, "accept-all-grey-hover.png"),
        os.path.join(ASSETS_DIR, "accept-all-grey-dark.png"),
    ],
    "Accept All 2": [
        os.path.join(ASSETS_DIR, "accept-all2.png"),
        os.path.join(ASSETS_DIR, "accept_all2.png"),
        os.path.join(ASSETS_DIR, "accept-all2-nohighlight.png"),
        os.path.join(ASSETS_DIR, "accept_all2-nohighlight.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey-35.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey-48.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey-62.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey.png"),
        os.path.join(ASSETS_DIR, "accept_all2-grey.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey-hover.png"),
        os.path.join(ASSETS_DIR, "accept-all2-grey-dark.png"),
    ],
    "Accept Changes": [
        os.path.join(ASSETS_DIR, "accept-changes.png"),
        os.path.join(ASSETS_DIR, "accept_changes.png"),
        os.path.join(ASSETS_DIR, "acceot-changes.png"),
        os.path.join(ASSETS_DIR, "accept-changes-nohighlight.png"),
        os.path.join(ASSETS_DIR, "accept_changes-nohighlight.png"),
        os.path.join(ASSETS_DIR, "accept-changes-grey-35.png"),
        os.path.join(ASSETS_DIR, "accept-changes-grey-50.png"),
        os.path.join(ASSETS_DIR, "accept-changes-grey.png"),
        os.path.join(ASSETS_DIR, "accept_changes-grey.png"),
        os.path.join(ASSETS_DIR, "accept-changes-grey-hover.png"),
        os.path.join(ASSETS_DIR, "accept-changes-grey-dark.png"),
    ],
    "Always Run": [os.path.join(ASSETS_DIR, "always-run.png")],
    "Allow Once": [os.path.join(ASSETS_DIR, "allow-once.png")],
    "Allow": [os.path.join(ASSETS_DIR, "allow.png"), os.path.join(ASSETS_DIR, "allow2.png")],
    "Yes Don't Ask": [os.path.join(ASSETS_DIR, "yes-dontask.png")],
}

THRESHOLDS = {
    "Keep All": 0.80,
    "Proceed": 0.80,
    "Accept All": 0.82,
    "Accept All 2": 0.82,
    "Accept Changes": 0.82,
    "Always Run": 0.85,
    "Allow Always": 0.85,
    "Allow Once": 0.88,
    "Allow": 0.88,
    "Yes Don't Ask": 0.85,
}

if sys.platform == "win32":
    TEMP_DIR = os.environ.get("TEMP", os.path.expanduser("~"))
    LOG_FILE = os.path.join(TEMP_DIR, "keep_all_auto.log")
else:
    LOG_FILE = "/tmp/keep_all_auto.log"

CHECK_INTERVAL = 1.0
CLICK_COOLDOWN_SECONDS = 10.0
recent_clicks = {}


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


def get_mouse_xy():
    """Returns (x, y) tuple of current mouse cursor position."""
    if sys.platform == "win32":
        class POINT(ctypes.Structure):
            _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]

        pt = POINT()
        try:
            ctypes.windll.user32.GetCursorPos(ctypes.byref(pt))
            return pt.x, pt.y
        except Exception:
            return None, None
    else:
        import subprocess
        try:
            res = subprocess.run(["xdotool", "getmouselocation", "--shell"], capture_output=True, text=True)
            x, y = None, None
            for line in res.stdout.splitlines():
                if line.startswith("X="):
                    x = int(line.split("=")[1])
                elif line.startswith("Y="):
                    y = int(line.split("=")[1])
            return x, y
        except Exception:
            return None, None


def set_mouse_xy(x, y):
    """Position mouse cursor at (x, y)."""
    if sys.platform == "win32":
        ctypes.windll.user32.SetCursorPos(int(x), int(y))
    else:
        import subprocess
        subprocess.run(["xdotool", "mousemove", str(x), str(y)], check=True)


def click_mouse():
    """Trigger left mouse click."""
    if sys.platform == "win32":
        ctypes.windll.user32.mouse_event(0x0002, 0, 0, 0, 0)
        time.sleep(0.03)
        ctypes.windll.user32.mouse_event(0x0004, 0, 0, 0, 0)
    else:
        import subprocess
        subprocess.run(["xdotool", "click", "1"], check=True)


def is_user_actively_moving_mouse():
    """Returns True if the user moved mouse cursor > 10 pixels in 80ms."""
    p1 = get_mouse_xy()
    time.sleep(0.08)
    p2 = get_mouse_xy()
    if p1[0] is None or p2[0] is None:
        return False
    delta = abs(p1[0] - p2[0]) + abs(p1[1] - p2[1])
    return delta > 10


def trigger_click(name, x, y, w, h):
    cx, cy = x + w // 2, y + h // 2
    now = time.time()

    # Cool-down check: Don't repeat click on same target location within 10s
    loc_key = (cx // 20, cy // 20)
    if loc_key in recent_clicks and (now - recent_clicks[loc_key]) < CLICK_COOLDOWN_SECONDS:
        return False

    orig_mx, orig_my = get_mouse_xy()
    cursor_already_on_button = (
        orig_mx is not None
        and orig_my is not None
        and (x - 5) <= orig_mx <= (x + w + 5)
        and (y - 5) <= orig_my <= (y + h + 5)
    )

    if not cursor_already_on_button and is_user_actively_moving_mouse():
        log(f"Notice: User actively moving mouse elsewhere; skipping click for '{name}'.")
        return False

    log(f"Clicking '{name}' at ({cx}, {cy}) (cursor_already_on_button={cursor_already_on_button})...")

    try:
        recent_clicks[loc_key] = now

        # Move to button and click
        set_mouse_xy(cx, cy)
        time.sleep(0.04)
        click_mouse()
        time.sleep(0.04)

        # Zero-Jump Restore: Return cursor back to user's working location
        if orig_mx is not None and orig_my is not None and not cursor_already_on_button:
            set_mouse_xy(orig_mx, orig_my)

        log(f"Automation for '{name}' successful (Cursor restored to {orig_mx}, {orig_my}).")
        return True
    except Exception as e:
        log(f"Input error for '{name}': {e}")
        return False


def get_screenshot():
    """Captures desktop screen into OpenCV image array."""
    try:
        pil_img = ImageGrab.grab()
        img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
        return img
    except Exception as e:
        log(f"Screenshot error: {e}")
        return None


def find_best_template_match(screen_gray, template_gray):
    """Multi-scale template matching across 0.80x to 1.25x."""
    best_val = -1.0
    best_loc = None
    best_shape = None

    scales = [0.80, 0.85, 0.90, 0.95, 1.00, 1.05, 1.10, 1.15, 1.20, 1.25]
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
        _, max_val, _, max_loc = cv2.minMaxLoc(res)
        if np.isnan(max_val) or np.isinf(max_val):
            continue
        if max_val > best_val:
            best_val = max_val
            best_loc = max_loc
            best_shape = resized_tpl.shape

    return best_val, best_loc, best_shape


def load_templates():
    """Loads all button image template variants."""
    loaded = {}
    for name, paths in TEMPLATES.items():
        if isinstance(paths, str):
            paths = [paths]
        tpl_list = []
        seen_hashes = set()
        for path in paths:
            if os.path.exists(path):
                tpl = cv2.imread(path)
                if tpl is not None:
                    tpl_gray = cv2.cvtColor(tpl, cv2.COLOR_BGR2GRAY)
                    if tpl_gray.std() < 10.0:
                        continue
                    h = tpl_gray.tobytes()
                    if h not in seen_hashes:
                        seen_hashes.add(h)
                        tpl_list.append(tpl_gray)
                else:
                    log(f"Warning: Could not read template file: {path}")
        if tpl_list:
            loaded[name] = tpl_list
        else:
            log(f"Warning: No valid templates found for {name}")
    return loaded


if __name__ == "__main__":
    log(f"Auto Keep All Clicker starting on {sys.platform} (DPI Aware + Cooldown Guard)...")
    log(f"Assets Directory: {ASSETS_DIR}")
    log(f"Log File: {LOG_FILE}")

    loaded_templates = load_templates()

    for name, tpl_list in loaded_templates.items():
        thresh = THRESHOLDS.get(name, 0.80)
        log(f"Loaded template: {name} ({len(tpl_list)} variants, threshold={thresh})")

    if not loaded_templates:
        log("No templates found in assets directory. Exiting.")
        sys.exit(1)

    last_reload = time.time()

    while True:
        try:
            if time.time() - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = time.time()

            screen = get_screenshot()
            if screen is not None:
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)

                matched = False
                for name, tpl_list in loaded_templates.items():
                    thresh = THRESHOLDS.get(name, 0.80)
                    for template_gray in tpl_list:
                        max_val, max_loc, best_shape = find_best_template_match(screen_gray, template_gray)

                        if max_val >= thresh:
                            log(f"Button '{name}' detected (Conf: {max_val:.2f} >= Threshold: {thresh})")
                            clicked = trigger_click(name, max_loc[0], max_loc[1], best_shape[1], best_shape[0])
                            if clicked:
                                time.sleep(4)
                            matched = True
                            break
                    if matched:
                        break
            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(3)
