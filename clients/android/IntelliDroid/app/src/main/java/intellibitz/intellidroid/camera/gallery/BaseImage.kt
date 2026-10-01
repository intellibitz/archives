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

package intellibitz.intellidroid.camera.gallery

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore.Images
import android.util.Log
import intellibitz.intellidroid.camera.BitmapManager
import intellibitz.intellidroid.camera.Util
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * Represents a particular image and provides access to the underlying bitmap
 * and two thumbnail bitmaps as well as other information such as the id, and
 * the path to the actual image data.
 */
abstract class BaseImage(
    protected val mContainer: BaseImageList,
    protected val mContentResolver: ContentResolver,
    protected val mId: Long,
    protected val mIndex: Int,
    protected val mUri: Uri,
    protected val mDataPath: String,
    protected val mMimeType: String,
    private val mDateTaken: Long,
    private val mTitle: String
) : IImage {

    companion object {
        private const val TAG = "BaseImage"
        private const val UNKNOWN_LENGTH = -1
        const val ROTATE_AS_NEEDED = true
        const val NO_NATIVE = false
    }

    private var mWidth = UNKNOWN_LENGTH
    private var mHeight = UNKNOWN_LENGTH

    val dataPath: String
        get() = mDataPath

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Image) return false
        return mUri == other.mUri
    }

    override fun hashCode(): Int {
        return mUri.hashCode()
    }

    fun fullSizeBitmap(minSideLength: Int, maxNumberOfPixels: Int): Bitmap? {
        return fullSizeBitmap(minSideLength, maxNumberOfPixels, ROTATE_AS_NEEDED, NO_NATIVE)
    }

    fun fullSizeBitmap(
        minSideLength: Int,
        maxNumberOfPixels: Int,
        rotateAsNeeded: Boolean,
        useNative: Boolean
    ): Bitmap? {
        val url = mContainer.contentUri(mId) ?: return null

        var b = Util.makeBitmap(minSideLength, maxNumberOfPixels, url, mContentResolver, useNative)

        if (b != null && rotateAsNeeded) {
            b = Util.rotate(b, degreesRotated)
        }

        return b
    }

    fun fullSizeImageData(): InputStream? {
        return try {
            mContentResolver.openInputStream(mUri)
        } catch (ex: IOException) {
            null
        }
    }

    val fullSizeImageUri: Uri
        get() = mUri

    val container: IImageList
        get() = mContainer

    val dateTaken: Long
        get() = mDateTaken

    val degreesRotated: Int
        get() = 0

    val mimeType: String
        get() = mMimeType

    val title: String
        get() = mTitle

    private fun setupDimension() {
        var input: ParcelFileDescriptor? = null
        try {
            input = mContentResolver.openFileDescriptor(mUri, "r")
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapManager.instance().decodeFileDescriptor(input.fileDescriptor, options)
            mWidth = options.outWidth
            mHeight = options.outHeight
        } catch (ex: FileNotFoundException) {
            mWidth = 0
            mHeight = 0
        } finally {
            Util.closeSilently(input)
        }
    }

    val width: Int
        get() {
            if (mWidth == UNKNOWN_LENGTH) setupDimension()
            return mWidth
        }

    val height: Int
        get() {
            if (mHeight == UNKNOWN_LENGTH) setupDimension()
            return mHeight
        }

    fun miniThumbBitmap(): Bitmap? {
        var b: Bitmap? = null
        try {
            val id = mId
            b = BitmapManager.instance().getThumbnail(
                mContentResolver,
                id,
                Images.Thumbnails.MICRO_KIND,
                null,
                false
            )
        } catch (ex: Throwable) {
            Log.e(TAG, "miniThumbBitmap got exception", ex)
            return null
        }
        if (b != null) {
            b = Util.rotate(b, degreesRotated)
        }
        return b
    }

    protected open fun onRemove() {
    }

    override fun toString(): String {
        return mUri.toString()
    }
}
