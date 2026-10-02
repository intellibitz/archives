import os
import time
import subprocess
import cv2
import numpy as np
import mss

# --- Configuration ---
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../assets"))
TEMPLATES = {
    "Keep All": [os.path.join(ASSETS_DIR, "keep-all.png")],
    "Proceed": [os.path.join(ASSETS_DIR, "proceed.png")],
    "Allow Always": [os.path.join(ASSETS_DIR, "allow-always.png")],
    "Accept All": [os.path.join(ASSETS_DIR, "accept-all.png")],
    "Accept All 2": [os.path.join(ASSETS_DIR, "accept-all2.png"), os.path.join(ASSETS_DIR, "accept_all2.png")],
    "Accept Changes": [os.path.join(ASSETS_DIR, "accept-changes.png"), os.path.join(ASSETS_DIR, "accept_changes.png"), os.path.join(ASSETS_DIR, "acceot-changes.png")],
    "Always Run": [os.path.join(ASSETS_DIR, "always-run.png")],
    "Allow Once": [os.path.join(ASSETS_DIR, "allow-once.png")],
    "Allow": [os.path.join(ASSETS_DIR, "allow.png"), os.path.join(ASSETS_DIR, "allow2.png")],
    "Yes Don't Ask": [os.path.join(ASSETS_DIR, "yes-dontask.png")]
}
LOG_FILE = "/tmp/keep_all_auto.log"

# Per-button confidence threshold mapping to handle different button anti-aliasing / contrast
BUTTON_THRESHOLDS = {
    "Accept All": 0.62,
    "Accept All 2": 0.62,
    "Accept Changes": 0.62,
    "Allow": 0.62,
    "Allow Once": 0.65,
    "Allow Always": 0.65,
    "Proceed": 0.70,
    "Keep All": 0.70,
    "Always Run": 0.65,
    "Yes Don't Ask": 0.62,
}
DEFAULT_THRESHOLD = 0.68
CHECK_INTERVAL = 2.0 

def log(msg):
    t = time.strftime("%H:%M:%S")
    line = f"[{t}] {msg}\n"
    try:
        with open(LOG_FILE, "a", buffering=1) as f:
            f.write(line)
    except:
        pass
    print(line.strip(), flush=True)

def get_mouse_xy():
    """Returns (x, y) tuple of current mouse cursor position."""
    env = os.environ.copy()
    env["DISPLAY"] = ":0"
    try:
        res = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=env, capture_output=True, text=True)
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

    # Guard: Do NOT touch or grab cursor if user is actively moving the mouse (>10px delta)
    if is_user_actively_moving_mouse():
        log(f"Notice: User is actively moving mouse; skipping cursor movement for '{name}'.")
        return False

    log(f"Clicking '{name}' at ({cx}, {cy})...")
    env = os.environ.copy()
    env["DISPLAY"] = ":0"

    try:
        # Save current mouse location to restore immediately after clicking
        orig_x, orig_y = get_mouse_xy()

        # Move to button and click
        subprocess.run([
            "xdotool", 
            "mousemove", str(cx), str(cy), 
            "click", "1"
        ], env=env, check=True)

        # Restore mouse cursor to user's original position instantly
        if orig_x is not None and orig_y is not None:
            time.sleep(0.02)
            subprocess.run(["xdotool", "mousemove", str(orig_x), str(orig_y)], env=env)
            
        log(f"Automation for '{name}' successful.")
        return True
    except Exception as e:
        log(f"Input error for '{name}': {e}")
        return False

def get_screenshot(sct):
    try:
        # Fast 5ms focusless memory screenshot via MSS
        monitor = sct.monitors[1] if len(sct.monitors) > 1 else sct.monitors[0]
        sct_img = sct.grab(monitor)
        img_np = np.array(sct_img) # BGRA image
        img_bgr = cv2.cvtColor(img_np, cv2.COLOR_BGRA2BGR)
        return img_bgr
    except Exception as e:
        log(f"Screenshot error: {e}")
        return None

def find_best_template_match(screen_gray, template_gray):
    """Multi-scale template matching (1.0x, 0.9x, 1.1x, 0.8x, 1.2x) for resolution independence."""
    best_val = -1.0
    best_loc = None
    best_shape = None
    
    scales = [1.0, 0.9, 1.1, 0.8, 1.2]
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
        if max_val > best_val:
            best_val = max_val
            best_loc = max_loc
            best_shape = resized_tpl.shape
            
    return best_val, best_loc, best_shape

def load_templates():
    loaded = {}
    for name, paths in TEMPLATES.items():
        if isinstance(paths, str):
            paths = [paths]
        for path in paths:
            if os.path.exists(path):
                tpl = cv2.imread(path)
                if tpl is not None:
                    # Convert to grayscale for robust template matching
                    tpl_gray = cv2.cvtColor(tpl, cv2.COLOR_BGR2GRAY)
                    loaded[name] = tpl_gray
                    break
                else:
                    log(f"Error: Could not read template {path}")
        if name not in loaded:
            log(f"Warning: Template missing for {name}")
    return loaded

if __name__ == "__main__":
    log("Watcher starting (Multi-Scale MSS Fast Focusless Screenshots + Smart Mouse Guard)...")
    
    loaded_templates = load_templates()

    for name in loaded_templates:
        log(f"Loaded template: {name}")

    if not loaded_templates:
        log("No templates found. Exiting.")
        exit(1)
        
    sct_instance = mss.MSS()
    last_reload = time.time()
    
    while True:
        try:
            # Periodically refresh templates every 60s in case images are updated/added
            if time.time() - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = time.time()

            screen = get_screenshot(sct_instance)
            if screen is not None:
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)
                
                for name, template_gray in loaded_templates.items():
                    threshold = BUTTON_THRESHOLDS.get(name, DEFAULT_THRESHOLD)
                    max_val, max_loc, best_shape = find_best_template_match(screen_gray, template_gray)
                    
                    if max_val >= threshold:
                        log(f"Button '{name}' detected via multi-scale grayscale (Conf: {max_val:.2f} >= Threshold: {threshold})")
                        clicked = trigger_click(name, max_loc[0], max_loc[1], best_shape[1], best_shape[0])
                        if clicked:
                            time.sleep(5) # Cooldown
                        break # Only handle one button per screenshot
            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(5)
