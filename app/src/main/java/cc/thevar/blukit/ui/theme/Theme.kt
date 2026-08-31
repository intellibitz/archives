package cc.thevar.blukit.ui.theme

import android.os.Build
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

private val DarkColorScheme = darkColorScheme(
    primary = BlukitDarkPrimary,
    onPrimary = BlukitDarkOnPrimary,
    primaryContainer = BlukitDarkPrimaryContainer,
    onPrimaryContainer = BlukitDarkOnPrimaryContainer,
    secondary = BlukitDarkSecondary,
    onSecondary = BlukitDarkOnSecondary,
    secondaryContainer = BlukitDarkSecondaryContainer,
    onSecondaryContainer = BlukitDarkOnSecondaryContainer,
    tertiary = BlukitDarkTertiary,
    onTertiary = BlukitDarkOnTertiary,
    tertiaryContainer = BlukitDarkTertiaryContainer,
    onTertiaryContainer = BlukitDarkOnTertiaryContainer,
)

private val StealthColorScheme = darkColorScheme(
    primary = StealthPrimary,
    onPrimary = StealthOnPrimary,
    secondary = StealthSecondary,
    onSecondary = StealthOnSecondary,
    background = StealthBlack,
    surface = StealthSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    error = StealthError,
    surfaceVariant = StealthSurfaceVariant,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outlineVariant = StealthOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = BlukitAcademicBlue,
    onPrimary = Color.White,
    primaryContainer = BlukitAcademicBlue.copy(alpha = 0.1f),
    onPrimaryContainer = BlukitAcademicBlue,
    secondary = BlukitAcademicGold,
    onSecondary = Color.White,
    background = BlukitPaper,
    surface = BlukitPaper,
    onBackground = BlukitInk,
    onSurface = BlukitInk,
    surfaceVariant = Color.White,
    onSurfaceVariant = BlukitInk.copy(alpha = 0.7f)
)

@Composable
fun BlukitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    stealthMode: Boolean = true, // Default to Stealth for Blukit feel
    dynamicColor: Boolean = false, // Prefer our hand-crafted Stealth palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        stealthMode -> StealthColorScheme
        (dynamicColor && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            if (stealthMode) {
                Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
                    content()
                }
            } else {
                content()
            }
        }
    )
}
