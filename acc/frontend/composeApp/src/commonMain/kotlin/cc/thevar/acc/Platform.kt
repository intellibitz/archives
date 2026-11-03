package cc.thevar.acc

interface Platform {
    val name: String
    val defaultGatewayHost: String
}

expect fun getPlatform(): Platform
