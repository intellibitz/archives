# Blukit: Graphical Assets Guide

This guide outlines how to generate and capture the required graphical assets for the Google Play Store using the built-in Compose Previews.

## 📸 Capturing Screenshots

I have established a dedicated preview file at `app/src/main/java/cc/thevar/blukit/ui/previews/MarketPreviews.kt` which contains optimized layouts for store screenshots.

### Steps to Capture:
1.  Open `MarketPreviews.kt` in Android Studio.
2.  Switch to the **Design** or **Split** view.
3.  Locate the following previews:
    -   `PreviewRadarPhone`: The main **Atmos Field** showing AIR METAs and the spectral radar.
    -   `PreviewChatPhone`: The **Air Field** and **Tie Field** interface showing METAs and granular units.
    -   `PreviewVibeField`: The granular breakdown of a specific **Vibe Meta**.
    -   `PreviewHarmonyHubFull`: The complete Harmony Hub architecture with recursive meta-expansion.
    -   `PreviewAirRitualGhost`: The **Vibe Ghost** interaction model for naming Airs.
    -   `PreviewVibingVibesTickerHeaders`: High-density feed showcasing varied vibe types.
    -   `PreviewRadarTablet`: The adaptive layout for larger spectral fields.
4.  Use the **"Copy Image"** or **"Screenshot"** icon in the preview header to save the high-resolution render.

## 🎨 Asset Specifications

| Asset | Size | Requirement |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | Use the generated output from `PreviewPlayStoreIcon`. |
| **Feature Graphic** | 1024 x 500 px | Capture from `PreviewFeatureGraphic`. |
| **Phone Screenshots** | 1080 x 1920+ px | Capture from `PreviewRadarPhone`, `PreviewChatPhone`, and `PreviewHarmonyHubFull`. |
| **Tablet Screenshots** | 2048 x 1536+ px | Capture from `PreviewRadarTablet`. |

## 🚀 Pro Tip
For the **Feature Graphic**, use a dark background (#000000) with **Stealth Rose** (#FF4081) and **Amber** (#FFB300) typography to reflect the **Rose Resonance** and **Unified Vibe Frequency**.
