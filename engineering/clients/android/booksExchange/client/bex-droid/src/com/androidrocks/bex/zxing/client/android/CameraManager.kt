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

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import com.google.zxing.ResultPoint
import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
@Suppress("DEPRECATION")
class CameraManager private constructor(private val mContext: Context) {
    private var mCamera: Camera? = null
    private var mScreenResolution: Point? = null
    var framingRect: Rect? = null
        private set
    private var mPreviewHandler: Handler? = null
    private var mPreviewMessage = 0
    private var mAutoFocusHandler: Handler? = null
    private var mAutoFocusMessage = 0
    private var mInitialized: Boolean = false
    private var mPreviewing: Boolean = false

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private val previewCallback = Camera.PreviewCallback { data, _ ->
        if (mPreviewHandler != null) {
            val message = mPreviewHandler!!.obtainMessage(
                mPreviewMessage, mScreenResolution!!.x,
                mScreenResolution!!.y, data
            )
            message.sendToTarget()
            mPreviewHandler = null
        }
    }

    private val autoFocusCallback = Camera.AutoFocusCallback { success, _ ->
        if (mAutoFocusHandler != null) {
            val message = mAutoFocusHandler!!.obtainMessage(mAutoFocusMessage, success)
            // Simulate continuous autofocus by sending a focus request every 1.5 seconds.
            mAutoFocusHandler!!.sendMessageDelayed(message, 1500L)
            mAutoFocusHandler = null
        }
    }

    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder?) {
        if (mCamera == null) {
            mCamera = Camera.open()
            mCamera!!.setPreviewDisplay(holder)

            if (!mInitialized) {
                mInitialized = true
                getScreenResolution()
            }

            setCameraParameters()
        }
    }

    fun closeDriver() {
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
    }

    fun startPreview() {
        if (mCamera != null && !mPreviewing) {
            mCamera!!.startPreview()
            mPreviewing = true
        }
    }

    fun stopPreview() {
        if (mCamera != null && mPreviewing) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mPreviewHandler = null
            mAutoFocusHandler = null
            mPreviewing = false
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    fun requestPreviewFrame(handler: Handler?, message: Int) {
        if (mCamera != null && mPreviewing) {
            mPreviewHandler = handler
            mPreviewMessage = message
            mCamera!!.setOneShotPreviewCallback(previewCallback)
        }
    }

    fun requestAutoFocus(handler: Handler?, message: Int) {
        if (mCamera != null && mPreviewing) {
            mAutoFocusHandler = handler
            mAutoFocusMessage = message
            mCamera!!.autoFocus(autoFocusCallback)
        }
    }

    init {
        getFramingRect()
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. The actual captured image should be a bit larger than indicated because they might
     * frame the shot too tightly. This target helps with alignment as well as forces the user to hold
     * the device far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    private fun getFramingRect() {
        if (framingRect == null) {
            getScreenResolution()
            if (mScreenResolution != null) {
                val size = (if (mScreenResolution!!.x < mScreenResolution!!.y) mScreenResolution!!.x else mScreenResolution!!.y) * 3 / 4
                val leftOffset = (mScreenResolution!!.x - size) / 2
                val topOffset = (mScreenResolution!!.y - size) / 2
                framingRect = Rect(leftOffset, topOffset, leftOffset + size, topOffset + size)
                Log.v(TAG, "Calculated framing rect: $framingRect")
            }
        }
    }

    /**
     * Converts the result points from still resolution coordinates to screen coordinates.
     *
     * @param points The points returned by the Reader subclass through Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and offset appropriately
     * so they can be drawn in screen coordinates.
     */
    fun convertResultPoints(points: Array<ResultPoint>): Array<Point> {
        getFramingRect()
        val frame = framingRect!!
        val count = points.size
        val output = Array(count) { Point() }
        for (x in 0 until count) {
            output[x] = Point()
            output[x].x = frame.left + (points[x].x + 0.5f).toInt()
            output[x].y = frame.top + (points[x].y + 0.5f).toInt()
        }
        return output
    }

    /**
     * Sets the camera up to take preview images which are used for both preview and decoding. We're
     * counting on the default YUV420 semi-planar data. If that changes in the future, we'll need to
     * specify it explicitly with setPreviewFormat().
     */
    private fun setCameraParameters() {
        val parameters = mCamera!!.parameters
        parameters.setPreviewSize(mScreenResolution!!.x, mScreenResolution!!.y)

        // Disables the built-in flash if present. Hopefully devices will honor this setting.
        parameters.set("flash-mode", "off")
        mCamera!!.parameters = parameters
        Log.v(
            TAG, "Setting params for preview: width " + mScreenResolution!!.x + " height " +
                    mScreenResolution!!.y
        )
    }

    private fun getScreenResolution() {
        if (mScreenResolution == null) {
            val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            mScreenResolution = Point(display.width, display.height)
        }
    }

    companion object {
        private const val TAG = "CameraManager"

        private var mCameraManager: CameraManager? = null

        @JvmStatic
        fun init(context: Context) {
            if (mCameraManager == null) {
                mCameraManager = CameraManager(context)
            }
        }

        @JvmStatic
        fun get(): CameraManager? {
            return mCameraManager
        }
    }
}
