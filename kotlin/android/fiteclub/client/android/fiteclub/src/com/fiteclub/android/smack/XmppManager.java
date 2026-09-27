package com.fiteclub.android.smack;

import java.util.Random;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class XmppManager {
	private static XmppManager manager;

	private XMPPConnection connection;
	private String xmppServer;
	private String xmppServiceName;
	private int port;
	private int timeout;
	private String loginName;
	private String password;
	private String matchRoomName;
	private String userName;
	private String fullMatchUserName;
	private String planRoomName;
	private MultiUserChat mucMatch;
	
	private XmppManager()
	{
		
	}
	
	public static XmppManager getManager()
	{
		if (manager == null)
			manager = new XmppManager();
		return manager;
	}

	public boolean checkConnection()
	{
		if ((connection == null) || (!connection.isConnected()))
		{
			initConnectionInfo();
			connection = SmackUtil.getConnection(connection, xmppServer, port, xmppServiceName, timeout);
		}
		if ((connection == null) || (!connection.isConnected()))
		{
			disconnect();
			return false;
		}
		return true;
	}
	
	private void disconnect() 
	{
		if ((connection != null) && (connection.isConnected()))
		{
			connection.disconnect();
		}
		connection = null;
	}

	private void initConnectionInfo() 
	{
		xmppServer = "xmpp.fiteclub.net";
		port = 5222;
		xmppServiceName = xmppServer;
		timeout = 15;
		
		int rnd = new Random(System.currentTimeMillis()).nextInt(1000);
		loginName = "fiteclub.player." + rnd;
		password = "fite2009";
		matchRoomName = "fyteclub.room@conference.xmpp.fiteclub.net";	//should be all lowercase
		userName = "player." + rnd;
		fullMatchUserName = matchRoomName + '/' + userName;
		planRoomName = "fyteclub.game." + rnd +  "@conference.xmpp.fiteclub.net";
	}

	public boolean checkLogin() 
	{
		if ((connection == null) || (!connection.isConnected()))
			return false;
		if (!connection.isAuthenticated())
			SmackUtil.doLogin(connection, loginName, password);
		if (!connection.isAuthenticated())
		{
			disconnect();
			return false;
		}
		return true;
	}
	
	public boolean checkMatchRoom()
	{
		if ((connection == null) || (!connection.isAuthenticated()))
			return false;
		if ((mucMatch == null) || (!mucMatch.isJoined()))
			mucMatch = SmackUtil.joinChatRoom(connection, matchRoomName, userName);
		if ((mucMatch == null) || (!mucMatch.isJoined()))
		{
			disconnect();
			return false;
		}
		return true;
	}
	
	public void clear()
	{
		
		leaveMatchRoom();
		disconnect();
	}

	public void leaveMatchRoom() 
	{
		if ((mucMatch != null) && (mucMatch.isJoined()))
		{
			mucMatch.leave();
		}
		mucMatch = null;
	}

	public String getNextMatchMessage() 
	{
		if ((mucMatch == null) || (!mucMatch.isJoined()))
			return null;
		while (true)
		{
			Message message = mucMatch.nextMessage(300);
			if (message == null)
				return null;
			if (message.getFrom().equals(fullMatchUserName))
				continue;
			return message.getBody();
		}
	}

	public void sendMatchMessage(String sendJsonText) 
	{
		if ((mucMatch != null) && (mucMatch.isJoined()))
			SmackUtil.sendMessage(mucMatch, sendJsonText);
	}

	public String getUserName() 
	{
		return userName;
	}
	
	public String getPlanRoomName()
	{
		return planRoomName;
	}

	public MultiUserChat joinGameRoom(String roomName, String userName) 
	{
		if ((connection == null) || (!connection.isAuthenticated()))
			return null;
		return SmackUtil.joinChatRoom(connection, roomName, userName);	
	}

	public MultiUserChat openGameRoom(String roomName, final MessageListener listener) 
	{
		if (!checkConnection())
			return null;
		if (!checkLogin())
			return null;
		MultiUserChat muc = SmackUtil.joinChatRoom(connection, roomName, userName);
		final String fullName = roomName + '/' + userName;
		muc.addMessageListener(new PacketListener() {

			public void processPacket(Packet packet) 
			{
				if (fullName.equals(packet.getFrom()))
					return;
				if (packet instanceof Message)
				{
					String message = ((Message)packet).getBody();
					listener.processMessage(message);
				}
			}
			
		});
		return muc;
	}
}
