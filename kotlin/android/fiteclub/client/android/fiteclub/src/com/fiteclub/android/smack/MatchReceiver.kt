package com.fiteclub.android.smack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MatchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (MatchService.ACTION_MATCH_SERVICE != action)
            return
        var msg: String? = null
        if (intent.hasExtra(MatchService.EXTRA_MATCH_ERROR_MSG)) {
            msg = "Error: " + intent.getStringExtra(MatchService.EXTRA_MATCH_ERROR_MSG)
        } else if (intent.hasExtra(MatchService.EXTRA_GAME_START)) {
            val player1 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER1)
            val player2 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER2)
            val roomName = intent.getStringExtra(MatchService.EXTRA_GAME_ROOMNAME)
            val side = intent.getIntExtra(MatchService.EXTRA_GAME_START, 0)
            startGame(context, side, player1, player2, roomName)
            msg = "player1: " + player1 +
                "\n" + "player2: " + player2 +
                "\n" + "room: " + roomName
        }
        if (msg != null)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    private fun startGame(
        context: Context, side: Int, player1: String?, player2: String?,
        roomName: String?
    ) {
        val intent = Intent(context, GameTestActivity::class.java)
        intent.putExtra(MatchService.EXTRA_GAME_START, side)
        intent.putExtra(MatchService.EXTRA_GAME_ROOMNAME, roomName)
        intent.putExtra(MatchService.EXTRA_GAME_PLAYER1, player1)
        intent.putExtra(MatchService.EXTRA_GAME_PLAYER2, player2)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
