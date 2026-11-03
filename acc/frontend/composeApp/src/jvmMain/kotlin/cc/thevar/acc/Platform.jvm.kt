package cc.thevar.acc

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val defaultGatewayHost: String = "localhost"
}

actual fun getPlatform(): Platform = JVMPlatform()
