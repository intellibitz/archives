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
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.util.Log
import intellibitz.intellidroid.camera.ImageManager
import intellibitz.intellidroid.camera.Util
import java.util.regex.Pattern

/**
 * A collection of <code>BaseImage</code>s.
 */
abstract class BaseImageList(
    resolver: ContentResolver,
    uri: Uri,
    sort: Int,
    bucketId: String?
) : IImageList {
    companion object {
        private const val TAG = "BaseImageList"
        private const val CACHE_CAPACITY = 512
        private val sPathWithId = Pattern.compile("(.*)/\\d+")

        private fun getPathWithoutId(uri: Uri): String {
            val path = uri.path
            val matcher = sPathWithId.matcher(path)
            return if (matcher.matches()) matcher.group(1) else path
        }
    }

    private val mCache = LruCache<Integer, BaseImage>(CACHE_CAPACITY)
    protected var mContentResolver: ContentResolver? = null
    protected var mSort: Int = 0
    protected var mBaseUri: Uri? = null
    protected var mCursor: Cursor? = null
    protected var mBucketId: String? = null
    protected var mCursorDeactivated = false

    init {
        mSort = sort
        mBaseUri = uri
        mBucketId = bucketId
        mContentResolver = resolver
        mCursor = createCursor()

        if (mCursor == null) {
            Log.w(TAG, "createCursor returns null.")
        }

        // TODO: We need to clear the cache because we may "reopen" the image
        // list. After we implement the image list state, we can remove this
        // kind of usage.
        mCache.clear()
    }

    fun close() {
        try {
            invalidateCursor()
        } catch (e: IllegalStateException) {
            // IllegalStateException may be thrown if the cursor is stale.
            Log.e(TAG, "Caught exception while deactivating cursor.", e)
        }
        mContentResolver = null
        mCursor?.close()
        mCursor = null
    }

    // TODO: Change public to protected
    fun contentUri(id: Long): Uri? {
        // TODO: avoid using exception for most cases
        try {
            // does our uri already have an id (single image query)?
            // if so just return it
            val existingId = ContentUris.parseId(mBaseUri)
            if (existingId != id) Log.e(TAG, "id mismatch")
            return mBaseUri
        } catch (ex: NumberFormatException) {
            // otherwise tack on the id
            return ContentUris.withAppendedId(mBaseUri, id)
        }
    }

    override fun getCount(): Int {
        val cursor = getCursor() ?: return 0
        synchronized(this) {
            return cursor.count
        }
    }

    override fun isEmpty(): Boolean {
        return getCount() == 0
    }

    private fun getCursor(): Cursor? {
        synchronized(this) {
            if (mCursor == null) return null
            if (mCursorDeactivated) {
                mCursor!!.requery()
                mCursorDeactivated = false
            }
            return mCursor
        }
    }

    override fun getImageAt(i: Int): IImage? {
        var result = mCache.get(i)
        if (result == null) {
            val cursor = getCursor() ?: return null
            synchronized(this) {
                result = if (cursor.moveToPosition(i)) loadImageFromCursor(cursor) else null
                mCache.put(i, result)
            }
        }
        return result
    }

    override fun removeImage(image: IImage): Boolean {
        // TODO: need to delete the thumbnails as well
        if (mContentResolver!!.delete(image.fullSizeImageUri(), null, null) > 0) {
            (image as BaseImage).onRemove()
            invalidateCursor()
            invalidateCache()
            return true
        } else {
            return false
        }
    }

    override fun removeImageAt(i: Int): Boolean {
        // TODO: need to delete the thumbnails as well
        return removeImage(getImageAt(i)!!)
    }

    protected abstract fun createCursor(): Cursor?

    protected abstract fun loadImageFromCursor(cursor: Cursor): BaseImage?

    protected abstract fun getImageId(cursor: Cursor): Long

    protected fun invalidateCursor() {
        mCursor?.deactivate()
        mCursorDeactivated = true
    }

    protected fun invalidateCache() {
        mCache.clear()
    }

    private fun isChildImageUri(uri: Uri): Boolean {
        // Sometimes, the URI of an image contains a query string with key
        // "bucketId" inorder to restore the image list. However, the query
        // string is not part of the mBaseUri. So, we check only other parts
        // of the two Uri to see if they are the same.
        val base = mBaseUri
        return Util.equals(base!!.scheme, uri.scheme)
                && Util.equals(base.host, uri.host)
                && Util.equals(base.authority, uri.authority)
                && Util.equals(base.path, getPathWithoutId(uri))
    }

    override fun getImageForUri(uri: Uri): IImage? {
        if (!isChildImageUri(uri)) return null
        // Find the id of the input URI.
        val matchId: Long
        try {
            matchId = ContentUris.parseId(uri)
        } catch (ex: NumberFormatException) {
            Log.i(TAG, "fail to get id in: $uri", ex)
            return null
        }
        // TODO: design a better method to get URI of specified ID
        val cursor = getCursor() ?: return null
        synchronized(this) {
            cursor.moveToPosition(-1) // before first
            for (i in 0 until cursor.count) {
                if (cursor.moveToNext()) {
                    if (getImageId(cursor) == matchId) {
                        var image = mCache.get(i)
                        if (image == null) {
                            image = loadImageFromCursor(cursor)
                            mCache.put(i, image)
                        }
                        return image
                    }
                }
            }
            return null
        }
    }

    override fun getImageIndex(image: IImage): Int {
        return (image as BaseImage).mIndex
    }

    // This provides a default sorting order string for subclasses.
    // The list is first sorted by date, then by id. The order can be ascending
    // or descending, depending on the mSort variable.
    // The date is obtained from the "datetaken" column. But if it is null,
    // the "date_modified" column is used instead.
    protected fun sortOrder(): String {
        val ascending =
            if (mSort == ImageManager.SORT_ASCENDING)
                " ASC"
            else
                " DESC"

        // Use DATE_TAKEN if it's non-null, otherwise use DATE_MODIFIED.
        // DATE_TAKEN is in milliseconds, but DATE_MODIFIED is in seconds.
        val dateExpr =
            "case ifnull(datetaken,0)" +
                    " when 0 then date_modified*1000" +
                    " else datetaken" +
                    " end"

        // Add id to the end so that we don't ever get random sorting
        // which could happen, I suppose, if the date values are the same.
        return "$dateExpr$ascending, _id$ascending"
    }
}
