# Blukit: Graphical Assets Guide

This guide outlines how to generate and capture the required graphical assets for the Google Play Store using the built-in Compose Previews.

## 📸 Capturing Screenshots

A dedicated preview file at `app/src/main/java/cc/thevar/blukit/ui/previews/MarketPreviews.kt` contains optimized layouts for store screenshots.

### Steps to Capture:
1.  Open `MarketPreviews.kt` in Android Studio.
2.  Switch to the **Design** or **Split** view.
3.  Locate the following previews:
    -   `PreviewRadarPhone`: The **Nearby** screen showing the list of available **People** and **Public Groups** in the immediate physical proximity.
    -   `PreviewChatPhone`: The **Private Chat** interface showing secure, encrypted messaging with standard chat bubbles and clear delivery status.
    -   `PreviewTimelineField`: The **History** view showing chronological records of all conversations and shared items.
    -   `PreviewMessageField`: The detailed view of a specific interaction or shared document.
    -   `PreviewBlukitToolbar`: The clean, modern toolbar with subtle Assistant mood indicators.
    -   `PreviewHumanityStage`: The profile and group identity anchor.
    -   `PreviewGroupRitualGhost`: The interface for creating new, localized **Public Groups**.
    -   `PreviewSourceOptionsMenu`: The control menu for managing peers (Block, Message, Add to Group).
4.  Use the **"Copy Image"** or **"Screenshot"** icon in the preview header to save the high-resolution render.

## 🎨 Asset Specifications

| Asset | Size | Requirement |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | Use the generated output from `PreviewPlayStoreIcon`. |
| **Feature Graphic** | 1024 x 500 px | Capture from `PreviewFeatureGraphic`. Use "OWN YOUR DATA" typography. |
| **Phone Screenshots** | 1080 x 1920+ px | Capture from `PreviewRadarPhone`, `PreviewChatPhone`, and `PreviewTimelineField`. |

## 🚀 Pro Tip
For the **Feature Graphic**, use the true-black background (#030507) with **Stealth Rose** (#FF4081) and **Amber** (#FFB300) accents to reflect the secure and intelligent nature of the Blukit experience.
