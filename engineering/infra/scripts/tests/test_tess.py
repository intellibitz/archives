import time, cv2, pytesseract
img = cv2.imread('/tmp/test_scr.png')
# Downscale
img = cv2.resize(img, (0,0), fx=0.5, fy=0.5)

t0 = time.time()
whitelist = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 "
data = pytesseract.image_to_data(img, config=f'--psm 11 -c tessedit_char_whitelist={whitelist}')
print('Time with whitelist:', time.time() - t0)
