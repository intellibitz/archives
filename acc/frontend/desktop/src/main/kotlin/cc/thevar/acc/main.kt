package cc.thevar.acc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cc.thevar.acc.di.commonModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(commonModule())
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "acc",
        ) {
            App()
        }
    }
}
