import cv2
import pytesseract
import numpy as np

img = np.zeros((200, 400, 3), dtype=np.uint8)
cv2.putText(img, 'Keep All', (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
data = pytesseract.image_to_data(img, output_type=pytesseract.Output.DICT)

def find_phrase(data, phrase):
    words = phrase.split()
    n = len(words)
    texts = data['text']
    for i in range(len(texts) - n + 1):
        match = True
        for j in range(n):
            if texts[i+j].lower() != words[j].lower():
                match = False
                break
        if match:
            x = data['left'][i]
            y = data['top'][i]
            w = data['left'][i+n-1] + data['width'][i+n-1] - x
            h = max(data['height'][i:i+n])
            return x, y, w, h
    return None

print(find_phrase(data, 'Keep All'))
