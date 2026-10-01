import cv2
import numpy as np
import time

# Create dummy screen and template
screen = np.zeros((1080, 1920, 3), dtype=np.uint8)
cv2.putText(screen, 'Keep All', (500, 500), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2)

template = np.zeros((50, 200, 3), dtype=np.uint8)
cv2.putText(template, 'Keep All', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

t0 = time.time()
screen_gray = cv2.cvtColor(screen, cv2.COLOR_BGR2GRAY)
template_gray = cv2.cvtColor(template, cv2.COLOR_BGR2GRAY)

screen_edges = cv2.Canny(screen_gray, 50, 150)
template_edges = cv2.Canny(template_gray, 50, 150)

res = cv2.matchTemplate(screen_edges, template_edges, cv2.TM_CCOEFF_NORMED)
_, max_val, _, max_loc = cv2.minMaxLoc(res)

print("Time:", time.time() - t0, "Conf:", max_val, "Loc:", max_loc)
