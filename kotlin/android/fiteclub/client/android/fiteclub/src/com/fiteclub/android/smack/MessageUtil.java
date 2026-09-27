package com.fiteclub.android.smack;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageUtil {
	private static final String TAG_MSG_TYPE = "msg_type";
	private static final String TAG_USER_NAME = "user_name";
	private static final String TAG_PLAYER2_NAME = "player2_name";
	private static final String TAG_ROOM_NAME = "room_name";
	
	public static final int MSG_TYPE_WAITING_GAME = 1;
	public static final int MSG_TYPE_ACCEPT_GAME = 2;
	public static final int MSG_TYPE_START_GAME = 3;

	public static String createWatingFightMessage(String userName, String gameRoomName) 
	{
		JSONObject json = new JSONObject();
		try {
			json.put(TAG_MSG_TYPE, MSG_TYPE_WAITING_GAME);
			json.put(TAG_USER_NAME, userName);
			json.put(TAG_ROOM_NAME, gameRoomName);
		} catch (JSONException e) {
		}
		return json.toString();
	}

	public static int getMessageType(JSONObject json) 
	{
		if (json.has(TAG_MSG_TYPE))
		{
			try {
				return json.getInt(TAG_MSG_TYPE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static String getUserName(JSONObject json) 
	{
		if (json.has(TAG_USER_NAME))
		{
			try {
				return json.getString(TAG_USER_NAME);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String createAcceptFightMessage(String userName,
			String player1, String gameRoomName) 
	{
		JSONObject json = new JSONObject();
		try {
			json.put(TAG_MSG_TYPE, MSG_TYPE_ACCEPT_GAME);
			json.put(TAG_USER_NAME, player1);
			json.put(TAG_PLAYER2_NAME, userName);
			json.put(TAG_ROOM_NAME, gameRoomName);
		} catch (JSONException e) {
		}
		return json.toString();
	}

	public static String getPlayer2(JSONObject json) 
	{
		if (json.has(TAG_PLAYER2_NAME))
		{
			try {
				return json.getString(TAG_PLAYER2_NAME);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getGameRoomName(JSONObject json) 
	{
		if (json.has(TAG_ROOM_NAME))
		{
			try {
				return json.getString(TAG_ROOM_NAME);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String createStartFightMessage(String player1,
			String player2, String gameRoomName) 
	{
		JSONObject json = new JSONObject();
		try {
			json.put(TAG_MSG_TYPE, MSG_TYPE_START_GAME);
			json.put(TAG_USER_NAME, player1);
			json.put(TAG_PLAYER2_NAME, player2);
			json.put(TAG_ROOM_NAME, gameRoomName);
		} catch (JSONException e) {
		}
		return json.toString();
	}

}
