package intellibitz.intellidroid.sound

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import intellibitz.intellidroid.R
import java.io.IOException

class AudioRecordActivity : AppCompatActivity() {
    private var mStartPlaying = true
    private var mStartRecording = true
    private var mPlayer: MediaPlayer? = null
    private var mRecorder: MediaRecorder? = null
    private var mFileName: String? = null
    private var btnRecord: Button? = null
    private var btnPlay: Button? = null
    private var btnOk: Button? = null
    private var btnCancel: Button? = null

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        val player = MediaPlayer()
        mPlayer = player
        try {
            player.setDataSource(mFileName)
            player.prepare()
            player.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
    }

    private fun startRecording() {
        val recorder = MediaRecorder()
        mRecorder = recorder
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setOutputFile(mFileName)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            recorder.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        recorder.start()
    }

    private fun stopRecording() {
        mRecorder?.let {
            it.stop()
            it.release()
        }
        mRecorder = null
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_audio_record)

        val uri: Uri? = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT)
        mFileName = uri?.path

        btnRecord = findViewById<Button>(R.id.btn_record).apply {
            text = "Start recording"
            setOnClickListener {
                onRecord(mStartRecording)
                text = if (mStartRecording) "Stop recording" else "Start recording"
                mStartRecording = !mStartRecording
            }
        }

        btnPlay = findViewById<Button>(R.id.btn_play).apply {
            text = "Start playing"
            setOnClickListener {
                onPlay(mStartPlaying)
                text = if (mStartPlaying) "Stop playing" else "Start playing"
                mStartPlaying = !mStartPlaying
            }
        }

        btnOk = findViewById<Button>(R.id.btn_ok).apply {
            setOnClickListener {
                val resultIntent = intent
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        btnCancel = findViewById<Button>(R.id.btn_cancel).apply {
            setOnClickListener {
                val resultIntent = intent
                setResult(Activity.RESULT_CANCELED, resultIntent)
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mRecorder != null) {
            mRecorder?.release()
            mRecorder = null
        }

        if (mPlayer != null) {
            mPlayer?.release()
            mPlayer = null
        }
    }

    companion object {
        private const val LOG_TAG = "AudioRecord"
    }
}
