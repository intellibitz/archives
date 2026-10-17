import os
import glob
import time
import subprocess
import cv2
import numpy as np

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
    "Yes Don't Ask": [os.path.join(ASSETS_DIR, "yes-dontask.png")]
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

LOG_FILE = "/tmp/keep_all_auto.log"
CHECK_INTERVAL = 1.0
active_clicked_buttons = set()

def log(msg):
    t = time.strftime("%H:%M:%S")
    line = f"[{t}] {msg}\n"
    try:
        with open(LOG_FILE, "a", buffering=1) as f:
            f.write(line)
    except:
        pass
    try:
        print(line.strip(), flush=True)
    except:
        pass

def get_env():
    """Builds execution environment with valid DISPLAY and XAUTHORITY credentials."""
    env = os.environ.copy()
    env["DISPLAY"] = os.environ.get("DISPLAY", ":0")
    if "XAUTHORITY" not in env or not os.path.exists(env.get("XAUTHORITY", "")):
        try:
            xauth_files = glob.glob("/run/user/1000/xauth_*")
            if xauth_files:
                env["XAUTHORITY"] = xauth_files[0]
            elif os.path.exists(os.path.expanduser("~/.Xauthority")):
                env["XAUTHORITY"] = os.path.expanduser("~/.Xauthority")
        except Exception:
            pass
    return env

def get_mouse_xy():
    """Returns (x, y) tuple of current mouse cursor position without moving it."""
    try:
        res = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=get_env(), capture_output=True, text=True)
        x, y = None, None
        for line in res.stdout.splitlines():
            if line.startswith("X="):
                x = int(line.split("=")[1])
            elif line.startswith("Y="):
                y = int(line.split("=")[1])
        return x, y
    except Exception:
        return None, None

def is_user_actively_moving_mouse():
    """Returns True if the mouse cursor moved > 10 pixels in 80ms."""
    p1 = get_mouse_xy()
    time.sleep(0.08)
    p2 = get_mouse_xy()
    if p1[0] is None or p2[0] is None:
        return False
    delta = abs(p1[0] - p2[0]) + abs(p1[1] - p2[1])
    return delta > 10

def trigger_click(name, x, y, w, h):
    cx, cy = x + w // 2, y + h // 2

    # Check if mouse is already positioned over the button
    mx, my = get_mouse_xy()
    cursor_already_on_button = (
        mx is not None and my is not None and
        (x - 5) <= mx <= (x + w + 5) and (y - 5) <= my <= (y + h + 5)
    )

    if not cursor_already_on_button and is_user_actively_moving_mouse():
        log(f"Notice: User is actively moving mouse elsewhere; skipping cursor movement for '{name}'.")
        return False

    log(f"Clicking '{name}' at ({cx}, {cy}) (cursor_already_on_button={cursor_already_on_button})...")
    env = get_env()

    try:
        if not cursor_already_on_button:
            subprocess.run(["xdotool", "mousemove", str(cx), str(cy)], env=env, check=True)
            time.sleep(0.08)

        ydotool_clicked = False
        try:
            res = subprocess.run(["ydotool", "click", "0xC0"], capture_output=True, timeout=2)
            if res.returncode == 0:
                ydotool_clicked = True
        except Exception:
            pass

        try:
            subprocess.run(["xdotool", "click", "1"], env=env, check=True)
        except Exception as xe:
            if not ydotool_clicked:
                raise xe

        # Zero-Jump Restore
        if mx is not None and my is not None and not cursor_already_on_button:
            subprocess.run(["xdotool", "mousemove", str(mx), str(my)], env=env, check=True)

        log(f"Automation for '{name}' successful.")
        return True
    except Exception as e:
        log(f"Input error for '{name}': {e}")
        return False

def get_screenshot():
    tmp = "/tmp/auto_keep_all_scr.png"
    try:
        subprocess.run(["spectacle", "-b", "-n", "-o", tmp], check=True, capture_output=True, env=get_env())
        img = cv2.imread(tmp)
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
    """Loads all template variants per button."""
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
    log("Watcher starting (Once-Per-Presence State Machine)...")
    
    loaded_templates = load_templates()

    for name, tpl_list in loaded_templates.items():
        thresh = THRESHOLDS.get(name, 0.80)
        log(f"Loaded template: {name} ({len(tpl_list)} variants, threshold={thresh})")

    if not loaded_templates:
        log("No templates found. Exiting.")
        exit(1)
        
    last_reload = time.time()
    
    while True:
        try:
            if time.time() - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = time.time()

            screen = get_screenshot()
            if screen is not None:
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)
                
                current_frame_locations = set()

                for name, tpl_list in loaded_templates.items():
                    thresh = THRESHOLDS.get(name, 0.80)
                    for template_gray in tpl_list:
                        max_val, max_loc, best_shape = find_best_template_match(screen_gray, template_gray)
                        
                        if max_val >= thresh:
                            cx = max_loc[0] + best_shape[1] // 2
                            cy = max_loc[1] + best_shape[0] // 2
                            loc_key = (cx // 30, cy // 30)
                            current_frame_locations.add(loc_key)

                            if loc_key in active_clicked_buttons:
                                # Already clicked during this button presence; skip re-clicking!
                                break

                            log(f"NEW Button '{name}' detected at ({cx}, {cy}) (Conf: {max_val:.2f} >= {thresh})")
                            clicked = trigger_click(name, max_loc[0], max_loc[1], best_shape[1], best_shape[0])
                            if clicked:
                                active_clicked_buttons.add(loc_key)
                                time.sleep(2)
                            break

                disappeared = active_clicked_buttons - current_frame_locations
                for loc_key in disappeared:
                    active_clicked_buttons.remove(loc_key)

            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(3)
