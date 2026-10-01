import cv2
import numpy as np
import time

prev = np.zeros((1080, 1920, 3), dtype=np.uint8)
curr = np.zeros((1080, 1920, 3), dtype=np.uint8)
cv2.putText(curr, 'Keep All', (900, 500), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)

t0 = time.time()
diff = cv2.absdiff(curr, prev)
gray = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
_, thresh = cv2.threshold(gray, 25, 255, cv2.THRESH_BINARY)
x, y, w, h = cv2.boundingRect(thresh)
print("Changed region:", x, y, w, h)
if w > 0 and h > 0:
    cropped = curr[y:y+h, x:x+w]
    import pytesseract
    # Try OCR on cropped
    t1 = time.time()
    data = pytesseract.image_to_data(cropped, output_type=pytesseract.Output.DICT)
    print("OCR time:", time.time() - t1)
print("Total time:", time.time() - t0)
