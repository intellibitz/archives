import glob
import os
import subprocess
import sys
import time
import cv2
import numpy as np

# Optional evdev support for native Wayland absolute input without cursor jumping
try:
    import evdev
    from evdev import UInput, ecodes as e
    HAS_EVDEV = True
except ImportError:
    HAS_EVDEV = False

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

# Matching threshold: 0.88 matches true interactive buttons while rejecting
# false-positive editor text, tabs, and explorer tree highlights.
THRESHOLDS = {
    "Accept All": 0.88,
    "Accept All 2": 0.88,
    "Accept Changes": 0.88,
    "Accept": 0.88,
    "Keep All": 0.88,
    "Proceed": 0.88,
    "Allow Always": 0.88,
    "Always Run": 0.88,
    "Allow Once": 0.88,
    "Allow": 0.88,
    "Yes Don't Ask": 0.88,
}
DEFAULT_THRESHOLD = 0.88
CONFIDENCE_THRESHOLD = 0.88

LOG_FILE = "/tmp/keep_all_auto.log"
PID_FILE = "/tmp/keep_all_auto.pid"

CHECK_INTERVAL = 1.0
RETRY_COOLDOWN_SECONDS = 2.0
MAX_RETRIES = 5
BLACKLIST_DURATION_SECONDS = 10.0

active_clicked_buttons = {}  # loc_key -> {"first_seen": t, "last_clicked": t, "count": n}
blacklisted_locations = {}   # loc_key -> unblock_time
uinput_device = None


def log(msg):
    t = time.strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{t}] {msg}\n"
    try:
        with open(LOG_FILE, "a", buffering=1) as f:
            f.write(line)
    except Exception:
        pass
    try:
        print(line.strip(), flush=True)
    except Exception:
        pass


def get_env():
    """Builds robust execution environment with dynamic display and auth socket resolution."""
    env = os.environ.copy()
    uid = os.getuid()
    run_dir = f"/run/user/{uid}"

    if "WAYLAND_DISPLAY" not in env or not env["WAYLAND_DISPLAY"]:
        wl_sockets = sorted(glob.glob(f"{run_dir}/wayland-*"))
        if wl_sockets:
            env["WAYLAND_DISPLAY"] = os.path.basename(wl_sockets[0])
            env["XDG_RUNTIME_DIR"] = run_dir

    if "DISPLAY" not in env or not env["DISPLAY"]:
        env["DISPLAY"] = ":0"

    if "XAUTHORITY" not in env or not os.path.exists(env.get("XAUTHORITY", "")):
        xauth_files = sorted(glob.glob(f"{run_dir}/xauth_*"))
        if xauth_files:
            env["XAUTHORITY"] = xauth_files[0]
        elif os.path.exists(os.path.expanduser("~/.Xauthority")):
            env["XAUTHORITY"] = os.path.expanduser("~/.Xauthority")

    ydotool_sock = f"{run_dir}/.ydotool_socket"
    if os.path.exists(ydotool_sock):
        env["YDOTOOL_SOCKET"] = ydotool_sock

    return env


def init_uinput(screen_width=1920, screen_height=1200):
    """Initializes hardware-level absolute pointer device for Wayland."""
    global uinput_device
    if not HAS_EVDEV:
        return None
    try:
        cap = {
            e.EV_KEY: [e.BTN_LEFT, e.BTN_RIGHT, e.BTN_TOUCH],
            e.EV_ABS: [
                (e.ABS_X, evdev.AbsInfo(value=0, min=0, max=screen_width, fuzz=0, flat=0, resolution=1)),
                (e.ABS_Y, evdev.AbsInfo(value=0, min=0, max=screen_height, fuzz=0, flat=0, resolution=1)),
            ]
        }
        ui = UInput(cap, name="AutoKeepAbsolutePointer")
        time.sleep(0.5)
        log(f"Initialized evdev UInput absolute pointing device ({screen_width}x{screen_height}).")
        return ui
    except Exception as ex:
        log(f"evdev UInput initialization notice: {ex}")
        return None


def dispatch_click(cx, cy):
    """Sends absolute click without jerking physical mouse cursor."""
    global uinput_device
    if uinput_device:
        try:
            # Sync position first so Wayland compositor updates mouse target before click down
            uinput_device.write(e.EV_ABS, e.ABS_X, int(cx))
            uinput_device.write(e.EV_ABS, e.ABS_Y, int(cy))
            uinput_device.syn()
            time.sleep(0.02)
            uinput_device.write(e.EV_KEY, e.BTN_LEFT, 1)
            uinput_device.syn()
            time.sleep(0.05)
            uinput_device.write(e.EV_KEY, e.BTN_LEFT, 0)
            uinput_device.syn()
            return True
        except Exception as ex:
            log(f"evdev click error: {ex}")

    env = get_env()

    # Fallback 1: ydotool
    try:
        res = subprocess.run(["ydotool", "mousemove", "-a", "--", str(cx), str(cy)], env=env, timeout=1)
        if res.returncode == 0:
            subprocess.run(["ydotool", "click", "0xC0"], env=env, timeout=1)
            return True
    except Exception:
        pass

    # Fallback 2: xdotool (reliable for XWayland / X11 applications like JetBrains IDEs)
    try:
        res = subprocess.run(["xdotool", "mousemove", str(cx), str(cy), "click", "1"], env=env, timeout=1, capture_output=True)
        if res.returncode == 0:
            return True
    except Exception:
        pass

    return False


def get_screenshot():
    """Captures screenshot using spectacle or fallback."""
    tmp = "/tmp/auto_keep_all_scr.png"
    env = get_env()
    try:
        subprocess.run(["spectacle", "-b", "-n", "-o", tmp], check=True, capture_output=True, env=env, timeout=3)
        img = cv2.imread(tmp)
        return img
    except Exception:
        try:
            subprocess.run(["import", "-window", "root", tmp], check=True, capture_output=True, env=env, timeout=3)
            img = cv2.imread(tmp)
            return img
        except Exception as e:
            log(f"Screenshot capture error: {e}")
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

    # Global Non-Maximum Suppression across all scales:
    all_raw_matches.sort(key=lambda m: m[4], reverse=True)
    distinct_matches = []
    for x, y, w, h, score in all_raw_matches:
        cx, cy = x + w // 2, y + h // 2
        if not any(abs(cx - (dx + dw // 2)) < 30 and abs(cy - (dy + dh // 2)) < 20 for dx, dy, dw, dh, _ in distinct_matches):
            distinct_matches.append((x, y, w, h, score))

    return distinct_matches


def find_best_template_match(screen_gray, tpl_item):
    """Finds best match score and bounding box for a template against screen_gray."""
    if isinstance(tpl_item, dict):
        template_gray = tpl_item["gray"]
    else:
        template_gray = tpl_item
    matches = find_all_template_matches(screen_gray, template_gray, thresh=0.1)
    if matches:
        x, y, w, h, score = matches[0]
        return score, x, y, w, h
    return 0.0, 0, 0, 0, 0


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
        if cand_sat < 25:
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
    log("Auto Keep All Clicker starting on Linux (Precision Absolute Input + Multi-Instance)...")
    log(f"Assets Directory: {ASSETS_DIR}")
    log(f"Log File: {LOG_FILE}")
    log(f"PID File: {PID_FILE}")

    try:
        with open(PID_FILE, "w") as pf:
            pf.write(str(os.getpid()))
    except Exception as e:
        log(f"Notice: Could not write PID file: {e}")

    # Capture first screenshot to determine screen resolution for evdev
    initial_screen = get_screenshot()
    if initial_screen is not None:
        sh, sw = initial_screen.shape[:2]
        uinput_device = init_uinput(sw, sh)
    else:
        uinput_device = init_uinput(1920, 1200)

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

            # Clean expired blacklisted locations
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
                    for (name, x, y, w, h, score, loc_key) in pending_clicks:
                        cx, cy = x + w // 2, y + h // 2
                        log(f"Dispatching precision click for '{name}' at ({cx}, {cy}) (Conf: {score:.2f})...")
                        dispatch_click(cx, cy)
                        prev_count = active_clicked_buttons.get(loc_key, {}).get("count", 0)
                        active_clicked_buttons[loc_key] = {
                            "first_seen": active_clicked_buttons.get(loc_key, {}).get("first_seen", now),
                            "last_clicked": now,
                            "count": prev_count + 1
                        }
                        time.sleep(0.08)

                # State Cleanup: Only expire locations that have been absent for over 15 seconds
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
