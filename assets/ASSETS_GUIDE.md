# Blukit: Graphical Assets Guide

This guide outlines how to generate and capture the required graphical assets for the Google Play Store using the built-in Compose Previews.

## 📸 Capturing Screenshots

I have established a dedicated preview file at `app/src/main/java/cc/thevar/blukit/ui/previews/MarketPreviews.kt` which contains optimized layouts for store screenshots.

### Steps to Capture:
1.  Open `MarketPreviews.kt` in Android Studio.
2.  Switch to the **Design** or **Split** view.
3.  Locate the following previews:
    -   `PreviewRadarPhone`: The main **Sensing Radar** showing the full-screen **Resonance Stream** overlaid on the atmospheric **Resonance Field**. Features animated **Energy Trails** around Sources and the AI-driven **Vibe Heatmap**. Includes the top **Synthesis Aura** glowing with the Sphere's current trend.
    -   `PreviewChatPhone`: The **Sphere Field** and **Private Sphere** interface showing **Resonance** headers with **Interactive Mini Radars**, high-density "JOIN" affordances, and Source counts.
    -   `PreviewTimelineField`: The **Ledger** view showing shared **Records** grouped by **Synthesis Events** (e.g., "Study Session", "Social Hour") and the "Anchored" persistence indicators.
    -   `PreviewEchoField`: The granular breakdown of a specific **Echo** into its constituent units.
    -   `PreviewTacticalHeader`: The low-profile Row 0 resonance controls (Eco, Stealth, Radios).
    -   `PreviewHumanityStage`: The Row 1 navigation and identity anchor, now situated in the Sensing Header.
    -   `PreviewSphereRitualGhost`: The **Resonance Ghost** interaction model for naming Spheres and selecting **Templates**.
4.  Use the **"Copy Image"** or **"Screenshot"** icon in the preview header to save the high-resolution render.

## 🎨 Asset Specifications

| Asset | Size | Requirement |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | Use the generated output from `PreviewPlayStoreIcon`. |
| **Feature Graphic** | 1024 x 500 px | Capture from `PreviewFeatureGraphic`. Use "OWN YOUR ECHO" typography. |
| **Phone Screenshots** | 1080 x 1920+ px | Capture from `PreviewRadarPhone`, `PreviewChatPhone`, and `PreviewTimelineField`. |

## 🚀 Pro Tip
For the **Feature Graphic**, use a dark background (#000000) with **Stealth Rose** (#FF4081) and **Amber** (#FFB300) gradients to reflect the **Rose Resonance** and the shifting atmospheric colors of the field.
