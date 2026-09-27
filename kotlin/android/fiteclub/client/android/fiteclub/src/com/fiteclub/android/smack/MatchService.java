package com.fiteclub.android.smack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MatchService extends Service {
	private static final String TAG = "MatchService";

	public static final String ACTION_MATCH_SERVICE = "com.fiteclub.match.ALL";
	public static final String EXTRA_MATCH_ERROR_MSG = "com.fiteclub.match.errmsg";
	public static final String EXTRA_GAME_START = "com.fiteclub.game.start";
	public static final String EXTRA_GAME_ROOMNAME = "com.fiteclub.game.roomname";
	public static final String EXTRA_GAME_PLAYER1 = "com.fiteclub.game.player1";
	public static final String EXTRA_GAME_PLAYER2 = "com.fiteclub.game.player2";
	public static final int PLAYER1_SIDE = 1;
	public static final int PLAYER2_SIDE = 2;
	
	private XmppManager xmppManager;
	
	private int state = 0;
	private String userName;
	private int broadcastSeconds;
	private int listenSeconds;
	private String gameRoomName;
	
	private String sendJsonText;
	private int maxSendCount;
	private int sendCount;
	private int sendTick;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		xmppManager = XmppManager.getManager();
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
        return null;
	}
		
	private void broadcastErrorMsg(String msg) 
	{
		Log.d(TAG, "broadcastErrorMsg state=" + state);

		Intent intent = new Intent();
		intent.setAction(ACTION_MATCH_SERVICE);
		intent.putExtra(EXTRA_MATCH_ERROR_MSG, msg);
		this.sendBroadcast(intent);
	}

	private void broadcastStartGame(int side, String player1, String player2, String roomName) 
	{
		Log.d(TAG, "broadcastStartGame state=" + state);
		//player1 = "fiteclub.muthu@gmail.com";
		//player2 = "fiteclub.tu@gmail.com";

		Intent intent = new Intent();
		intent.setAction(ACTION_MATCH_SERVICE);
		intent.putExtra(EXTRA_GAME_START, side);
		intent.putExtra(EXTRA_GAME_ROOMNAME, roomName);
		intent.putExtra(EXTRA_GAME_PLAYER1, player1);
		intent.putExtra(EXTRA_GAME_PLAYER2, player2);
		this.sendBroadcast(intent);
	}

	@Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);

		Log.d(TAG, "onStart state=" + state);
		if (state != 0)
		{
			broadcastErrorMsg("match service is busy!");
			return;
		}
		state = 1;	//connecting match room
		new Thread(new Runnable() {
			public void run() 
			{
				doMatch();
				state = 0;	//back to init
			}
		}).start();
	}

	private void doMatch() 
	{
		Log.d(TAG, "doMatch state=" + state);
		
		if (!startMatch())
			return;

		int tick1 =  listenSeconds * 1000;
		int tick2 =  (listenSeconds + broadcastSeconds) * 1000;
		int tick = 0;
		sendCount = 0;
		sendJsonText = null;
		sendTick = 0;
		while (tick < tick2)
		{
			switch (state)
			{
				case 2: //listening game request
					if (tick > tick1)
						state = 3;
					break;
				case 3:	//broadcast game request
					if (tick > sendTick)
					{
						if (sendJsonText == null)
							sendJsonText = MessageUtil.createWatingFightMessage(userName, gameRoomName);
						sendMessageToChatRoom();
						sendTick = tick + 2000;
					}
					break;
				case 5: //accept the game
					if (tick > sendTick)
					{
						if (sendCount > 0) {
							sendMessageToChatRoom();
							sendCount--;
							sendTick = tick + 2000;
						} else {
							sendJsonText = null;
							if (tick > tick1) {
								state = 3;
							} else {
								state = 2;
							}
						}
					}
					break;
				case 6: //start the game as player 1
					if (tick > sendTick)
					{
						if (sendCount > 0) {
							sendMessageToChatRoom();
							sendCount--;
							sendTick = tick + 2000;
						} else {
							sendJsonText = null;
							state = 7;
						}
					}
					break;
				case 7: //done
				case 8: //start the game as player 2
					Log.d(TAG, "doMatch finishing start game state=" + state);
					xmppManager.leaveMatchRoom();
					return;
				default:
					broadcastErrorMsg("Interal state error!");
					return;
			}

			String message = xmppManager.getNextMatchMessage(); 
			if (message != null)
			{
				processMessage(message);
				continue;
			}
			tick += 300;
		}
		if (state == 3)
		{
			state = 4;	//time out
			xmppManager.leaveMatchRoom();
			broadcastErrorMsg("can not match a player!");
		}
	}

	private void sendMessageToChatRoom() 
	{
		Log.d(TAG, "sendMessageToChatRoom state=" + state);
		Log.d(TAG, "sendMessageToChatRoom message=" + sendJsonText);
		
		if (sendJsonText == null)
			return;
		xmppManager.sendMatchMessage(sendJsonText);
	}

	private void processMessage(String message) 
	{
		try {
			JSONObject json = new JSONObject(message);
			Log.d(TAG, "processMessage state=" + state);
			Log.d(TAG, "processMessage message=" + json.toString());
			int msgType = MessageUtil.getMessageType(json);
			switch (msgType)
			{
				case MessageUtil.MSG_TYPE_WAITING_GAME:
					acceptGame(json);
					break;
				case MessageUtil.MSG_TYPE_ACCEPT_GAME:
					startGameAsPlayer1(json);
					break;
				case MessageUtil.MSG_TYPE_START_GAME:
					startGameAsPlayer2(json);
					break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void startGameAsPlayer2(JSONObject json) 
	{
		Log.d(TAG, "startGameAsPlayer2 state=" + state);

		if (state != 5)	//only process the packet when in "accept the game" state
			return;
		String player2 = MessageUtil.getPlayer2(json);
		if (!userName.equals(player2))
			return;
		String player1 = MessageUtil.getUserName(json);
		String roomName = MessageUtil.getGameRoomName(json);
		broadcastStartGame(PLAYER2_SIDE, player1, player2, roomName);
		state = 8;	//start the game as player 2
	}

	private void startGameAsPlayer1(JSONObject json) 
	{
		Log.d(TAG, "startGameAsPlayer1 state=" + state);

		if (state != 3) //only process the packet when in "broadcast request" state
			return;
		String player1 = MessageUtil.getUserName(json);
		if (!userName.equals(player1))
			return;
		String roomName = MessageUtil.getGameRoomName(json);
		if (!gameRoomName.equals(roomName))
			return;
		String player2 = MessageUtil.getPlayer2(json);
		sendJsonText = MessageUtil.createStartFightMessage(player1, player2, roomName);
	    state = 6;	//start the game as player 1
	    sendCount = maxSendCount;
	    sendTick = 0;
		broadcastStartGame(PLAYER1_SIDE, userName, player2, roomName);
	}

	private void acceptGame(JSONObject json) 
	{
		Log.d(TAG, "acceptGame state=" + state);

		if (state != 2) //only process the packet when in "listen" state
			return;
		String player1 = MessageUtil.getUserName(json);
		if (player1 == null)
			return;
		String roomName = MessageUtil.getGameRoomName(json);
		sendJsonText = MessageUtil.createAcceptFightMessage(userName, player1, roomName);
	    state = 5;	//accept the game 
	    sendCount = maxSendCount;
	    sendTick = 0;
	}

	private boolean startMatch() 
	{
		if (!xmppManager.checkConnection())
		{
			broadcastErrorMsg("Can not connect to xmpp server!");
			return false;
		}
		if (!xmppManager.checkLogin())
		{
			broadcastErrorMsg("can not login the xmpp server!");
			return false;
		}
		if (!xmppManager.checkMatchRoom())
		{
			broadcastErrorMsg("can not join the match room!");
			return false;
		}
		
		userName = xmppManager.getUserName();
		gameRoomName = xmppManager.getPlanRoomName();
		broadcastSeconds = 50;
		listenSeconds = 10;
		maxSendCount = 5;

		state = 2;	//listening game request
		return true;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		xmppManager.clear();
	}
}
