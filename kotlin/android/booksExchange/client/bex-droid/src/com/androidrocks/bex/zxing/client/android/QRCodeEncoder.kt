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
import android.os.Handler
import android.os.Message
import android.provider.Contacts
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.androidrocks.bex.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

@Suppress("DEPRECATION")
class QRCodeEncoder(private val mActivity: Activity, intent: Intent?) {
    var contents: String? = null
        private set
    var displayContents: String? = null
        private set
    var title: String? = null
        private set
    private var mFormat: BarcodeFormat? = null

    init {
        if (!encodeContents(intent)) {
            throw IllegalArgumentException("No valid data to encode.")
        }
    }

    fun requestBarcode(handler: Handler?, pixelResolution: Int) {
        val encodeThread: Thread = EncodeThread(
            contents, handler, pixelResolution,
            mFormat
        )
        encodeThread.start()
    }

    val format: String
        get() = mFormat.toString()

    // It would be nice if the string encoding lived in the core ZXing library,
    // but we use platform specific code like PhoneNumberUtils, so it can't.
    private fun encodeContents(intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }

        // default to QR_CODE if no format given
        val format = intent.getStringExtra(Intents.Encode.FORMAT)
        if (format == null || format.length == 0 ||
            format == Contents.Format.QR_CODE
        ) {
            val type = intent.getStringExtra(Intents.Encode.TYPE)
            if (type == null || type.length == 0) {
                return false
            }
            mFormat = BarcodeFormat.QR_CODE
            encodeQRCodeContents(intent, type)
        } else {
            val data = intent.getStringExtra(Intents.Encode.DATA)
            if (data != null && data.length != 0) {
                contents = data
                displayContents = data
                title = mActivity.getString(R.string.contents_text)
                if (format == Contents.Format.CODE_128) mFormat =
                    BarcodeFormat.CODE_128 else if (format == Contents.Format.CODE_39) mFormat =
                    BarcodeFormat.CODE_39 else if (format == Contents.Format.EAN_8) mFormat =
                    BarcodeFormat.EAN_8 else if (format == Contents.Format.EAN_13) mFormat =
                    BarcodeFormat.EAN_13 else if (format == Contents.Format.UPC_A) mFormat =
                    BarcodeFormat.UPC_A else if (format == Contents.Format.UPC_E) mFormat =
                    BarcodeFormat.UPC_E
            }
        }
        return contents != null && contents!!.length > 0
    }

    private fun encodeQRCodeContents(intent: Intent, type: String) {
        if (type == Contents.Type.TEXT) {
            val data = intent.getStringExtra(Intents.Encode.DATA)
            if (data != null && data.length > 0) {
                contents = data
                displayContents = data
                title = mActivity.getString(R.string.contents_text)
            }
        } else if (type == Contents.Type.EMAIL) {
            val data = intent.getStringExtra(Intents.Encode.DATA)
            if (data != null && data.length > 0) {
                contents = "mailto:$data"
                displayContents = data
                title = mActivity.getString(R.string.contents_email)
            }
        } else if (type == Contents.Type.PHONE) {
            val data = intent.getStringExtra(Intents.Encode.DATA)
            if (data != null && data.length > 0) {
                contents = "tel:$data"
                displayContents = PhoneNumberUtils.formatNumber(data)
                title = mActivity.getString(R.string.contents_phone)
            }
        } else if (type == Contents.Type.SMS) {
            val data = intent.getStringExtra(Intents.Encode.DATA)
            if (data != null && data.length > 0) {
                contents = "sms:$data"
                displayContents = PhoneNumberUtils.formatNumber(data)
                title = mActivity.getString(R.string.contents_sms)
            }
        } else if (type == Contents.Type.CONTACT) {
            val bundle = intent.getBundleExtra(Intents.Encode.DATA)
            if (bundle != null) {
                val newContents = java.lang.StringBuilder()
                val newDisplayContents = java.lang.StringBuilder()
                newContents.append("MECARD:")
                val name = bundle.getString(ContactsContract.Intents.Insert.NAME)
                if (name != null && name.length > 0) {
                    newContents.append("N:").append(name).append(';')
                    newDisplayContents.append(name)
                }
                val address = bundle.getString(ContactsContract.Intents.Insert.POSTAL)
                if (address != null && address.length > 0) {
                    newContents.append("ADR:").append(address).append(';')
                    newDisplayContents.append('\n').append(address)
                }
                for (x in Contents.PHONE_KEYS.indices) {
                    val phone = bundle.getString(Contents.PHONE_KEYS[x])
                    if (phone != null && phone.length > 0) {
                        newContents.append("TEL:").append(phone).append(';')
                        newDisplayContents.append('\n').append(PhoneNumberUtils.formatNumber(phone))
                    }
                }
                for (x in Contents.EMAIL_KEYS.indices) {
                    val email = bundle.getString(Contents.EMAIL_KEYS[x])
                    if (email != null && email.length > 0) {
                        newContents.append("EMAIL:").append(email).append(';')
                        newDisplayContents.append('\n').append(email)
                    }
                }
                // Make sure we've encoded at least one field.
                if (newDisplayContents.length > 0) {
                    newContents.append(';')
                    contents = newContents.toString()
                    displayContents = newDisplayContents.toString()
                    title = mActivity.getString(R.string.contents_contact)
                } else {
                    contents = null
                    displayContents = null
                }
            }
        } else if (type == Contents.Type.LOCATION) {
            val bundle = intent.getBundleExtra(Intents.Encode.DATA)
            if (bundle != null) {
                // These must use Bundle.getFloat(), not getDouble(), it's part of the API.
                val latitude = bundle.getFloat("LAT", Float.MAX_VALUE)
                val longitude = bundle.getFloat("LONG", Float.MAX_VALUE)
                if (latitude != Float.MAX_VALUE && longitude != Float.MAX_VALUE) {
                    contents = "geo:$latitude,$longitude"
                    displayContents = "$latitude,$longitude"
                    title = mActivity.getString(R.string.contents_location)
                }
            }
        }
    }

    private class EncodeThread(
        private val mContents: String?,
        private val mHandler: Handler?,
        private val mPixelResolution: Int,
        private val mFormat: BarcodeFormat?
    ) : Thread() {
        override fun run() {
            try {
                val result = MultiFormatWriter().encode(
                    mContents,
                    mFormat, mPixelResolution, mPixelResolution
                )
                val width = result.width
                val height = result.height
                val array = result.array
                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val grey = array[y][x].toInt() and 0xff
                        // pixels[y * width + x] = (0xff << 24) | (grey << 16) | (grey << 8) | grey;
                        pixels[y * width + x] = -0x1000000 or (0x00010101 * grey)
                    }
                }

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                val message = Message.obtain(mHandler, R.id.encode_succeeded)
                message.obj = bitmap
                message.sendToTarget()
            } catch (e: WriterException) {
                Log.e(TAG, e.toString())
                val message = Message.obtain(mHandler, R.id.encode_failed)
                message.sendToTarget()
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.toString())
                val message = Message.obtain(mHandler, R.id.encode_failed)
                message.sendToTarget()
            }
        }

        companion object {
            private const val TAG = "EncodeThread"
        }
    }
}
