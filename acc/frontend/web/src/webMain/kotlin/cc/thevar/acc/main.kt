package cc.thevar.acc

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cc.thevar.acc.di.commonModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(commonModule())
    }
    ComposeViewport {
        App()
    }
}
