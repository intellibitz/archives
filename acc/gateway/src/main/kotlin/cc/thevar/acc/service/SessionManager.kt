package cc.thevar.acc.service

import io.ktor.server.websocket.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager {
    val uiSessions: MutableSet<DefaultWebSocketServerSession> = Collections.newSetFromMap(ConcurrentHashMap<DefaultWebSocketServerSession, Boolean>())
    val systemSessions: MutableSet<DefaultWebSocketServerSession> = Collections.newSetFromMap(ConcurrentHashMap<DefaultWebSocketServerSession, Boolean>())
}
