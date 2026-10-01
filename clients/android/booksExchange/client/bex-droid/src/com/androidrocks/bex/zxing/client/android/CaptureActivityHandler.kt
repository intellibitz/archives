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
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Message
import com.androidrocks.bex.R
import com.google.zxing.Result

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
class CaptureActivityHandler internal constructor(
    private val mActivity: CaptureActivity, decodeMode: String?,
    beginScanning: Boolean
) : Handler() {
    private val mDecodeThread: DecodeThread
    private var mState: State

    private enum class State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    init {
        mDecodeThread = DecodeThread(mActivity, decodeMode)
        mDecodeThread.start()
        mState = State.SUCCESS

        // Start ourselves capturing previews and decoding.
        CameraManager.get()!!.startPreview()
        if (beginScanning) {
            restartPreviewAndDecode()
        }
    }

    override fun handleMessage(message: Message) {
        when (message.what) {
            R.id.auto_focus ->                 // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (mState == State.PREVIEW) {
                    CameraManager.get()!!.requestAutoFocus(this, R.id.auto_focus)
                }

            R.id.restart_preview -> restartPreviewAndDecode()
            R.id.decode_succeeded -> {
                mState = State.SUCCESS
                val bundle = message.data
                val barcode = bundle.getParcelable<Bitmap>(DecodeThread.BARCODE_BITMAP)
                mActivity.handleDecode(message.obj as Result, barcode)
            }

            R.id.decode_failed -> {
                // We're decoding as fast as possible, so when one decode fails, start another.
                mState = State.PREVIEW
                CameraManager.get()!!.requestPreviewFrame(mDecodeThread.mHandler, R.id.decode)
            }

            R.id.return_scan_result -> {
                mActivity.setResult(Activity.RESULT_OK, message.obj as Intent)
                mActivity.finish()
            }

            R.id.launch_product_query -> {
                val url = message.obj as String
                mActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    fun quitSynchronously() {
        mState = State.DONE
        CameraManager.get()!!.stopPreview()
        val quit = Message.obtain(mDecodeThread.mHandler, R.id.quit)
        quit.sendToTarget()
        try {
            mDecodeThread.join()
        } catch (e: InterruptedException) {
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded)
        removeMessages(R.id.decode_failed)
    }

    private fun restartPreviewAndDecode() {
        if (mState == State.SUCCESS) {
            mState = State.PREVIEW
            CameraManager.get()!!.requestPreviewFrame(mDecodeThread.mHandler, R.id.decode)
            CameraManager.get()!!.requestAutoFocus(this, R.id.auto_focus)
            mActivity.drawViewfinder()
        }
    }
}
