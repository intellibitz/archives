package com.fiteclub.android.smack;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

public class SmackUtil {
	private static final String TAG = "SmackUtil";
	
	static 
	{
		Log.d(TAG, "init start");
		try {
			initSmack(SmackUtil.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "init end");
	}

	private static void initSmack(ClassLoader classLoader) throws ClassNotFoundException 
	{
		String[] classNames = {
		        "org.jivesoftware.smackx.ServiceDiscoveryManager",
		        "org.jivesoftware.smack.PrivacyListManager",  
		        //"org.jivesoftware.smackx.XHTMLManager",
		        "org.jivesoftware.smackx.muc.MultiUserChat",
		        //"org.jivesoftware.smackx.filetransfer.FileTransferManager",
		        //"org.jivesoftware.smackx.LastActivityManager",
		        "org.jivesoftware.smack.ReconnectionManager"   
		};
		for (String className : classNames)
		{
			classLoader.loadClass(className);
		}
		
		initProviders(classLoader);
	}

	private static void initProviders(ClassLoader classLoader) throws ClassNotFoundException 
	{
		String[][] providers = {
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
				{"extensionProvider", "x", "http://jabber.org/protocol/muc#user", "org.jivesoftware.smackx.provider.MUCUserProvider"},
				{"iqProvider", "query", "http://jabber.org/protocol/muc#admin", "org.jivesoftware.smackx.provider.MUCAdminProvider"},
				{"iqProvider", "query", "http://jabber.org/protocol/muc#owner", "org.jivesoftware.smackx.provider.MUCOwnerProvider"},
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
		};
		
		ProviderManager manager = ProviderManager.getInstance();
		for (String[] info : providers)
		{
			String tag = info[0];
			String elementName = info[1];
			String namespace = info[2];
			String className = info[3];
			
			Class<?> provider = classLoader.loadClass(className);
			if (tag.equals("iqProvider")) {
                if (IQProvider.class.isAssignableFrom(provider)) {
                	try {
						manager.addIQProvider(elementName, namespace, provider.newInstance());
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
                }
                else if (IQ.class.isAssignableFrom(provider)) {
                	manager.addIQProvider(elementName, namespace, provider);
                }
			} else if (tag.equals("extensionProvider")) {
                if (PacketExtensionProvider.class.isAssignableFrom(provider)) {
                	try {
						manager.addExtensionProvider(elementName, namespace, provider.newInstance());
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
                }
                else if (PacketExtension.class.isAssignableFrom(provider)) {
                	manager.addExtensionProvider(elementName, namespace, provider);
                }
			}
		}
	}

	public static XMPPConnection getConnection(XMPPConnection conn, String host, int port, 
			String serviceName, int timeout) 
	{
		Log.d(TAG, "connect start");
		for (int i=0; i<3; i++)	//retry count=3
		{
			if (conn == null)
			{
				ConnectionConfiguration config = new ConnectionConfiguration(
					host, port, serviceName);
				config.setDebuggerEnabled(true);
				config.setReconnectionAllowed(true);
				SmackConfiguration.setPacketReplyTimeout(timeout*1000);
				conn = new XMPPConnection(config);
			}
			try {
				conn.connect();
				Log.d(TAG, "connect success");
				return conn;
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "connect fail");
		return null;
	}

	public static boolean doLogin(XMPPConnection conn, String loginName, String password) 
	{
		if (conn.isAuthenticated())
			return true;
		for (int i=0; i<3; i++)	//retry count=3
		{
			try {
				if (!conn.isConnected())
					conn.connect();
				Log.d(TAG, "login start: " + loginName);
				conn.login(loginName, password);
				Log.d(TAG, "login success");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (conn.isConnected())
					conn.disconnect();	//must disconnect, or login failed always coz last login failure change the internal state
				conn.connect();
				AccountManager manager = conn.getAccountManager();
				Log.d(TAG, "create account start");
				manager.createAccount(loginName, password);
				Log.d(TAG, "create account end");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Log.d(TAG, "login fail");
		return false;
	}

	public static MultiUserChat joinChatRoom(XMPPConnection conn, String roomName, String userName) 
	{
		Log.d(TAG, "open chat room start: " + roomName);
		MultiUserChat muc = new MultiUserChat(conn, roomName);
		for (int i=0; i<3; i++)	//retry count=3
		{
			try {
				muc.join(userName);
				if (muc.isJoined())
				{
					Log.d(TAG, "open chat room success");
					return muc;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "open chat room fail");
		return null;
	}

	public static boolean sendMessage(MultiUserChat muc, String message) 
	{
		for (int i=0; i<3; i++)	//retry count=3
		{
			try {
				muc.sendMessage(message);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
