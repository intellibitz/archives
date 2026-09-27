package com.fiteclub.android.smack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MatchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		if (!MatchService.ACTION_MATCH_SERVICE.equals(action))
			return;
		String msg = null;
		if (intent.hasExtra(MatchService.EXTRA_MATCH_ERROR_MSG)) {
			msg = "Error: " + intent.getStringExtra(MatchService.EXTRA_MATCH_ERROR_MSG);
		} else if (intent.hasExtra(MatchService.EXTRA_GAME_START)) {
			String player1 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER1);
			String player2 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER2);
			String roomName = intent.getStringExtra(MatchService.EXTRA_GAME_ROOMNAME);
			int side = intent.getIntExtra(MatchService.EXTRA_GAME_START, 0);
			startGame(context, side, player1, player2, roomName);
			msg = "player1: " + player1
				+ "\n" + "player2: " + player2
				+ "\n" + "room: " + roomName;
		}
		if (msg != null)
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	private void startGame(Context context, int side, String player1, String player2,
			String roomName) 
	{
		Intent intent = new Intent(context, GameTestActivity.class);
		intent.putExtra(MatchService.EXTRA_GAME_START, side);
		intent.putExtra(MatchService.EXTRA_GAME_ROOMNAME, roomName);
		intent.putExtra(MatchService.EXTRA_GAME_PLAYER1, player1);
		intent.putExtra(MatchService.EXTRA_GAME_PLAYER2, player2);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
