package intellibitz.intellidroid.sound;

/**
 */
/*
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import intellibitz.intellidroid.R;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;


public class AudioRecordActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AudioRecord";
    boolean mStartPlaying = true;
    boolean mStartRecording = true;
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    private String mFileName = null;
    private Button btnRecord = null;
    private Button btnPlay = null;
    private Button btnOk = null;
    private Button btnCancel = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_record);

        Uri uri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        mFileName = uri.getPath();

        btnRecord = (Button) findViewById(R.id.btn_record);
        btnRecord.setText("Start recording");
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    btnRecord.setText("Stop recording");
                } else {
                    btnRecord.setText("Start recording");
                }
                mStartRecording = !mStartRecording;

            }
        });
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setText("Start playing");
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    btnPlay.setText("Stop playing");
                } else {
                    btnPlay.setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;

            }
        });
        btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}