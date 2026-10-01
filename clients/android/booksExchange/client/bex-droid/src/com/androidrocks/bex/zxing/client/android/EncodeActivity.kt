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
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import com.androidrocks.bex.R

/**
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 */
class EncodeActivity : Activity() {

    private var mQRCodeEncoder: QRCodeEncoder? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mFirstLayout = false

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        val intent = intent
        if (intent != null && (intent.action == Intents.Encode.ACTION ||
                    intent.action == Intents.Encode.DEPRECATED_ACTION)
        ) {
            setContentView(R.layout.encode)
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val layout = findViewById<View>(R.id.encode_view)
        layout.viewTreeObserver.addOnGlobalLayoutListener(mLayoutListener)
        mFirstLayout = true
    }

    /**
     * This needs to be delayed until after the first layout so that the view dimensions will be
     * available.
     */
    val mLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (mFirstLayout) {
                val layout = findViewById<View>(R.id.encode_view)
                val width = layout.width
                val height = layout.height
                var smallerDimension = if (width < height) width else height
                smallerDimension = smallerDimension * 7 / 8

                val intent = intent
                try {
                    mQRCodeEncoder = QRCodeEncoder(this@EncodeActivity, intent)
                    title = getString(R.string.app_name) + " - " + mQRCodeEncoder!!.title
                    mQRCodeEncoder!!.requestBarcode(mHandler, smallerDimension)
                    mProgressDialog = ProgressDialog.show(
                        this@EncodeActivity, null,
                        getString(R.string.msg_encode_in_progress), true, true, mCancelListener
                    )
                } catch (e: IllegalArgumentException) {
                    showErrorMessage(R.string.msg_encode_contents_failed)
                }
                mFirstLayout = false
            }
        }
    }

    val mHandler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            when (message.what) {
                R.id.encode_succeeded -> {
                    mProgressDialog!!.dismiss()
                    mProgressDialog = null
                    val image = message.obj as Bitmap
                    val view = findViewById<View>(R.id.image_view) as ImageView
                    view.setImageBitmap(image)
                    val contents = findViewById<View>(R.id.contents_text_view) as TextView
                    contents.text = mQRCodeEncoder!!.displayContents
                    mQRCodeEncoder = null
                }

                R.id.encode_failed -> {
                    showErrorMessage(R.string.msg_encode_barcode_failed)
                    mQRCodeEncoder = null
                }
            }
        }
    }

    private fun showErrorMessage(message: Int) {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.button_ok, mClickListener)
        builder.show()
    }

    private val mClickListener = DialogInterface.OnClickListener { dialog, which -> finish() }

    private val mCancelListener = DialogInterface.OnCancelListener { dialog -> finish() }
}
