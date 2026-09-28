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
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.util.Log
import intellibitz.intellidroid.camera.BitmapManager
import java.io.IOException
import java.io.InputStream

/**
 * Represents a particular video and provides access to the underlying data and
 * two thumbnail bitmaps as well as other information such as the id, and the
 * path to the actual video data.
 */
class VideoObject(
    container: BaseImageList,
    cr: ContentResolver,
    id: Long,
    index: Int,
    uri: Uri,
    dataPath: String,
    mimeType: String,
    dateTaken: Long,
    title: String
) : BaseImage(container, cr, id, index, uri, dataPath, mimeType, dateTaken, title), IImage {

    companion object {
        private const val TAG = "VideoObject"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is VideoObject) return false
        return fullSizeImageUri() == other.fullSizeImageUri()
    }

    override fun hashCode(): Int {
        return fullSizeImageUri().toString().hashCode()
    }

    override fun fullSizeBitmap(
        minSideLength: Int,
        maxNumberOfPixels: Int,
        rotateAsNeeded: Boolean,
        useNative: Boolean
    ): Bitmap? {
        return ThumbnailUtils.createVideoThumbnail(mDataPath, Video.Thumbnails.MINI_KIND)
    }

    override fun fullSizeImageData(): InputStream? {
        return try {
            mContentResolver.openInputStream(fullSizeImageUri())
        } catch (ex: IOException) {
            null
        }
    }

    override fun getHeight(): Int {
        return 0
    }

    override fun getWidth(): Int {
        return 0
    }

    fun isReadonly(): Boolean {
        return false
    }

    fun isDrm(): Boolean {
        return false
    }

    fun rotateImageBy(degrees: Int): Boolean {
        return false
    }

    fun thumbBitmap(rotateAsNeeded: Boolean): Bitmap? {
        return fullSizeBitmap(THUMBNAIL_TARGET_SIZE, THUMBNAIL_MAX_NUM_PIXELS)
    }

    override fun miniThumbBitmap(): Bitmap? {
        return try {
            val id = mId
            BitmapManager.instance().getThumbnail(mContentResolver, id, Images.Thumbnails.MICRO_KIND, null, true)
        } catch (ex: Throwable) {
            Log.e(TAG, "miniThumbBitmap got exception", ex)
            null
        }
    }

    override fun toString(): String {
        return StringBuilder("VideoObject").append(mId).toString()
    }
}
