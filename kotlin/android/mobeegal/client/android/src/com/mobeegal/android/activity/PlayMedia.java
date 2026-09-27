/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import com.mobeegal.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author gunasekaran
 */
public class PlayMedia
        extends Activity
{

    private String path;
    private VideoView mVideoView;
    private Button playButton;
    private Button stopButton;
    private Button pauseButton;
    private Button resumeButton;
    private Bundle b;

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.playmedia);
        b = this.getIntent().getExtras();
        if (b != null)
        {
            path = b.getString("key");
        }
        Toast.makeText(PlayMedia.this, path, Toast.LENGTH_SHORT).show();
        mVideoView = (VideoView) findViewById(R.id.video);
        playButton = (Button) findViewById(R.id.play);
        stopButton = (Button) findViewById(R.id.stop);
        pauseButton = (Button) findViewById(R.id.pause);
        resumeButton = (Button) findViewById(R.id.resume);
        mVideoView.setMediaController(new MediaController(this));


        playButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {

                try
                {
                    URL url = new URL(path);
                    URLConnection cn = url.openConnection();
                    cn.connect();
                    InputStream stream = cn.getInputStream();
                    if (stream == null)
                    {
                        throw new RuntimeException("stream is null");
                    }
                    File temp = File.createTempFile("mediaplayertmp", "dat");
                    String tempPath = temp.getAbsolutePath();
                    FileOutputStream out = new FileOutputStream(temp);
                    byte[] buf = new byte[128];
                    do
                    {
                        int numread = stream.read(buf);
                        if (numread <= 0)
                        {
                            break;
                        }
                        out.write(buf, 0, numread);
                    }
                    while (true);
                    mVideoView.setVideoURI(Uri.parse(tempPath));
                    mVideoView.requestFocus();
                    stream.close();
                }
                catch (Exception ex)
                {
                    Toast.makeText(PlayMedia.this, "Illegal",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {

                mVideoView.stopPlayback();
                finish();
            }
        });

        pauseButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                mVideoView.pause();

            }
        });

        resumeButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                mVideoView.start();
            }
        });


        Log.i("\n\nvideo", String.valueOf(mVideoView.isPlaying()));
    }

} 
