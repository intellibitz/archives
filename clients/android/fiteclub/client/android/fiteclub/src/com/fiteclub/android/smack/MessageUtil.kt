package com.fiteclub.android.smack

import org.json.JSONException
import org.json.JSONObject

object MessageUtil {
    private const val TAG_MSG_TYPE = "msg_type"
    private const val TAG_USER_NAME = "user_name"
    private const val TAG_PLAYER2_NAME = "player2_name"
    private const val TAG_ROOM_NAME = "room_name"

    const val MSG_TYPE_WAITING_GAME = 1
    const val MSG_TYPE_ACCEPT_GAME = 2
    const val MSG_TYPE_START_GAME = 3

    fun createWatingFightMessage(userName: String?, gameRoomName: String?): String {
        val json = JSONObject()
        try {
            json.put(TAG_MSG_TYPE, MSG_TYPE_WAITING_GAME)
            json.put(TAG_USER_NAME, userName)
            json.put(TAG_ROOM_NAME, gameRoomName)
        } catch (e: JSONException) {
        }
        return json.toString()
    }

    fun getMessageType(json: JSONObject): Int {
        if (json.has(TAG_MSG_TYPE)) {
            try {
                return json.getInt(TAG_MSG_TYPE)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return 0
    }

    fun getUserName(json: JSONObject): String? {
        if (json.has(TAG_USER_NAME)) {
            try {
                return json.getString(TAG_USER_NAME)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun createAcceptFightMessage(
        userName: String?,
        player1: String?, gameRoomName: String?
    ): String {
        val json = JSONObject()
        try {
            json.put(TAG_MSG_TYPE, MSG_TYPE_ACCEPT_GAME)
            json.put(TAG_USER_NAME, player1)
            json.put(TAG_PLAYER2_NAME, userName)
            json.put(TAG_ROOM_NAME, gameRoomName)
        } catch (e: JSONException) {
            }
        return json.toString()
    }

    fun getPlayer2(json: JSONObject): String? {
        if (json.has(TAG_PLAYER2_NAME)) {
            try {
                return json.getString(TAG_PLAYER2_NAME)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getGameRoomName(json: JSONObject): String? {
        if (json.has(TAG_ROOM_NAME)) {
            try {
                return json.getString(TAG_ROOM_NAME)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun createStartFightMessage(
        player1: String?,
        player2: String?, gameRoomName: String?
    ): String {
        val json = JSONObject()
        try {
            json.put(TAG_MSG_TYPE, MSG_TYPE_START_GAME)
            json.put(TAG_USER_NAME, player1)
            json.put(TAG_PLAYER2_NAME, player2)
            json.put(TAG_ROOM_NAME, gameRoomName)
        } catch (e: JSONException) {
        }
        return json.toString()
    }
}
