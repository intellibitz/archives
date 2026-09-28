package com.fiteclub.android.smack

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.fiteclub.android.R
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smackx.muc.MultiUserChat

class GameTestActivity : Activity() {
    companion object {
        private const val TAG = "GameTestActivity"
    }

    private var xmppManager: XmppManager? = null
    private var userName: String? = null
    private var muc: MultiUserChat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        this.title = "Game Test"

        Log.d(TAG, "onCreate")

        val intent = this.intent
        if (intent.hasExtra(MatchService.EXTRA_GAME_START)) {
            val player1 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER1)
            val player2 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER2)
            val roomName = intent.getStringExtra(MatchService.EXTRA_GAME_ROOMNAME)
            val side = intent.getIntExtra(MatchService.EXTRA_GAME_START, 0)
            startGame(side, player1, player2, roomName)
        }
        Log.d(TAG, "onCreate end")
    }

    private fun startGame(
        side: Int, player1: String?, player2: String?,
        roomName: String?
    ) {
        Log.d(TAG, "startGame")
        Log.d(TAG, "side: $side")
        Log.d(TAG, "player1: $player1")
        Log.d(TAG, "player2: $player2")
        Log.d(TAG, "roomName: $roomName")

        if (side == MatchService.PLAYER1_SIDE) {
            userName = player1
        } else {
            userName = player2
        }

        xmppManager = XmppManager.getManager()

        muc = xmppManager!!.openGameRoom(roomName, object : MessageListener {
            override fun processMessage(message: String?) {
                Log.d(TAG, "Got a new message: $message")
            }
        })
        if (muc == null) {
            Toast.makeText(this, "Can not open game room!", Toast.LENGTH_LONG).show()
            return
        }
        object : Thread() {
            override fun run() {
                playGame()
            }
        }.start()
    }

    private fun playGame() {
        for (i in 0 until 100) {
            if (muc == null)
                return
            try {
                muc!!.sendMessage("Message from $userName id=$i")
            } catch (e: XMPPException) {
                e.printStackTrace()
            }
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (xmppManager != null) {
            if ((muc != null) && (muc!!.isJoined))
                muc!!.leave()
            muc = null
            xmppManager!!.clear()
            xmppManager = null
        }
    }
}
