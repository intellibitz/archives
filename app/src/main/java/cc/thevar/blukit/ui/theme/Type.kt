package cc.thevar.blukit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Major Headline: Field/Screen titles (e.g., Nearby)
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold, // Relaxed from Black
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp // Relaxed from 2
    ),
    // Tactical Titles: Card titles (e.g., Crowd Name)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Relaxed from Black
        fontSize = 15.sp, // Increased from 14
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp // Relaxed from 1
    ),
    // Primary Content: Message body
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Normal for reading long messages
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp, // Removed letter spacing for body
    ),
    // Secondary Content: Sender names, subtitles
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Relaxed from Bold
        fontSize = 13.sp, // Increased from 12
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    // Tactical Labels: Buttons, Indicators, Timestamps
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Relaxed from Black
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp // Relaxed from 1.5
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Relaxed from Black
        fontSize = 11.sp, // Increased from 10
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp // Relaxed from 0.5
    )
)
