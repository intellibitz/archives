package cc.thevar.blukit.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

class WearActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlukitWearApp()
        }
    }
}

@Composable
fun BlukitWearApp() {
    MaterialTheme {
        ScreenScaffold {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    ListHeader {
                        Text("SPREAD PULSES")
                    }
                }
                item {
                    Text("Nearby Pulses will appear here.")
                }
            }
        }
    }
}
