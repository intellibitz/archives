/*
 * Copyright (C) 2009 The Android Open Source Project
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

package intellibitz.intellidroid.camera

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.util.Log
import java.io.FileDescriptor
import java.util.WeakHashMap

/**
 * This class provides several utilities to cancel bitmap decoding.
 * <p>
 * The function decodeFileDescriptor() is used to decode a bitmap. During
 * decoding if another thread wants to cancel it, it calls the function
 * cancelThreadDecoding() specifying the Thread which is in decoding.
 * <p>
 * cancelThreadDecoding() is sticky until allowThreadDecoding() is called.
 */
class BitmapManager private constructor() {
    companion object {
        private const val TAG = "BitmapManager"
        private var sManager: BitmapManager? = null

        @Synchronized
        fun instance(): BitmapManager {
            if (sManager == null) {
                sManager = BitmapManager()
            }
            return sManager!!
        }
    }

    private val mThreadStatus = WeakHashMap<Thread, ThreadStatus>()

    /**
     * Get thread status and createFileInES one if specified.
     */
    private fun getOrCreateThreadStatus(t: Thread): ThreadStatus {
        synchronized(this) {
            var status = mThreadStatus[t]
            if (status == null) {
                status = ThreadStatus()
                mThreadStatus[t] = status
            }
            return status
        }
    }

    /**
     * The following three methods are used to keep track of
     * BitmapFaction.Options used for decoding and cancelling.
     */
    private fun setDecodingOptions(t: Thread, options: BitmapFactory.Options) {
        synchronized(this) {
            getOrCreateThreadStatus(t).mOptions = options
        }
    }

    fun removeDecodingOptions(t: Thread) {
        synchronized(this) {
            val status = mThreadStatus[t]
            status?.mOptions = null
        }
    }

    /**
     * The following three methods are used to keep track of which thread
     * is being disabled for bitmap decoding.
     */
    fun canThreadDecoding(t: Thread): Boolean {
        synchronized(this) {
            val status = mThreadStatus[t]
            if (status == null) {
                // allow decoding by default
                return true
            }

            val result = status.mState != State.CANCEL
            return result
        }
    }

    fun allowThreadDecoding(t: Thread) {
        synchronized(this) {
            getOrCreateThreadStatus(t).mState = State.ALLOW
        }
    }

    fun cancelThreadDecoding(t: Thread, cr: ContentResolver) {
        synchronized(this) {
            val status = getOrCreateThreadStatus(t)
            status.mState = State.CANCEL
            status.mOptions?.requestCancelDecode()

            // Wake up threads in waiting list
            notifyAll()

            // Since our cancel request can arrive MediaProvider earlier than getThumbnail request,
            // we use mThumbRequesting flag to make sure our request does cancel the request.
            try {
                synchronized(status) {
                    while (status.mThumbRequesting) {
                        Images.Thumbnails.cancelThumbnailRequest(cr, -1, t.id)
                        Video.Thumbnails.cancelThumbnailRequest(cr, -1, t.id)
                        status.wait(200)
                    }
                }
            } catch (ex: InterruptedException) {
                // ignore it.
            }
        }
    }

    fun getThumbnail(cr: ContentResolver, origId: Long, kind: Int,
                     options: BitmapFactory.Options, isVideo: Boolean): Bitmap? {
        val t = Thread.currentThread()
        val status = getOrCreateThreadStatus(t)

        if (!canThreadDecoding(t)) {
            Log.d(TAG, "Thread $t is not allowed to decode.")
            return null
        }

        try {
            synchronized(status) {
                status.mThumbRequesting = true
            }
            return if (isVideo) {
                Video.Thumbnails.getThumbnail(cr, origId, t.id, kind, null)
            } else {
                Images.Thumbnails.getThumbnail(cr, origId, t.id, kind, null)
            }
        } finally {
            synchronized(status) {
                status.mThumbRequesting = false
                status.notifyAll()
            }
        }
    }

    /**
     * The real place to delegate bitmap decoding to BitmapFactory.
     */
    fun decodeFileDescriptor(fd: FileDescriptor, options: BitmapFactory.Options): Bitmap? {
        if (options.mCancel) {
            return null
        }

        val thread = Thread.currentThread()
        if (!canThreadDecoding(thread)) {
            Log.d(TAG, "Thread $thread is not allowed to decode.")
            return null
        }

        setDecodingOptions(thread, options)
        val b = BitmapFactory.decodeFileDescriptor(fd, null, options)

        removeDecodingOptions(thread)
        return b
    }

    private enum class State { CANCEL, ALLOW }

    private class ThreadStatus {
        var mState = State.ALLOW
        var mOptions: BitmapFactory.Options? = null
        var mThumbRequesting = false

        override fun toString(): String {
            val s: String = when (mState) {
                State.CANCEL -> "Cancel"
                State.ALLOW -> "Allow"
                else -> "?"
            }
            return "thread state = $s, options = $mOptions"
        }
    }
}
