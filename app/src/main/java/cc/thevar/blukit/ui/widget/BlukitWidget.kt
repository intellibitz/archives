package cc.thevar.blukit.ui.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cc.thevar.blukit.MainActivity
import cc.thevar.blukit.BlukitApplication

class BlukitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val app = context.applicationContext as BlukitApplication
            val vibeStore = app.vibeStore
            val messages by vibeStore.messages.collectAsState(initial = emptyList())
            val count = messages.size

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPREAD VIBES",
                        style = TextStyle(
                            color = ColorProvider(Color.Cyan),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "$count VIBES AROUND",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Button(
                        text = "OPEN BLUKIT",
                        onClick = actionStartActivity<MainActivity>(),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

class BlukitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BlukitWidget()
}
