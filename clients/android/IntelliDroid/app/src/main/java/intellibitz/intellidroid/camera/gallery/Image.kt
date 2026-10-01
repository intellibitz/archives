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
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore.Images
import android.provider.MediaStore.Images.ImageColumns
import android.util.Log
import intellibitz.intellidroid.camera.BitmapManager
import intellibitz.intellidroid.camera.Util
import java.io.IOException

/**
 * The class for normal images in gallery.
 */
class Image(
    container: BaseImageList,
    cr: ContentResolver,
    id: Long,
    index: Int,
    uri: Uri,
    dataPath: String,
    mimeType: String,
    dateTaken: Long,
    title: String,
    rotation: Int
) : BaseImage(container, cr, id, index, uri, dataPath, mimeType, dateTaken, title), IImage {

    companion object {
        private const val TAG = "BaseImage"
        private val THUMB_PROJECTION = arrayOf(
            BaseColumns._ID
        )
    }

    private var mExif: ExifInterface? = null
    var degreesRotated: Int = rotation
        private set

    override fun getDegreesRotated(): Int {
        return degreesRotated
    }

    protected fun setDegreesRotated(degrees: Int) {
        if (degreesRotated == degrees) return
        degreesRotated = degrees
        val values = ContentValues()
        values.put(ImageColumns.ORIENTATION, degreesRotated)
        mContentResolver.update(mUri, values, null, null)

        //TODO: Consider invalidate the cursor in container
        // ((BaseImageList) getContainer()).invalidateCursor();
    }

    fun isReadonly(): Boolean {
        val mimeType = getMimeType()
        return !"image/jpeg".equals(mimeType) && !"image/png".equals(mimeType)
    }

    fun isDrm(): Boolean {
        return false
    }

    /**
     * Replaces the tag if already there. Otherwise, adds to the exif tags.
     *
     * @param tag
     * @param value
     */
    fun replaceExifTag(tag: String, value: String) {
        if (mExif == null) {
            loadExifData()
        }
        mExif?.setAttribute(tag, value)
    }

    private fun loadExifData() {
        try {
            mExif = ExifInterface(mDataPath)
        } catch (ex: IOException) {
            Log.e(TAG, "cannot read exif", ex)
        }
    }

    @Throws(IOException::class)
    private fun saveExifData() {
        mExif?.saveAttributes()
    }

    private fun setExifRotation(degrees: Int) {
        try {
            var degrees = degrees % 360
            if (degrees < 0) degrees += 360

            var orientation = ExifInterface.ORIENTATION_NORMAL
            when (degrees) {
                0 -> orientation = ExifInterface.ORIENTATION_NORMAL
                90 -> orientation = ExifInterface.ORIENTATION_ROTATE_90
                180 -> orientation = ExifInterface.ORIENTATION_ROTATE_180
                270 -> orientation = ExifInterface.ORIENTATION_ROTATE_270
            }

            replaceExifTag(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveExifData()
        } catch (ex: Exception) {
            Log.e(TAG, "unable to save exif data with new orientation " + fullSizeImageUri(), ex)
        }
    }

    /**
     * Save the rotated image by updating the Exif "Orientation" tag.
     *
     * @param degrees
     */
    fun rotateImageBy(degrees: Int): Boolean {
        val newDegrees = (getDegreesRotated() + degrees) % 360
        setExifRotation(newDegrees)
        setDegreesRotated(newDegrees)

        return true
    }

    fun thumbBitmap(rotateAsNeeded: Boolean): Bitmap? {
        var bitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inDither = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmap = BitmapManager.instance().getThumbnail(
            mContentResolver, mId,
            Images.Thumbnails.MINI_KIND, options, false
        )

        if (bitmap != null && rotateAsNeeded) {
            bitmap = Util.rotate(bitmap, getDegreesRotated())
        }

        return bitmap
    }
}
