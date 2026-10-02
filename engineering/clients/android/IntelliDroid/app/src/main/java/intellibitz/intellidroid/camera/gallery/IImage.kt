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

import android.graphics.Bitmap
import android.net.Uri
import java.io.InputStream

/**
 * The interface of all images used in gallery.
 */
interface IImage {
    companion object {
        const val THUMBNAIL_TARGET_SIZE = 320
        const val MINI_THUMB_TARGET_SIZE = 96
        const val THUMBNAIL_MAX_NUM_PIXELS = 512 * 384
        const val MINI_THUMB_MAX_NUM_PIXELS = 128 * 128
        const val UNCONSTRAINED = -1
        const val ROTATE_AS_NEEDED = true
        const val NO_ROTATE = false
        const val USE_NATIVE = true
        const val NO_NATIVE = false
    }

    /**
     * Get the image list which contains this image.
     */
    fun getContainer(): IImageList?

    /**
     * Get the bitmap for the full size image.
     */
    fun fullSizeBitmap(minSideLength: Int, maxNumberOfPixels: Int): Bitmap?

    fun fullSizeBitmap(minSideLength: Int, maxNumberOfPixels: Int, rotateAsNeeded: Boolean, useNative: Boolean): Bitmap?

    fun getDegreesRotated(): Int

    /**
     * Get the input stream associated with a given full size image.
     */
    fun fullSizeImageData(): InputStream?

    fun fullSizeImageUri(): Uri?

    /**
     * Get the path of the (full size) image data.
     */
    fun getDataPath(): String?

    // Get the title of the image
    fun getTitle(): String?

    // Get metadata of the image
    fun getDateTaken(): Long

    fun getMimeType(): String?

    fun getWidth(): Int

    fun getHeight(): Int

    // Get property of the image
    fun isReadonly(): Boolean

    fun isDrm(): Boolean

    // Get the bitmap of the medium thumbnail
    fun thumbBitmap(rotateAsNeeded: Boolean): Bitmap?

    // Get the bitmap of the mini thumbnail.
    fun miniThumbBitmap(): Bitmap?

    // Rotate the image
    fun rotateImageBy(degrees: Int): Boolean
}
