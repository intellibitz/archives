package com.fiteclub.android.smack;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.fiteclub.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GameTestActivity extends Activity {
	private static final String TAG = "GameTestActivity";
	private XmppManager xmppManager;
	private String userName;
	private MultiUserChat muc;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.setTitle("Game Test");
        
        Log.d(TAG, "onCreate");
        
        Intent intent = this.getIntent();
        if (intent.hasExtra(MatchService.EXTRA_GAME_START)) {
			String player1 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER1);
			String player2 = intent.getStringExtra(MatchService.EXTRA_GAME_PLAYER2);
			String roomName = intent.getStringExtra(MatchService.EXTRA_GAME_ROOMNAME);
			int side = intent.getIntExtra(MatchService.EXTRA_GAME_START, 0);
			startGame(side, player1, player2, roomName);
        }
        Log.d(TAG, "onCreate end");
	}

	private void startGame(int side, String player1, String player2,
			String roomName) 
	{
		Log.d(TAG, "startGame");
		Log.d(TAG, "side: " + side);
		Log.d(TAG, "player1: " + player1);
		Log.d(TAG, "player2: " + player2);
		Log.d(TAG, "roomName: " + roomName);

		if (side == MatchService.PLAYER1_SIDE) {
			userName = player1;
		} else {
			userName = player2;
		}

		xmppManager = XmppManager.getManager();

		muc = xmppManager.openGameRoom(roomName, new MessageListener() {

			public void processMessage(String message) 
			{
				Log.d(TAG, "Got a new message: " + message);
			}
			
		});
		if (muc == null)
		{
			Toast.makeText(this, "Can not open game room!", Toast.LENGTH_LONG).show();
			return;
		}
		new Thread() {
			@Override
			public void run() 
			{
				playGame();
			}
		}.start();
	}

	
	private void playGame()  
	{
		for (int i=0; i<100; i++)
		{
			if (muc == null)
				return;
			try {
				muc.sendMessage("Message from " + userName + " id=" + i);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		if (xmppManager != null)
		{
			if ((muc != null) && (muc.isJoined()))
				muc.leave();
			muc = null;
			xmppManager.clear();
			xmppManager = null;
		}
	}

}
