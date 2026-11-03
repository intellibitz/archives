package cc.thevar.blukit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import kotlinx.coroutines.delay

@Composable
fun MessageRippleEffect(
    isPrivate: Boolean = false,
    onFinished: () -> Unit
) {
    val rippleColor = if (isPrivate) StealthRose else StealthPrimary
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.85f) // Near the InteractionHub
        val maxRadius = size.maxDimension

        drawCircle(
            color = rippleColor.copy(alpha = (1f - animatable.value) * 0.6f),
            radius = animatable.value * maxRadius,
            center = center,
            style = Stroke(width = (4.dp.toPx() * (1f - animatable.value)).coerceAtLeast(1f))
        )
        
        drawCircle(
            color = rippleColor.copy(alpha = (1f - animatable.value) * 0.3f),
            radius = animatable.value * maxRadius * 0.7f,
            center = center,
            style = Stroke(width = (2.dp.toPx() * (1f - animatable.value)).coerceAtLeast(1f))
        )
    }
}
