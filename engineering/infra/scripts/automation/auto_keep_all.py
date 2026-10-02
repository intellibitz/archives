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

BUTTON_TARGET_LABELS = [
    "Accept All", "Accept Changes", "Proceed", "Keep All", 
    "Allow Always", "Allow Once", "Always Run", "Allow", "Yes, Don't Ask"
]

BUTTON_THRESHOLDS = {
    "Accept All": 0.58,
    "Accept All 2": 0.58,
    "Accept Changes": 0.58,
    "Allow": 0.58,
    "Allow Once": 0.60,
    "Allow Always": 0.60,
    "Proceed": 0.65,
    "Keep All": 0.65,
    "Always Run": 0.60,
    "Yes Don't Ask": 0.58,
}
DEFAULT_THRESHOLD = 0.65
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

    # Guard: Do NOT touch cursor if user is actively moving the mouse (>10px)
    if is_user_actively_moving_mouse():
        log(f"Notice: User is actively moving mouse; skipping cursor movement for '{name}'.")
        return False

    log(f"Clicking '{name}' at ({cx}, {cy})...")
    env = os.environ.copy()
    env["DISPLAY"] = ":0"

    try:
        orig_x, orig_y = get_mouse_xy()

        subprocess.run([
            "xdotool", 
            "mousemove", str(cx), str(cy), 
            "click", "1"
        ], env=env, check=True)

        if orig_x is not None and orig_y is not None:
            time.sleep(0.02)
            subprocess.run(["xdotool", "mousemove", str(orig_x), str(orig_y)], env=env)
            
        log(f"Automation for '{name}' successful.")
        return True
    except Exception as e:
        log(f"Input error for '{name}': {e}")
        return False

def check_atspi_buttons():
    """100% exact text string matching via Linux AT-SPI D-Bus accessibility tree."""
    try:
        import pyatspi
        desktop = pyatspi.Registry.getDesktop(0)
        
        def search_node(node, depth=0):
            if depth > 7:
                return None
            try:
                role = node.getRoleName()
                name = node.name
                if name and ('push button' in role or 'button' in role or 'dialog' in role):
                    for label in BUTTON_TARGET_LABELS:
                        if label.lower() in name.lower():
                            bbox = node.get_position(pyatspi.DESKTOP_COORDS)
                            size = node.get_size()
                            if bbox and size and size[0] > 0 and size[1] > 0:
                                return label, bbox[0], bbox[1], size[0], size[1]
                for child in node:
                    if child:
                        match = search_node(child, depth + 1)
                        if match:
                            return match
            except Exception:
                pass
            return None

        for app in desktop:
            if app:
                match = search_node(app)
                if match:
                    return match
    except Exception:
        pass
    return None

def get_screenshot(sct):
    try:
        monitor = sct.monitors[1] if len(sct.monitors) > 1 else sct.monitors[0]
        sct_img = sct.grab(monitor)
        img_np = np.array(sct_img) # BGRA
        img_bgr = cv2.cvtColor(img_np, cv2.COLOR_BGRA2BGR)
        return img_bgr
    except Exception as e:
        log(f"Screenshot error: {e}")
        return None

def find_best_template_match(screen_gray, template_gray):
    """Multi-scale & inverted contrast template matching."""
    best_val = -1.0
    best_loc = None
    best_shape = None
    
    # Check standard and inverted contrast (for dark/light theme adaptability)
    screen_variants = [screen_gray, cv2.bitwise_not(screen_gray)]
    scales = [1.0, 0.9, 1.1, 0.8, 1.2]
    
    for scr in screen_variants:
        for scale in scales:
            if scale == 1.0:
                resized_tpl = template_gray
            else:
                w = int(template_gray.shape[1] * scale)
                h = int(template_gray.shape[0] * scale)
                if w <= 0 or h <= 0 or w > scr.shape[1] or h > scr.shape[0]:
                    continue
                interpolation = cv2.INTER_AREA if scale < 1.0 else cv2.INTER_CUBIC
                resized_tpl = cv2.resize(template_gray, (w, h), interpolation=interpolation)
                
            res = cv2.matchTemplate(scr, resized_tpl, cv2.TM_CCOEFF_NORMED)
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
                    tpl_gray = cv2.cvtColor(tpl, cv2.COLOR_BGR2GRAY)
                    loaded[name] = tpl_gray
                    break
        if name not in loaded:
            log(f"Warning: Template missing for {name}")
    return loaded

if __name__ == "__main__":
    log("Watcher starting (AT-SPI Accessibility + Dual-Pass Multi-Scale OpenCV + Smart Mouse Guard)...")
    
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
            if time.time() - last_reload > 60:
                loaded_templates = load_templates()
                last_reload = time.time()

            # Method 1: Check AT-SPI Accessibility D-Bus Tree (Exact Text Matching)
            atspi_match = check_atspi_buttons()
            if atspi_match:
                name, bx, by, bw, bh = atspi_match
                log(f"Button '{name}' detected via AT-SPI D-Bus Accessibility at ({bx}, {by})")
                clicked = trigger_click(name, bx, by, bw, bh)
                if clicked:
                    time.sleep(5)
                continue

            # Method 2: In-Memory Multi-Scale Contrast-Adaptive OpenCV Template Matching
            screen = get_screenshot(sct_instance)
            if screen is not None:
                screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)
                
                for name, template_gray in loaded_templates.items():
                    threshold = BUTTON_THRESHOLDS.get(name, DEFAULT_THRESHOLD)
                    max_val, max_loc, best_shape = find_best_template_match(screen_gray, template_gray)
                    
                    if max_val >= threshold:
                        log(f"Button '{name}' detected via multi-scale vision (Conf: {max_val:.2f} >= Threshold: {threshold})")
                        clicked = trigger_click(name, max_loc[0], max_loc[1], best_shape[1], best_shape[0])
                        if clicked:
                            time.sleep(5)
                        break
            time.sleep(CHECK_INTERVAL)
        except Exception as e:
            log(f"Loop error: {e}")
            time.sleep(5)
