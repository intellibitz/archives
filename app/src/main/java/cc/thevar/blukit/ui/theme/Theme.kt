package cc.thevar.blukit.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
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
    onTertiaryContainer = BlukitDarkOnTertiaryContainer
)

private val StealthColorScheme = darkColorScheme(
    primary = StealthPrimary,
    onPrimary = StealthOnPrimary,
    secondary = StealthSecondary,
    onSecondary = StealthOnSecondary,
    background = StealthBlack,
    surface = StealthBlack,
    onBackground = Color.White,
    onSurface = Color.White,
    error = StealthError,
    surfaceVariant = StealthGray
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
    stealthMode: Boolean = true, // Default to Stealth for Blukit feel
    dynamicColor: Boolean = false, // Prefer our hand-crafted Stealth palette
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
        content = {
            if (stealthMode) {
                Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
                    BlukitAtmosphere()
                    content()
                }
            } else {
                content()
            }
        }
    )
}

/**
 * Global "Blukit Atmosphere": Suble shimmering grain to make every pixel feel the vibe.
 */
@Composable
fun BlukitAtmosphere() {
    val infiniteTransition = rememberInfiniteTransition(label = "Atmosphere")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Shimmer"
    )

    val dots = remember {
        List(150) {
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEach { dot ->
            drawCircle(
                color = StealthPrimary.copy(alpha = shimmer * 0.1f),
                radius = 1f,
                center = Offset(dot.x * size.width, dot.y * size.height)
            )
        }
    }
}
