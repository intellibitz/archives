# Blukit: Graphical Assets Guide

This guide outlines how to generate and capture the required graphical assets for the Google Play Store using the built-in Compose Previews.

## 📸 Capturing Screenshots

I have established a dedicated preview file at `app/src/main/java/cc/thevar/blukit/ui/previews/MarketPreviews.kt` which contains optimized layouts for store screenshots.

### Steps to Capture:
1.  Open `MarketPreviews.kt` in Android Studio.
2.  Switch to the **Design** or **Split** view.
3.  Locate the following previews:
    -   `PreviewRadarPhone`: The main **The Crowd** field showing the spatial radar with its centralized context anchor, prominent identity anchor ("YOU"), and the unified tactical header with "RADAR/SEARCH" labels. Includes the **Spectral Dimming** effect and the AI-driven **Atmospheric Heatmap** and **Aura Glow**.
    -   `PreviewChatPhone`: The **Crowd Field** and **Chain Field** interface showing **Ties** and the **Resonance** headers with high-density "ENTER" affordances and user counts.
    -   `PreviewTimelineField`: The visual student journey showing shared **Memories**.
    -   `PreviewPulseField`: The granular breakdown of a specific **Pulse** into its constituent units.
    -   `PreviewTacticalHeader`: The low-profile Row 0 tactical controls (Eco, Stealth, Radios).
    -   `PreviewHumanityStage`: The Row 1 navigation and identity anchor, now situated in the Radar Header.
    -   `PreviewCrowdRitualGhost`: The **Pulse Ghost** interaction model for naming Crowds and selecting **Templates**.
    -   `PreviewPulsingResonanceTickerHeaders`: High-density feed showcasing varied pulse types and unit counts.
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
For the **Feature Graphic**, use a dark background (#000000) with **Stealth Rose** (#FF4081) and **Amber** (#FFB300) typography to reflect the **Rose Resonance** and **Unified Pulse Frequency**.
