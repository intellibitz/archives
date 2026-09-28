package com.fiteclub.android.smack

import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smackx.muc.MultiUserChat
import java.util.Random

class XmppManager private constructor() {
    companion object {
        @Volatile
        private var manager: XmppManager? = null

        @JvmStatic
        fun getManager(): XmppManager {
            if (manager == null)
                manager = XmppManager()
            return manager!!
        }
    }

    private var connection: XMPPConnection? = null
    private var xmppServer: String? = null
    private var xmppServiceName: String? = null
    private var port: Int = 0
    private var timeout: Int = 0
    private var loginName: String? = null
    private var password: String? = null
    private var matchRoomName: String? = null
    private var userName: String? = null
    private var fullMatchUserName: String? = null
    private var planRoomName: String? = null
    private var mucMatch: MultiUserChat? = null

    fun checkConnection(): Boolean {
        if ((connection == null) || (!connection!!.isConnected)) {
            initConnectionInfo()
            connection = SmackUtil.getConnection(connection, xmppServer, port, xmppServiceName, timeout)
        }
        if ((connection == null) || (!connection!!.isConnected)) {
            disconnect()
            return false
        }
        return true
    }

    private fun disconnect() {
        if ((connection != null) && (connection!!.isConnected)) {
            connection!!.disconnect()
        }
        connection = null
    }

    private fun initConnectionInfo() {
        xmppServer = "xmpp.fiteclub.net"
        port = 5222
        xmppServiceName = xmppServer
        timeout = 15

        val rnd = Random(System.currentTimeMillis()).nextInt(1000)
        loginName = "fiteclub.player.$rnd"
        password = "fite2009"
        matchRoomName = "fyteclub.room@conference.xmpp.fiteclub.net" //should be all lowercase
        userName = "player.$rnd"
        fullMatchUserName = "$matchRoomName/$userName"
        planRoomName = "fyteclub.game.$rnd@conference.xmpp.fiteclub.net"
    }

    fun checkLogin(): Boolean {
        if ((connection == null) || (!connection!!.isConnected))
            return false
        if (!connection!!.isAuthenticated)
            SmackUtil.doLogin(connection!!, loginName, password)
        if (!connection!!.isAuthenticated) {
            disconnect()
            return false
        }
        return true
    }

    fun checkMatchRoom(): Boolean {
        if ((connection == null) || (!connection!!.isAuthenticated))
            return false
        if ((mucMatch == null) || (!mucMatch!!.isJoined))
            mucMatch = SmackUtil.joinChatRoom(connection!!, matchRoomName, userName)
        if ((mucMatch == null) || (!mucMatch!!.isJoined)) {
            disconnect()
            return false
        }
        return true
    }

    fun clear() {
        leaveMatchRoom()
        disconnect()
    }

    fun leaveMatchRoom() {
        if ((mucMatch != null) && (mucMatch!!.isJoined)) {
            mucMatch!!.leave()
        }
        mucMatch = null
    }

    fun getNextMatchMessage(): String? {
        if ((mucMatch == null) || (!mucMatch!!.isJoined))
            return null
        while (true) {
            val message = mucMatch!!.nextMessage(300) ?: return null
            if (message.from == fullMatchUserName)
                continue
            return message.body
        }
    }

    fun sendMatchMessage(sendJsonText: String?) {
        if ((mucMatch != null) && (mucMatch!!.isJoined))
            SmackUtil.sendMessage(mucMatch!!, sendJsonText)
    }

    fun getUserName(): String? {
        return userName
    }

    fun getPlanRoomName(): String? {
        return planRoomName
    }

    fun joinGameRoom(roomName: String?, userName: String?): MultiUserChat? {
        if ((connection == null) || (!connection!!.isAuthenticated))
            return null
        return SmackUtil.joinChatRoom(connection!!, roomName, userName)
    }

    fun openGameRoom(roomName: String?, listener: MessageListener): MultiUserChat? {
        if (!checkConnection())
            return null
        if (!checkLogin())
            return null
        val muc = SmackUtil.joinChatRoom(connection!!, roomName, userName)
        val fullName = "$roomName/$userName"
        muc!!.addMessageListener(object : PacketListener {
            override fun processPacket(packet: Packet) {
                if (fullName == packet.from)
                    return
                if (packet is Message) {
                    val message = packet.body
                    listener.processMessage(message)
                }
            }
        })
        return muc
    }
}
