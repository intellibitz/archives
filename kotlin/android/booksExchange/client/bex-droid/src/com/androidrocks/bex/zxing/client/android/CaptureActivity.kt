/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Zxing project to suit Books-Exchange requirements.
 * Original source from Zxing - http://code.google.com/p/zxing/
 */

/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidrocks.bex.zxing.client.android

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.os.Vibrator
import android.preference.PreferenceManager
import android.text.ClipboardManager
import android.text.SpannableStringBuilder
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.androidrocks.bex.R
import com.androidrocks.bex.zxing.client.android.result.ResultButtonListener
import com.androidrocks.bex.zxing.client.android.result.ResultHandler
import com.androidrocks.bex.zxing.client.android.result.ResultHandlerFactory
import com.google.zxing.Result
import java.io.IOException

/**
 * The barcode reader activity itself. This is loosely based on the CameraPreview
 * example included in the Android SDK.
 */
@Suppress("DEPRECATION")
class CaptureActivity : Activity(), SurfaceHolder.Callback {

    private enum class Source {
        NATIVE_APP_INTENT,
        PRODUCT_SEARCH_LINK,
        ZXING_LINK,
        NONE
    }

    @JvmField
    var mHandler: CaptureActivityHandler? = null

    private var mViewfinderView: ViewfinderView? = null
    private var mStatusView: View? = null
    private var mResultView: View? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mLastResult: Result? = null
    private var mHasSurface = false
    private var mPlayBeep = false
    private var mVibrate = false
    private var mCopyToClipboard = false
    private var mSource: Source? = null
    private var mSourceUrl: String? = null
    private var mDecodeMode: String? = null
    private var mVersionName: String? = null

    private val mBeepListener: MediaPlayer.OnCompletionListener = BeepListener()

    override fun onCreate(icicle: Bundle?) {
        Log.i(TAG, "Creating CaptureActivity")
        super.onCreate(icicle)

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.capture)

        CameraManager.init(application)
        mViewfinderView = findViewById<View>(R.id.viewfinder_view) as ViewfinderView
        mResultView = findViewById(R.id.result_view)
        mStatusView = findViewById(R.id.status_view)
        mHandler = null
        mLastResult = null
        mHasSurface = false

        showHelpOnFirstLaunch()
    }

    override fun onResume() {
        super.onResume()

        val surfaceView = findViewById<View>(R.id.preview_view) as SurfaceView
        val surfaceHolder = surfaceView.holder
        if (mHasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        val intent = intent
        val action = intent?.action
        val dataString = intent?.dataString
        if (intent != null && action != null) {
            if (action == Intents.Scan.ACTION || action == Intents.Scan.DEPRECATED_ACTION) {
                // Scan the formats the intent requested, and return the result to the calling activity.
                mSource = Source.NATIVE_APP_INTENT
                mDecodeMode = intent.getStringExtra(Intents.Scan.MODE)
                resetStatusView()
            } else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) &&
                dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)
            ) {
                // Scan only products and send the result to mobile Product Search.
                mSource = Source.PRODUCT_SEARCH_LINK
                mSourceUrl = dataString
                mDecodeMode = Intents.Scan.PRODUCT_MODE
                resetStatusView()
            } else if (dataString != null && dataString == ZXING_URL) {
                // Scan all formats and handle the results ourselves.
                // TODO: In the future we could allow the hyperlink to include a URL to send the results to.
                mSource = Source.ZXING_LINK
                mSourceUrl = dataString
                mDecodeMode = null
                resetStatusView()
            } else {
                // Scan all formats and handle the results ourselves (launched from Home).
                mSource = Source.NONE
                mDecodeMode = null
                resetStatusView()
            }
        } else {
            mSource = Source.NONE
            mDecodeMode = null
            if (mLastResult == null) {
                resetStatusView()
            }
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, true)
        mVibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, false)
        mCopyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true)
        initBeepSound()
    }

    override fun onPause() {
        super.onPause()
        if (mHandler != null) {
            mHandler!!.quitSynchronously()
            mHandler = null
        }
        CameraManager.get()!!.closeDriver()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSource == Source.NATIVE_APP_INTENT) {
                setResult(RESULT_CANCELED)
                finish()
                return true
            } else if ((mSource == Source.NONE || mSource == Source.ZXING_LINK) && mLastResult != null) {
                resetStatusView()
                mHandler!!.sendEmptyMessage(R.id.restart_preview)
                return true
            }
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Handle these events so they don't launch the Camera app
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, SHARE_ID, 0, R.string.menu_share).setIcon(R.drawable.share_menu_item)
        menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
            .setIcon(android.R.drawable.ic_menu_preferences)
        menu.add(0, HELP_ID, 0, R.string.menu_help)
            .setIcon(android.R.drawable.ic_menu_help)
        menu.add(0, ABOUT_ID, 0, R.string.menu_about)
            .setIcon(android.R.drawable.ic_menu_info_details)
        return true
    }

    // Don't display the share menu item if the result overlay is showing.
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(SHARE_ID).isVisible = mLastResult == null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            SHARE_ID -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(this, ShareActivity::class.java.name)
                startActivity(intent)
            }

            SETTINGS_ID -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(this, PreferencesActivity::class.java.name)
                startActivity(intent)
            }

            HELP_ID -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(this, HelpActivity::class.java.name)
                startActivity(intent)
            }

            ABOUT_ID -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.title_about) + mVersionName)
                builder.setMessage(getString(R.string.msg_about) + "\n\n" + getString(R.string.zxing_url))
                builder.setIcon(R.drawable.zxing_icon)
                builder.setPositiveButton(R.string.button_open_browser, mAboutListener)
                builder.setNegativeButton(R.string.button_cancel, null)
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(config: Configuration) {
        // Do nothing, this is to prevent the activity from being restarted when the keyboard opens.
        super.onConfigurationChanged(config)
    }

    private val mAboutListener = DialogInterface.OnClickListener { dialogInterface, i ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.zxing_url)))
        startActivity(intent)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!mHasSurface) {
            mHasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mHasSurface = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    fun handleDecode(rawResult: Result, barcode: Bitmap?) {
        mLastResult = rawResult
        playBeepSoundAndVibrate()
        drawResultPoints(barcode, rawResult)

        when (mSource) {
            Source.NATIVE_APP_INTENT, Source.PRODUCT_SEARCH_LINK -> handleDecodeExternally(
                rawResult,
                barcode
            )

            Source.ZXING_LINK, Source.NONE -> handleDecodeInternally(rawResult, barcode)
            else -> {}
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param rawResult The decoded results which contains the points to draw.
     */
    private fun drawResultPoints(barcode: Bitmap?, rawResult: Result) {
        val points = rawResult.resultPoints
        if (points != null && points.size > 0 && barcode != null) {
            val canvas = Canvas(barcode)
            val paint = Paint()
            paint.color = resources.getColor(R.color.result_image_border)
            paint.strokeWidth = 3.0f
            paint.style = Paint.Style.STROKE
            val border = Rect(2, 2, barcode.width - 2, barcode.height - 2)
            canvas.drawRect(border, paint)

            paint.color = resources.getColor(R.color.result_points)
            if (points.size == 2) {
                paint.strokeWidth = 4.0f
                canvas.drawLine(
                    points[0].x, points[0].y, points[1].x,
                    points[1].y, paint
                )
            } else {
                paint.strokeWidth = 10.0f
                for (point in points) {
                    canvas.drawPoint(point.x, point.y, paint)
                }
            }
        }
    }

    // Put up our own UI for how to handle the decoded contents.
    private fun handleDecodeInternally(rawResult: Result, barcode: Bitmap?) {
        mStatusView!!.visibility = View.GONE
        mViewfinderView!!.visibility = View.GONE
        mResultView!!.visibility = View.VISIBLE

        val barcodeImageView = findViewById<View>(R.id.barcode_image_view) as ImageView
        barcodeImageView.maxWidth = MAX_RESULT_IMAGE_SIZE
        barcodeImageView.maxHeight = MAX_RESULT_IMAGE_SIZE
        barcodeImageView.setImageBitmap(barcode)

        val formatTextView = findViewById<View>(R.id.format_text_view) as TextView
        formatTextView.text = getString(R.string.msg_default_format) + ": " +
                rawResult.barcodeFormat.toString()

        val resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult)
        val typeTextView = findViewById<View>(R.id.type_text_view) as TextView
        typeTextView.text = getString(R.string.msg_default_type) + ": " +
                resultHandler.type.toString()

        val contentsTextView = findViewById<View>(R.id.contents_text_view) as TextView
        val title: CharSequence = getString(resultHandler.displayTitle)
        val styled = SpannableStringBuilder(title.toString() + "\n\n")
        styled.setSpan(UnderlineSpan(), 0, title.length, 0)
        val displayContents = resultHandler.displayContents
        styled.append(displayContents)
        contentsTextView.text = styled

        val buttonCount = resultHandler.buttonCount
        val buttonView = findViewById<View>(R.id.result_button_view) as ViewGroup
        buttonView.requestFocus()
        for (x in 0 until ResultHandler.MAX_BUTTON_COUNT) {
            val button = buttonView.getChildAt(x) as Button
            if (x < buttonCount) {
                button.visibility = View.VISIBLE
                button.text = resultHandler.getButtonText(x)
                button.setOnClickListener(ResultButtonListener(resultHandler, x))
            } else {
                button.visibility = View.GONE
            }
        }

        if (mCopyToClipboard) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = displayContents
        }
    }

    // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
    private fun handleDecodeExternally(rawResult: Result, barcode: Bitmap?) {
        mViewfinderView!!.drawResultBitmap(barcode)

        // Since this message will only be shown for a second, just tell the user what kind of
        // barcode was found (e.g. contact info) rather than the full contents, which they won't
        // have time to read.
        val resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult)
        val textView = findViewById<View>(R.id.status_text_view) as TextView
        textView.gravity = Gravity.CENTER
        textView.textSize = 18.0f
        textView.text = getString(resultHandler.displayTitle)

        mStatusView!!.setBackgroundColor(resources.getColor(R.color.transparent))

        if (mCopyToClipboard) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = resultHandler.displayContents
        }

        if (mSource == Source.NATIVE_APP_INTENT) {
            // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
            // the deprecated intent is retired.
            val intent = Intent(intent.action)
            intent.putExtra(Intents.Scan.RESULT, rawResult.toString())
            intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.barcodeFormat.toString())
            val message = Message.obtain(mHandler, R.id.return_scan_result)
            message.obj = intent
            mHandler!!.sendMessageDelayed(message, INTENT_RESULT_DURATION)
        } else if (mSource == Source.PRODUCT_SEARCH_LINK) {
            // Reformulate the URL which triggered us into a query, so that the request goes to the same
            // TLD as the scan URL.
            val message = Message.obtain(mHandler, R.id.launch_product_query)
            val end = mSourceUrl!!.lastIndexOf("/scan")
            message.obj = mSourceUrl!!.substring(0, end) + "?q=" +
                    resultHandler.displayContents.toString() + "&source=zxing"
            mHandler!!.sendMessageDelayed(message, INTENT_RESULT_DURATION)
        }
    }

    /**
     * We want the help screen to be shown automatically the first time a new version of the app is
     * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
     * it to a value stored as a preference.
     */
    private fun showHelpOnFirstLaunch() {
        try {
            val info = packageManager.getPackageInfo(PACKAGE_NAME, 0)
            val currentVersion = info.versionCode
            // Since we're paying to talk to the PackageManager anyway, it makes sense to cache the app
            // version name here for display in the about box later.
            this.mVersionName = info.versionName
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0)
            if (currentVersion > lastVersion) {
                prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(this, HelpActivity::class.java.name)
                startActivity(intent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, e)
        }
    }

    /**
     * Creates the beep MediaPlayer in advance so that the sound can be triggered with the least
     * latency possible.
     */
    private fun initBeepSound() {
        if (mPlayBeep && mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_SYSTEM)
            mMediaPlayer!!.setOnCompletionListener(mBeepListener)

            val file = resources.openRawResourceFd(R.raw.beep)
            try {
                mMediaPlayer!!.setDataSource(
                    file.fileDescriptor, file.startOffset,
                    file.length
                )
                file.close()
                mMediaPlayer!!.setVolume(BEEP_VOLUME, BEEP_VOLUME)
                mMediaPlayer!!.prepare()
            } catch (e: IOException) {
                mMediaPlayer = null
            }
        }
    }

    private fun playBeepSoundAndVibrate() {
        if (mPlayBeep && mMediaPlayer != null) {
            mMediaPlayer!!.start()
        }
        if (mVibrate) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get()!!.openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            Log.w(TAG, ioe)
            return
        }
        if (mHandler == null) {
            val beginScanning = mLastResult == null
            mHandler = CaptureActivityHandler(this, mDecodeMode, beginScanning)
        }
    }

    private fun resetStatusView() {
        mResultView!!.visibility = View.GONE
        mStatusView!!.visibility = View.VISIBLE
        mStatusView!!.setBackgroundColor(resources.getColor(R.color.status_view))
        mViewfinderView!!.visibility = View.VISIBLE

        val textView = findViewById<View>(R.id.status_text_view) as TextView
        textView.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        textView.textSize = 14.0f
        textView.setText(R.string.msg_default_status)
        mLastResult = null
    }

    fun drawViewfinder() {
        mViewfinderView!!.drawViewfinder()
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private class BeepListener : MediaPlayer.OnCompletionListener {
        override fun onCompletion(mediaPlayer: MediaPlayer) {
            mediaPlayer.seekTo(0)
        }
    }

    companion object {
        private const val TAG = "CaptureActivity"

        private const val SHARE_ID = Menu.FIRST
        private const val SETTINGS_ID = Menu.FIRST + 1
        private const val HELP_ID = Menu.FIRST + 2
        private const val ABOUT_ID = Menu.FIRST + 3

        private const val MAX_RESULT_IMAGE_SIZE = 150
        private const val INTENT_RESULT_DURATION = 1500L
        private const val BEEP_VOLUME = 0.15f
        private const val VIBRATE_DURATION = 200L

        private const val PACKAGE_NAME = "com.androidrocks.bex.zxing.client.android"
        private const val PRODUCT_SEARCH_URL_PREFIX = "http://www.google"
        private const val PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan"
        private const val ZXING_URL = "http://zxing.appspot.com/scan"
    }
}
