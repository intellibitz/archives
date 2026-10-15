import os
import cv2
import sys
import numpy as np

# Ensure automation folder is in python path
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
AUTOMATION_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../automation"))
sys.path.insert(0, AUTOMATION_DIR)

import auto_keep_all

def test_blue_and_grey_variants():
    templates = auto_keep_all.load_templates()
    assets_dir = auto_keep_all.ASSETS_DIR

    test_targets = [
        ("Accept All", "accept-all.png", "accept-all-grey.png"),
        ("Accept All 2", "accept-all2.png", "accept-all2-grey.png"),
        ("Accept Changes", "accept-changes.png", "accept-changes-grey.png"),
    ]

    for btn_name, blue_file, grey_file in test_targets:
        blue_path = os.path.join(assets_dir, blue_file)
        grey_path = os.path.join(assets_dir, grey_file)

        assert os.path.exists(blue_path), f"Missing blue asset: {blue_file}"
        assert os.path.exists(grey_path), f"Missing grey asset: {grey_file}"

        blue_gray = cv2.cvtColor(cv2.imread(blue_path), cv2.COLOR_BGR2GRAY)
        grey_gray = cv2.cvtColor(cv2.imread(grey_path), cv2.COLOR_BGR2GRAY)

        # Verify blue match
        max_blue = max(
            auto_keep_all.find_best_template_match(blue_gray, tpl)[0]
            for tpl in templates[btn_name]
        )
        assert max_blue >= auto_keep_all.CONFIDENCE_THRESHOLD, (
            f"{btn_name} blue match {max_blue:.2f} < threshold {auto_keep_all.CONFIDENCE_THRESHOLD}"
        )

        # Verify grey (hover/active state) match
        max_grey = max(
            auto_keep_all.find_best_template_match(grey_gray, tpl)[0]
            for tpl in templates[btn_name]
        )
        assert max_grey >= auto_keep_all.CONFIDENCE_THRESHOLD, (
            f"{btn_name} grey match {max_grey:.2f} < threshold {auto_keep_all.CONFIDENCE_THRESHOLD}"
        )

        print(f"✓ {btn_name}: Blue ({max_blue:.2f}), Grey ({max_grey:.2f}) passed.")

if __name__ == "__main__":
    test_blue_and_grey_variants()
    print("All template tests passed successfully!")
