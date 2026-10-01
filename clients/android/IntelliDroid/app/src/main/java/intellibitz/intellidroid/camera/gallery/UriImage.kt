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
import android.util.Log
import intellibitz.intellidroid.camera.BitmapManager
import intellibitz.intellidroid.camera.Util
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class UriImage(
    private val mContainer: IImageList,
    private val mContentResolver: ContentResolver,
    private val mUri: Uri
) : IImage {

    companion object {
        private const val TAG = "UriImage"
    }

    override fun getDegreesRotated(): Int {
        return 0
    }

    override fun getDataPath(): String? {
        return mUri.path
    }

    private fun getInputStream(): InputStream? {
        return try {
            if (mUri.scheme == "file") {
                java.io.FileInputStream(mUri.path)
            } else {
                mContentResolver.openInputStream(mUri)
            }
        } catch (ex: FileNotFoundException) {
            null
        }
    }

    private fun getPFD(): ParcelFileDescriptor? {
        return try {
            if (mUri.scheme == "file") {
                val path = mUri.path
                ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                mContentResolver.openFileDescriptor(mUri, "r")
            }
        } catch (ex: FileNotFoundException) {
            null
        }
    }

    override fun fullSizeBitmap(minSideLength: Int, maxNumberOfPixels: Int): Bitmap? {
        return fullSizeBitmap(minSideLength, maxNumberOfPixels, ROTATE_AS_NEEDED, NO_NATIVE)
    }

    override fun fullSizeBitmap(
        minSideLength: Int,
        maxNumberOfPixels: Int,
        rotateAsNeeded: Boolean
    ): Bitmap? {
        return fullSizeBitmap(minSideLength, maxNumberOfPixels, rotateAsNeeded, NO_NATIVE)
    }

    override fun fullSizeBitmap(
        minSideLength: Int,
        maxNumberOfPixels: Int,
        rotateAsNeeded: Boolean,
        useNative: Boolean
    ): Bitmap? {
        return try {
            val pfdInput = getPFD()
            Util.makeBitmap(minSideLength, maxNumberOfPixels, pfdInput, useNative)
        } catch (ex: Exception) {
            Log.e(TAG, "got exception decoding bitmap ", ex)
            null
        }
    }

    override fun fullSizeImageUri(): Uri? {
        return mUri
    }

    override fun fullSizeImageData(): InputStream? {
        return getInputStream()
    }

    override fun miniThumbBitmap(): Bitmap? {
        return thumbBitmap(ROTATE_AS_NEEDED)
    }

    override fun getTitle(): String? {
        return mUri.toString()
    }

    override fun thumbBitmap(rotateAsNeeded: Boolean): Bitmap? {
        return fullSizeBitmap(THUMBNAIL_TARGET_SIZE, THUMBNAIL_MAX_NUM_PIXELS, rotateAsNeeded)
    }

    private fun snifBitmapOptions(): BitmapFactory.Options? {
        val input = getPFD() ?: return null
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapManager.instance().decodeFileDescriptor(input.fileDescriptor, options)
            options
        } finally {
            Util.closeSilently(input)
        }
    }

    override fun getMimeType(): String? {
        val options = snifBitmapOptions()
        return if (options != null && options.outMimeType != null) options.outMimeType else ""
    }

    override fun getHeight(): Int {
        val options = snifBitmapOptions()
        return options?.outHeight ?: 0
    }

    override fun getWidth(): Int {
        val options = snifBitmapOptions()
        return options?.outWidth ?: 0
    }

    override fun getContainer(): IImageList? {
        return mContainer
    }

    override fun getDateTaken(): Long {
        return 0
    }

    override fun isReadonly(): Boolean {
        return true
    }

    override fun isDrm(): Boolean {
        return false
    }

    override fun rotateImageBy(degrees: Int): Boolean {
        return false
    }
}
