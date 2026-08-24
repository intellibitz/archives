# Blukit: Graphical Assets Guide

This guide outlines how to generate and capture the required graphical assets for the Google Play Store using the built-in Compose Previews.

## 📸 Capturing Screenshots

I have established a dedicated preview file at `app/src/main/java/cc/thevar/blukit/ui/previews/MarketPreviews.kt` which contains optimized layouts for store screenshots.

### Steps to Capture:
1.  Open `MarketPreviews.kt` in Android Studio.
2.  Switch to the **Design** or **Split** view.
3.  Locate the following previews:
    -   `PreviewRadarPhone`: The main discovery interface.
    -   `PreviewChatPhone`: The **Intelligent Ticker** messaging interface.
    -   `PreviewRoseResonance`: Highlighting the **Stealth Rose** energy of private Ties.
    -   `PreviewProfileStealth`: The OLED-optimized identity setup.
    -   `PreviewRadarTablet`: Highlighting the adaptive layout on larger screens.
4.  Use the **"Copy Image"** or **"Screenshot"** icon in the preview header to save the high-resolution render.

## 🎨 Asset Specifications

| Asset | Size | Requirement |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | Use the generated output from the `Image Asset` wizard. |
| **Feature Graphic** | 1024 x 500 px | A high-contrast graphic with the Blukit logo. |
| **Phone Screenshots** | 1080 x 1920+ px | Capture from `PreviewRadarPhone` and `PreviewChatPhone`. |
| **Tablet Screenshots** | 2048 x 1536+ px | Capture from `PreviewRadarTablet`. |

## 🚀 Pro Tip
For the **Feature Graphic**, use a dark background (#000000) with **Stealth Rose** (#FF4081) and **Amber** (#FFB300) typography to reflect the **Rose Resonance** and **Unified Vibe Frequency**.
