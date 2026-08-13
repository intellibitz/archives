package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthSurface
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<ContactEntity>,
    onStartChat: (ContactEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            // Space for global badge
            Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "The air is still. Find a vibe in the field to form a tie.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StealthPrimary.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contacts, key = { it.contactId }) { contact ->
                    TieItem(
                        contact = contact,
                        onStartChat = { onStartChat(contact) }
                    )
                }
            }
        }
    }
}

@Composable
fun TieItem(
    contact: ContactEntity,
    onStartChat: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Vibe")
    val vibeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VibeAlpha"
    )

    Surface(
        onClick = onStartChat,
        shape = RoundedCornerShape(4.dp),
        color = StealthSurface.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(StealthPrimary, Color.Transparent),
                    startX = 0f,
                    endX = 200f
                ),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cyber Cyan left-edge is handled by the border and the layout below
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(StealthPrimary)
            )
            
            Spacer(modifier = Modifier.width(16.dp))

            // Avatar with Glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .blur(8.dp)
                        .background(StealthPrimary.copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StealthSurface)
                        .border(1.dp, StealthPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.avatarUri ?: "👤",
                        fontSize = 24.sp
                    )
                }
                
                // Signal Strength Indicator
                Canvas(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    drawCircle(
                        color = StealthPrimary.copy(alpha = vibeAlpha),
                        radius = size.minDimension / 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                val lastSeenDate = remember(contact.lastSeen) {
                    val date = Date(contact.lastSeen)
                    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    format.format(date)
                }
                Text(
                    text = "LAST SYNC: $lastSeenDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }

            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack, // Using back as a "chevron" variant or just a simple arrow
                contentDescription = null,
                tint = StealthPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp).rotate(180f)
            )
        }
    }
}
