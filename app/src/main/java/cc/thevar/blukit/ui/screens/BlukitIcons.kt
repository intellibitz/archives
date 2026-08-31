/**
 * BLUKIT ICONOGRAPHY
 *
 * This module contains specialized icons and menu action items.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.ui.theme.StealthPrimary

@Composable
fun StatusIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOn: Boolean,
    isWeak: Boolean = false,
    isPermissionMissing: Boolean = false,
    size: Dp = 24.dp,
    forceWarning: Boolean = false,
    onColor: Color = StealthPrimary,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusAnim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isPermissionMissing || !isOn && !forceWarning -> Color.Red
                forceWarning || isWeak -> Color.Yellow
                else -> onColor
            }.copy(alpha = if (!isOn || isWeak || forceWarning) alpha else 1f),
            modifier = Modifier.size(size * 0.65f)
        )
    }
}

@Composable
fun MenuActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}
