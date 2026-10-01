import os
import time
import subprocess
import cv2

# --- Configuration ---
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
TEMPLATES = {
    "Keep All": [os.path.join(SCRIPT_DIR, "keep-all.png")],
    "Proceed": [os.path.join(SCRIPT_DIR, "proceed.png")],
    "Allow Always": [os.path.join(SCRIPT_DIR, "allow-always.png")],
    "Accept All": [os.path.join(SCRIPT_DIR, "accept-all.png")],
    "Accept All 2": [os.path.join(SCRIPT_DIR, "accept-all2.png"), os.path.join(SCRIPT_DIR, "accept_all2.png")],
    "Accept Changes": [os.path.join(SCRIPT_DIR, "accept-changes.png"), os.path.join(SCRIPT_DIR, "accept_changes.png"), os.path.join(SCRIPT_DIR, "acceot-changes.png")],
    "Always Run": [os.path.join(SCRIPT_DIR, "always-run.png")],
    "Allow Once": [os.path.join(SCRIPT_DIR, "allow-once.png")],
    "Allow": [os.path.join(SCRIPT_DIR, "allow.png"), os.path.join(SCRIPT_DIR, "allow2.png")]
}
LOG_FILE = "/tmp/keep_all_auto.log"
# We lower the confidence slightly because edge matching can be a bit more sensitive to sub-pixel rendering differences
CONFIDENCE_THRESHOLD = 0.60
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

def get_screen_center():
    env = os.environ.copy()
    env["DISPLAY"] = ":0"
    try:
        res = subprocess.run(["xdotool", "getdisplaygeometry"], env=env, capture_output=True, text=True, check=True)
        parts = res.stdout.strip().split()
        return int(parts[0]) // 2, int(parts[1]) // 2
    except Exception:
        return 960, 600

SCREEN_CENTER_X, SCREEN_CENTER_Y = get_screen_center()

def trigger_click(name, x, y, w, h):
    cx, cy = x + w // 2, y + h // 2
    log(f"Clicking '{name}' at ({cx}, {cy})...")
    env = os.environ.copy()
    env["DISPLAY"] = ":0"
    
    try:
        from Xlib import display
        d = display.Display()
        q = d.screen().root.query_pointer()
        if q.mask & 0x1F00:
            log(f"User is actively using the mouse (button pressed). Skipping click for '{name}'.")
            return False
    except Exception as e:
        log(f"Warning: could not get mouse state: {e}")

    try:
        # Save current mouse location to restore after clicking
        res = subprocess.run(["xdotool", "getmouselocation", "--shell"], env=env, capture_output=True, text=True)
        orig_x, orig_y = None, None
        for line in res.stdout.splitlines():
            if line.startswith("X="):
                orig_x = line.split("=")[1]
            elif line.startswith("Y="):
                orig_y = line.split("=")[1]

        # Move to button and click natively for ALL buttons (including Keep All)
        subprocess.run([
            "xdotool", 
            "mousemove", str(cx), str(cy), 
            "click", "1"
        ], env=env, check=True)

        # Restore mouse cursor
        if orig_x is not None and orig_y is not None:
            time.sleep(0.05)
            subprocess.run(["xdotool", "mousemove", str(orig_x), str(orig_y)], env=env)
            
        log(f"Automation for '{name}' successful.")
        return True
    except Exception as e:
        log(f"Input error for '{name}': {e}")
        return False

def get_screenshot():
    tmp = "/tmp/gha_keep_all_scr.png"
    try:
        subprocess.run(["spectacle", "-b", "-n", "-o", tmp], check=True, capture_output=True)
        img = cv2.imread(tmp)
        return img
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
                    # Convert to grayscale and then to edges
                    tpl_gray = cv2.cvtColor(tpl, cv2.COLOR_BGR2GRAY)
                    tpl_edges = cv2.Canny(tpl_gray, 50, 150)
                    loaded[name] = tpl_edges
                    break
                else:
                    log(f"Error: Could not read template {path}")
        if name not in loaded:
            log(f"Warning: Template missing for {name}")
    return loaded

if __name__ == "__main__":
    log(f"Watcher starting (Screen Center: {SCREEN_CENTER_X}, {SCREEN_CENTER_Y})...")
    
    loaded_templates = load_templates()

    for name in loaded_templates:
        log(f"Loaded template: {name}")

    if not loaded_templates:
        log("No templates found. Exiting.")
        exit(1)
        
    last_reload = time.time()
    while True:
        try:
            # Periodically refresh templates every 60s in case images are updated/added
            if time.time() - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = time.time()

            screen = get_screenshot()
            if screen is not None:
                # Convert screen to edges for color-independent matching
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)
                screen_edges = cv2.Canny(screen_gray, 50, 150)
                
                for name, template_edges in loaded_templates.items():
                    res = cv2.matchTemplate(screen_edges, template_edges, cv2.TM_CCOEFF_NORMED)
                    _, max_val, _, max_loc = cv2.minMaxLoc(res)
                    if max_val >= CONFIDENCE_THRESHOLD:
                        log(f"Button '{name}' detected via edges (Conf: {max_val:.2f})")
                        # template_edges.shape gives (height, width)
                        clicked = trigger_click(name, max_loc[0], max_loc[1], template_edges.shape[1], template_edges.shape[0])
                        if clicked:
                            time.sleep(5) # Cooldown
                        break # Only handle one button per screenshot
            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(5)
