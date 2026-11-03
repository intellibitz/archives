package cc.thevar.acc

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val defaultGatewayHost: String = "10.0.2.2" // Emulator default
}

actual fun getPlatform(): Platform = AndroidPlatform()
