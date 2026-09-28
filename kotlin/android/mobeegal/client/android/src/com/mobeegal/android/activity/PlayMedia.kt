/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.activity

import android.app.Activity
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.mobeegal.android.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

/**
 * @author gunasekaran
 */
class PlayMedia : Activity() {

    private var path: String? = null
    private var mVideoView: VideoView? = null
    private var playButton: Button? = null
    private var stopButton: Button? = null
    private var pauseButton: Button? = null
    private var resumeButton: Button? = null
    private var b: Bundle? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        window.setFormat(PixelFormat.TRANSLUCENT)
        setContentView(R.layout.playmedia)
        b = this.intent.extras
        if (b != null) {
            path = b!!.getString("key")
        }
        Toast.makeText(this@PlayMedia, path, Toast.LENGTH_SHORT).show()
        mVideoView = findViewById(R.id.video) as VideoView
        playButton = findViewById(R.id.play) as Button
        stopButton = findViewById(R.id.stop) as Button
        pauseButton = findViewById(R.id.pause) as Button
        resumeButton = findViewById(R.id.resume) as Button
        mVideoView!!.setMediaController(MediaController(this))

        playButton!!.setOnClickListener {
            try {
                val url = URL(path)
                val cn = url.openConnection()
                cn.connect()
                val stream: InputStream? = cn.getInputStream()
                if (stream == null) {
                    throw RuntimeException("stream is null")
                }
                val temp = File.createTempFile("mediaplayertmp", "dat")
                val tempPath = temp.absolutePath
                val out = FileOutputStream(temp)
                val buf = ByteArray(128)
                do {
                    val numread = stream.read(buf)
                    if (numread <= 0) {
                        break
                    }
                    out.write(buf, 0, numread)
                } while (true)
                mVideoView!!.setVideoURI(Uri.parse(tempPath))
                mVideoView!!.requestFocus()
                stream.close()
            } catch (ex: Exception) {
                Toast.makeText(
                    this@PlayMedia, "Illegal",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        stopButton!!.setOnClickListener {
            mVideoView!!.stopPlayback()
            finish()
        }

        pauseButton!!.setOnClickListener {
            mVideoView!!.pause()
        }

        resumeButton!!.setOnClickListener {
            mVideoView!!.start()
        }

        Log.i("\n\nvideo", mVideoView!!.isPlaying.toString())
    }
}
