package cc.thevar.blukit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    onTertiaryContainer = BlukitDarkOnTertiaryContainer
)

private val StealthColorScheme = darkColorScheme(
    primary = StealthPrimary,
    onPrimary = StealthOnPrimary,
    secondary = StealthSecondary,
    onSecondary = StealthOnSecondary,
    background = StealthBackground,
    surface = StealthSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BlukitPrimary,
    onPrimary = BlukitOnPrimary,
    primaryContainer = BlukitPrimaryContainer,
    onPrimaryContainer = BlukitOnPrimaryContainer,
    secondary = BlukitSecondary,
    onSecondary = BlukitOnSecondary,
    secondaryContainer = BlukitSecondaryContainer,
    onSecondaryContainer = BlukitOnSecondaryContainer,
    tertiary = BlukitTertiary,
    onTertiary = BlukitOnTertiary,
    tertiaryContainer = BlukitTertiaryContainer,
    onTertiaryContainer = BlukitOnTertiaryContainer
)

@Composable
fun BlukitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    stealthMode: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        stealthMode -> StealthColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
