/**
 * BLUKIT RADAR COMPONENTS
 *
 * This module handles spatial visualizations, including the 
 * Peer Radar and Connection Heatmaps.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun VibeHeatmap(energy: Float, themeColor: Color, trend: String? = null) {
    val atmosphereColor = when (trend) {
        "ACADEMIC" -> AssistantAcademic
        "TRANSIT" -> AssistantTransit
        "SOCIAL" -> AssistantSocial
        "DINING" -> AssistantFood
        "WORK" -> AssistantAction
        else -> themeColor
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.8f
        
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(
                    atmosphereColor.copy(alpha = 0.15f * energy),
                    atmosphereColor.copy(alpha = 0.05f * energy),
                    androidx.compose.ui.graphics.Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}

/**
 * PEER RADAR LAYER: A full-screen spatial visualization of nearby People.
 */
@Composable
fun PeerRadarLayer(
    devices: List<Source>,
    onDeviceClick: (Source) -> Unit,
    onDeviceLongClick: (Source) -> Unit,
    selectedDevices: Set<String>,
    pulsedPeers: Set<String>,
    bubbleSenders: Set<String>,
    themeColor: Color,
    density: androidx.compose.ui.unit.Density
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(24.dp)
    ) {
        val maxRadiusPx = with(density) { 140.dp.toPx() }

        devices.forEachIndexed { index, device ->
            val id = device.persistentId ?: device.id
            val isPulsed = id in pulsedPeers
            val isSelected = device.id in selectedDevices
            
            val proximity = device.proximityFactor
            val radiusValue = (1f - proximity) * maxRadiusPx + with(density) { 60.dp.toPx() }
            val angle = (index.toDouble() / devices.size.coerceAtLeast(1)) * 2 * PI
            
            val offsetX = (radiusValue * cos(angle)).toFloat()
            val offsetY = (radiusValue * sin(angle)).toFloat()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = with(density) { offsetX.toDp() }, y = with(density) { offsetY.toDp() })
            ) {
                PersonaSignature(
                    source = device,
                    isPulsed = isPulsed,
                    isSelected = isSelected,
                    size = 40.dp,
                    onClick = { onDeviceClick(device) },
                    onLongClick = { onDeviceLongClick(device) }
                )
            }
        }
    }
}

/**
 * MINI PEER VIEW: A lightweight view for a specific Group context.
 */
@Composable
fun GroupMiniRadar(
    group: Group,
    members: List<Source>,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    isDefaultGroup: Boolean = false,
    onSourceClick: (Source) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    activeBubbles: List<BubbleData> = emptyList()
) {
    val bubbleSenders = remember(activeBubbles) { activeBubbles.asSequence().map { it.senderId }.toSet() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isDefaultGroup) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(themeColor.copy(alpha = 0.2f))
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy((-12).dp) 
                ) {
                    members.take(10).forEach { device ->
                        PersonaSignature(
                            source = device,
                            isPulsed = bubbleSenders.contains(device.id) || bubbleSenders.contains(device.persistentId),
                            isSelected = false,
                            size = 32.dp,
                            themeColor = themeColor,
                            onClick = { onSourceClick(device) },
                            onLongClick = { onSourceLongClick(device) }
                        )
                    }
                    if (members.size > 10) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StealthBlack.copy(alpha = 0.5f))
                                .border(0.5.dp, themeColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${members.size - 10}",
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            val owner = members.find { it.id == group.ownerId || it.persistentId == group.ownerId }
            val centerEmoji = owner?.emoji ?: group.projectionEmoji ?: "⚡"

            Box(modifier = Modifier.zIndex(2f)) {
                PersonaSignature(
                    source = Source(id = "OWNER", name = owner?.name ?: group.name, emoji = centerEmoji),
                    isPulsed = owner?.let { bubbleSenders.contains(it.id) || bubbleSenders.contains(it.persistentId) } ?: false,
                    isSelected = false,
                    size = 48.dp,
                    themeColor = themeColor,
                    onClick = { owner?.let { onSourceClick(it) } }
                )
            }

            val others = members.filter { it.id != group.ownerId && it.persistentId != group.ownerId }
            others.take(8).forEachIndexed { index, device ->
                val radius = 48f 
                val angle = (index.toDouble() / others.size.coerceAtLeast(1)) * 2 * PI
                val xOffset = (radius * cos(angle)).toFloat().dp
                val yOffset = (radius * sin(angle)).toFloat().dp

                Box(modifier = Modifier.offset(xOffset, yOffset)) {
                    PersonaSignature(
                        source = device,
                        isPulsed = bubbleSenders.contains(device.id) || bubbleSenders.contains(device.persistentId),
                        isSelected = false,
                        size = 32.dp,
                        themeColor = themeColor,
                        onClick = { onSourceClick(device) },
                        onLongClick = { onSourceLongClick(device) }
                    )
                }
            }
        }
    }
}
