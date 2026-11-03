package cc.thevar.acc

import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val defaultGatewayHost: String = window.location.hostname
}

actual fun getPlatform(): Platform = WasmPlatform()
