package com.fiteclub.android.smack

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class MatchService : Service() {
    companion object {
        private const val TAG = "MatchService"

        const val ACTION_MATCH_SERVICE = "com.fiteclub.match.ALL"
        const val EXTRA_MATCH_ERROR_MSG = "com.fiteclub.match.errmsg"
        const val EXTRA_GAME_START = "com.fiteclub.game.start"
        const val EXTRA_GAME_ROOMNAME = "com.fiteclub.game.roomname"
        const val EXTRA_GAME_PLAYER1 = "com.fiteclub.game.player1"
        const val EXTRA_GAME_PLAYER2 = "com.fiteclub.game.player2"
        const val PLAYER1_SIDE = 1
        const val PLAYER2_SIDE = 2
    }

    private var xmppManager: XmppManager? = null

    private var state = 0
    private var userName: String? = null
    private var broadcastSeconds: Int = 0
    private var listenSeconds: Int = 0
    private var gameRoomName: String? = null

    private var sendJsonText: String? = null
    private var maxSendCount: Int = 0
    private var sendCount: Int = 0
    private var sendTick: Int = 0

    override fun onCreate() {
        super.onCreate()
        xmppManager = XmppManager.getManager()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun broadcastErrorMsg(msg: String) {
        Log.d(TAG, "broadcastErrorMsg state=$state")

        val intent = Intent()
        intent.action = ACTION_MATCH_SERVICE
        intent.putExtra(EXTRA_MATCH_ERROR_MSG, msg)
        this.sendBroadcast(intent)
    }

    private fun broadcastStartGame(side: Int, player1: String?, player2: String?, roomName: String?) {
        Log.d(TAG, "broadcastStartGame state=$state")
        //player1 = "fiteclub.muthu@gmail.com";
        //player2 = "fiteclub.tu@gmail.com";

        val intent = Intent()
        intent.action = ACTION_MATCH_SERVICE
        intent.putExtra(EXTRA_GAME_START, side)
        intent.putExtra(EXTRA_GAME_ROOMNAME, roomName)
        intent.putExtra(EXTRA_GAME_PLAYER1, player1)
        intent.putExtra(EXTRA_GAME_PLAYER2, player2)
        this.sendBroadcast(intent)
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)

        Log.d(TAG, "onStart state=$state")
        if (state != 0) {
            broadcastErrorMsg("match service is busy!")
            return
        }
        state = 1 //connecting match room
        Thread {
            doMatch()
            state = 0 //back to init
        }.start()
    }

    private fun doMatch() {
        Log.d(TAG, "doMatch state=$state")

        if (!startMatch())
            return

        val tick1 = listenSeconds * 1000
        val tick2 = (listenSeconds + broadcastSeconds) * 1000
        var tick = 0
        sendCount = 0
        sendJsonText = null
        sendTick = 0
        while (tick < tick2) {
            when (state) {
                2 -> { //listening game request
                    if (tick > tick1)
                        state = 3
                }
                3 -> { //broadcast game request
                    if (tick > sendTick) {
                        if (sendJsonText == null)
                            sendJsonText = MessageUtil.createWatingFightMessage(userName, gameRoomName)
                        sendMessageToChatRoom()
                        sendTick = tick + 2000
                    }
                }
                5 -> { //accept the game
                    if (tick > sendTick) {
                        if (sendCount > 0) {
                            sendMessageToChatRoom()
                            sendCount--
                            sendTick = tick + 2000
                        } else {
                            sendJsonText = null
                            if (tick > tick1) {
                                state = 3
                            } else {
                                state = 2
                            }
                        }
                    }
                }
                6 -> { //start the game as player 1
                    if (tick > sendTick) {
                        if (sendCount > 0) {
                            sendMessageToChatRoom()
                            sendCount--
                            sendTick = tick + 2000
                        } else {
                            sendJsonText = null
                            state = 7
                        }
                    }
                }
                7, //done
                8 -> { //start the game as player 2
                    Log.d(TAG, "doMatch finishing start game state=$state")
                    xmppManager!!.leaveMatchRoom()
                    return
                }
                else -> {
                    broadcastErrorMsg("Interal state error!")
                    return
                }
            }

            val message = xmppManager!!.getNextMatchMessage()
            if (message != null) {
                processMessage(message)
                continue
            }
            tick += 300
        }
        if (state == 3) {
            state = 4 //time out
            xmppManager!!.leaveMatchRoom()
            broadcastErrorMsg("can not match a player!")
        }
    }

    private fun sendMessageToChatRoom() {
        Log.d(TAG, "sendMessageToChatRoom state=$state")
        Log.d(TAG, "sendMessageToChatRoom message=$sendJsonText")

        if (sendJsonText == null)
            return
        xmppManager!!.sendMatchMessage(sendJsonText)
    }

    private fun processMessage(message: String) {
        try {
            val json = JSONObject(message)
            Log.d(TAG, "processMessage state=$state")
            Log.d(TAG, "processMessage message=" + json.toString())
            val msgType = MessageUtil.getMessageType(json)
            when (msgType) {
                MessageUtil.MSG_TYPE_WAITING_GAME ->
                    acceptGame(json)
                MessageUtil.MSG_TYPE_ACCEPT_GAME ->
                    startGameAsPlayer1(json)
                MessageUtil.MSG_TYPE_START_GAME ->
                    startGameAsPlayer2(json)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun startGameAsPlayer2(json: JSONObject) {
        Log.d(TAG, "startGameAsPlayer2 state=$state")

        if (state != 5) //only process the packet when in "accept the game" state
            return
        val player2 = MessageUtil.getPlayer2(json)
        if (userName != player2)
            return
        val player1 = MessageUtil.getUserName(json)
        val roomName = MessageUtil.getGameRoomName(json)
        broadcastStartGame(PLAYER2_SIDE, player1, player2, roomName)
        state = 8 //start the game as player 2
    }

    private fun startGameAsPlayer1(json: JSONObject) {
        Log.d(TAG, "startGameAsPlayer1 state=$state")

        if (state != 3) //only process the packet when in "broadcast request" state
            return
        val player1 = MessageUtil.getUserName(json)
        if (userName != player1)
            return
        val roomName = MessageUtil.getGameRoomName(json)
        if (gameRoomName != roomName)
            return
        val player2 = MessageUtil.getPlayer2(json)
        sendJsonText = MessageUtil.createStartFightMessage(player1, player2, roomName)
        state = 6 //start the game as player 1
        sendCount = maxSendCount
        sendTick = 0
        broadcastStartGame(PLAYER1_SIDE, userName, player2, roomName)
    }

    private fun acceptGame(json: JSONObject) {
        Log.d(TAG, "acceptGame state=$state")

        if (state != 2) //only process the packet when in "listen" state
            return
        val player1 = MessageUtil.getUserName(json) ?: return
        val roomName = MessageUtil.getGameRoomName(json)
        sendJsonText = MessageUtil.createAcceptFightMessage(userName, player1, roomName)
        state = 5 //accept the game
        sendCount = maxSendCount
        sendTick = 0
    }

    private fun startMatch(): Boolean {
        if (!xmppManager!!.checkConnection()) {
            broadcastErrorMsg("Can not connect to xmpp server!")
            return false
        }
        if (!xmppManager!!.checkLogin()) {
            broadcastErrorMsg("can not login the xmpp server!")
            return false
        }
        if (!xmppManager!!.checkMatchRoom()) {
            broadcastErrorMsg("can not join the match room!")
            return false
        }

        userName = xmppManager!!.getUserName()
        gameRoomName = xmppManager!!.getPlanRoomName()
        broadcastSeconds = 50
        listenSeconds = 10
        maxSendCount = 5

        state = 2 //listening game request
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        xmppManager!!.clear()
    }
}
