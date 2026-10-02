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
    "Allow": [os.path.join(ASSETS_DIR, "allow.png"), os.path.join(ASSETS_DIR, "allow2.png")]
}
LOG_FILE = "/tmp/keep_all_auto.log"

# Strict confidence threshold (0.80) to eliminate false positive cursor grabs
CONFIDENCE_THRESHOLD = 0.80
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

def is_user_actively_moving_mouse():
    """Returns True if the user is currently moving the cursor."""
    env = os.environ.copy()
    env["DISPLAY"] = ":0"
    try:
        res1 = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=env, capture_output=True, text=True)
        time.sleep(0.08)
        res2 = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=env, capture_output=True, text=True)
        return res1.stdout != res2.stdout
    except Exception:
        return False

def trigger_click(name, x, y, w, h):
    cx, cy = x + w // 2, y + h // 2

    # Guard: Do NOT touch or grab cursor if user is actively moving the mouse
    if is_user_actively_moving_mouse():
        log(f"Notice: User is actively moving mouse; skipping cursor movement for '{name}'.")
        return False

    log(f"Clicking '{name}' at ({cx}, {cy})...")
    env = os.environ.copy()
    env["DISPLAY"] = ":0"

    try:
        # Save current mouse location to restore immediately after clicking
        res = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=env, capture_output=True, text=True)
        orig_x, orig_y = None, None
        for line in res.stdout.splitlines():
            if line.startswith("X="):
                orig_x = line.split("=")[1]
            elif line.startswith("Y="):
                orig_y = line.split("=")[1]

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
    log("Watcher starting (MSS Fast Focusless Screenshots)...")
    
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
                    res = cv2.matchTemplate(screen_gray, template_gray, cv2.TM_CCOEFF_NORMED)
                    _, max_val, _, max_loc = cv2.minMaxLoc(res)
                    if max_val >= CONFIDENCE_THRESHOLD:
                        log(f"Button '{name}' detected via grayscale (Conf: {max_val:.2f})")
                        clicked = trigger_click(name, max_loc[0], max_loc[1], template_gray.shape[1], template_gray.shape[0])
                        if clicked:
                            time.sleep(5) # Cooldown
                        break # Only handle one button per screenshot
            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(5)
