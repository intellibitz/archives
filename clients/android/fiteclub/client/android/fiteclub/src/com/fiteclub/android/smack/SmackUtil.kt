package com.fiteclub.android.smack

import android.util.Log
import org.jivesoftware.smack.AccountManager
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.SmackConfiguration
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.PacketExtension
import org.jivesoftware.smack.provider.IQProvider
import org.jivesoftware.smack.provider.PacketExtensionProvider
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smackx.muc.MultiUserChat

object SmackUtil {
    private const val TAG = "SmackUtil"

    init {
        Log.d(TAG, "init start")
        try {
            initSmack(SmackUtil::class.java.classLoader!!)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        Log.d(TAG, "init end")
    }

    @Throws(ClassNotFoundException::class)
    private fun initSmack(classLoader: ClassLoader) {
        val classNames = arrayOf(
            "org.jivesoftware.smackx.ServiceDiscoveryManager",
            "org.jivesoftware.smack.PrivacyListManager",
            //"org.jivesoftware.smackx.XHTMLManager",
            "org.jivesoftware.smackx.muc.MultiUserChat",
            //"org.jivesoftware.smackx.filetransfer.FileTransferManager",
            //"org.jivesoftware.smackx.LastActivityManager",
            "org.jivesoftware.smack.ReconnectionManager"
        )
        for (className in classNames) {
            classLoader.loadClass(className)
        }

        initProviders(classLoader)
    }

    @Throws(ClassNotFoundException::class)
    private fun initProviders(classLoader: ClassLoader) {
        val providers = arrayOf(
            //{"iqProvider", "query", "jabber:iq:private", "org.jivesoftware.smackx.PrivateDataManager$PrivateDataIQProvider"},
            //{"iqProvider", "query", "jabber:iq:time", "org.jivesoftware.smackx.packet.Time"},
            //{"extensionProvider", "x", "jabber:x:roster", "org.jivesoftware.smackx.provider.RosterExchangeProvider"},
            //{"extensionProvider", "x", "jabber:x:event", "org.jivesoftware.smackx.provider.MessageEventProvider"},
            //{"extensionProvider", "active", "http://jabber.org/protocol/chatstates", "org.jivesoftware.smackx.packet.ChatStateExtension$Provider"},
            //{"extensionProvider", "composing", "http://jabber.org/protocol/chatstates", "org.jivesoftware.smackx.packet.ChatStateExtension$Provider"},
            //{"extensionProvider", "paused", "http://jabber.org/protocol/chatstates", "org.jivesoftware.smackx.packet.ChatStateExtension$Provider"},
            //{"extensionProvider", "inactive", "http://jabber.org/protocol/chatstates", "org.jivesoftware.smackx.packet.ChatStateExtension$Provider"},
            //{"extensionProvider", "gone", "http://jabber.org/protocol/chatstates", "org.jivesoftware.smackx.packet.ChatStateExtension$Provider"},
            //{"extensionProvider", "html", "http://jabber.org/protocol/xhtml-im", "org.jivesoftware.smackx.provider.XHTMLExtensionProvider"},
            //{"extensionProvider", "x", "jabber:x:conference", "org.jivesoftware.smackx.GroupChatInvitation$Provider"},
            //{"iqProvider", "query", "http://jabber.org/protocol/disco#items", "org.jivesoftware.smackx.provider.DiscoverItemsProvider"},
            //{"iqProvider", "query", "http://jabber.org/protocol/disco#info", "org.jivesoftware.smackx.provider.DiscoverInfoProvider"},
            //{"extensionProvider", "x", "jabber:x:data", "org.jivesoftware.smackx.provider.DataFormProvider"},
            arrayOf("extensionProvider", "x", "http://jabber.org/protocol/muc#user", "org.jivesoftware.smackx.provider.MUCUserProvider"),
            arrayOf("iqProvider", "query", "http://jabber.org/protocol/muc#admin", "org.jivesoftware.smackx.provider.MUCAdminProvider"),
            arrayOf("iqProvider", "query", "http://jabber.org/protocol/muc#owner", "org.jivesoftware.smackx.provider.MUCOwnerProvider")
            //{"extensionProvider", "x", "jabber:x:delay", "org.jivesoftware.smackx.provider.DelayInformationProvider"},
            //{"iqProvider", "query", "jabber:iq:version", "org.jivesoftware.smackx.packet.Version"},
            //{"iqProvider", "vCard", "vcard-temp", "org.jivesoftware.smackx.provider.VCardProvider"},
            //{"iqProvider", "offline", "http://jabber.org/protocol/offline", "org.jivesoftware.smackx.packet.OfflineMessageRequest$Provider"},
            //{"extensionProvider", "offline", "http://jabber.org/protocol/offline", "org.jivesoftware.smackx.packet.OfflineMessageInfo$Provider"},
            //{"iqProvider", "query", "jabber:iq:last", "org.jivesoftware.smackx.packet.LastActivity$Provider"},
            //{"iqProvider", "query", "jabber:iq:search", "org.jivesoftware.smackx.search.UserSearch$Provider"},
            //{"iqProvider", "sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", "org.jivesoftware.smackx.packet.SharedGroupsInfo$Provider"},
            //{"extensionProvider", "addresses", "http://jabber.org/protocol/address", "org.jivesoftware.smackx.provider.MultipleAddressesProvider"},
            //{"iqProvider", "si", "http://jabber.org/protocol/si", "org.jivesoftware.smackx.provider.StreamInitiationProvider"},
            //{"iqProvider", "query", "http://jabber.org/protocol/bytestreams", "org.jivesoftware.smackx.provider.BytestreamsProvider"},
            //{"iqProvider", "open", "http://jabber.org/protocol/ibb", "org.jivesoftware.smackx.provider.IBBProviders$Open"},
            //{"iqProvider", "close", "http://jabber.org/protocol/ibb", "org.jivesoftware.smackx.provider.IBBProviders$Close"},
            //{"extensionProvider", "data", "http://jabber.org/protocol/ibb", "org.jivesoftware.smackx.provider.IBBProviders$Data"},
            //{"iqProvider", "query", "jabber:iq:privacy", "org.jivesoftware.smack.provider.PrivacyProvider"}
        )

        val manager = ProviderManager.getInstance()
        for (info in providers) {
            val tag = info[0]
            val elementName = info[1]
            val namespace = info[2]
            val className = info[3]

            val provider = classLoader.loadClass(className)
            if (tag == "iqProvider") {
                if (IQProvider::class.java.isAssignableFrom(provider)) {
                    try {
                        manager.addIQProvider(elementName, namespace, provider.newInstance())
                    } catch (e: InstantiationException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                } else if (IQ::class.java.isAssignableFrom(provider)) {
                    manager.addIQProvider(elementName, namespace, provider)
                }
            } else if (tag == "extensionProvider") {
                if (PacketExtensionProvider::class.java.isAssignableFrom(provider)) {
                    try {
                        manager.addExtensionProvider(elementName, namespace, provider.newInstance())
                    } catch (e: InstantiationException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                } else if (PacketExtension::class.java.isAssignableFrom(provider)) {
                    manager.addExtensionProvider(elementName, namespace, provider)
                }
            }
        }
    }

    fun getConnection(
        connIn: XMPPConnection?, host: String?, port: Int,
        serviceName: String?, timeout: Int
    ): XMPPConnection? {
        var conn = connIn
        Log.d(TAG, "connect start")
        for (i in 0 until 3) { //retry count=3
            if (conn == null) {
                val config = ConnectionConfiguration(
                    host, port, serviceName
                )
                config.isDebuggerEnabled = true
                config.isReconnectionAllowed = true
                SmackConfiguration.setPacketReplyTimeout(timeout * 1000)
                conn = XMPPConnection(config)
            }
            try {
                conn!!.connect()
                Log.d(TAG, "connect success")
                return conn
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                Thread.sleep(1500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        Log.d(TAG, "connect fail")
        return null
    }

    fun doLogin(conn: XMPPConnection, loginName: String?, password: String?): Boolean {
        if (conn.isAuthenticated)
            return true
        for (i in 0 until 3) { //retry count=3
            try {
                if (!conn.isConnected)
                    conn.connect()
                Log.d(TAG, "login start: $loginName")
                conn.login(loginName, password)
                Log.d(TAG, "login success")
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                if (conn.isConnected)
                    conn.disconnect() //must disconnect, or login failed always coz last login failure change the internal state
                conn.connect()
                val manager = conn.accountManager
                Log.d(TAG, "create account start")
                manager.createAccount(loginName, password)
                Log.d(TAG, "create account end")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Log.d(TAG, "login fail")
        return false
    }

    fun joinChatRoom(conn: XMPPConnection, roomName: String?, userName: String?): MultiUserChat? {
        Log.d(TAG, "open chat room start: $roomName")
        val muc = MultiUserChat(conn, roomName)
        for (i in 0 until 3) { //retry count=3
            try {
                muc.join(userName)
                if (muc.isJoined) {
                    Log.d(TAG, "open chat room success")
                    return muc
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Log.d(TAG, "open chat room fail")
        return null
    }

    fun sendMessage(muc: MultiUserChat, message: String?): Boolean {
        for (i in 0 until 3) { //retry count=3
            try {
                muc.sendMessage(message)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}
