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
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore.Images.Media

/**
 * Represents an ordered collection of Image objects. Provides an API to add
 * and remove an image.
 */
class ImageList(resolver: ContentResolver, imageUri: Uri, sort: Int, bucketId: String?) : BaseImageList(resolver, imageUri, sort, bucketId), IImageList {

    companion object {
        val IMAGE_PROJECTION = arrayOf(
            Media._ID,
            Media.DATA,
            Media.DATE_TAKEN,
            Media.MINI_THUMB_MAGIC,
            Media.ORIENTATION,
            Media.TITLE,
            Media.MIME_TYPE,
            Media.DATE_MODIFIED
        )
        @Suppress("unused")
        private const val TAG = "ImageList"
        private val ACCEPTABLE_IMAGE_TYPES = arrayOf("image/jpeg", "image/png", "image/gif")
        private const val WHERE_CLAUSE = "(" + Media.MIME_TYPE + " in (?, ?, ?))"
        private const val WHERE_CLAUSE_WITH_BUCKET_ID = WHERE_CLAUSE + " AND " + Media.BUCKET_ID + " = ?"
        private const val INDEX_ID = 0
        private const val INDEX_DATA_PATH = 1
        private const val INDEX_DATE_TAKEN = 2
        private const val INDEX_MINI_THUMB_MAGIC = 3
        private const val INDEX_ORIENTATION = 4
        private const val INDEX_TITLE = 5
        private const val INDEX_MIME_TYPE = 6
        private const val INDEX_DATE_MODIFIED = 7
    }

    fun getBucketIds(): HashMap<String, String> {
        val uri = mBaseUri.buildUpon()
            .appendQueryParameter("distinct", "true").build()
        val cursor = Media.query(
            mContentResolver, uri,
            arrayOf(
                Media.BUCKET_DISPLAY_NAME,
                Media.BUCKET_ID
            ),
            whereClause(), whereClauseArgs(), null
        )
        return try {
            val hash = HashMap<String, String>()
            while (cursor.moveToNext()) {
                hash[cursor.getString(1)] = cursor.getString(0)
            }
            hash
        } finally {
            cursor.close()
        }
    }

    protected fun whereClause(): String {
        return if (mBucketId == null) WHERE_CLAUSE else WHERE_CLAUSE_WITH_BUCKET_ID
    }

    protected fun whereClauseArgs(): Array<String> {
        // TODO: Since mBucketId won't change, we should keep the array.
        if (mBucketId != null) {
            val count = ACCEPTABLE_IMAGE_TYPES.size
            val result = arrayOfNulls<String>(count + 1)
            System.arraycopy(ACCEPTABLE_IMAGE_TYPES, 0, result, 0, count)
            result[count] = mBucketId
            @Suppress("UNCHECKED_CAST")
            return result as Array<String>
        }
        return ACCEPTABLE_IMAGE_TYPES
    }

    override fun createCursor(): Cursor {
        return Media.query(
            mContentResolver, mBaseUri, IMAGE_PROJECTION,
            whereClause(), whereClauseArgs(), sortOrder()
        )
    }

    override fun getImageId(cursor: Cursor): Long {
        return cursor.getLong(INDEX_ID)
    }

    override fun loadImageFromCursor(cursor: Cursor): BaseImage {
        val id = cursor.getLong(INDEX_ID)
        val dataPath = cursor.getString(INDEX_DATA_PATH)
        var dateTaken = cursor.getLong(INDEX_DATE_TAKEN)
        if (dateTaken == 0L) {
            dateTaken = cursor.getLong(INDEX_DATE_MODIFIED) * 1000
        }
        val miniThumbMagic = cursor.getLong(INDEX_MINI_THUMB_MAGIC)
        val orientation = cursor.getInt(INDEX_ORIENTATION)
        var title = cursor.getString(INDEX_TITLE)
        val mimeType = cursor.getString(INDEX_MIME_TYPE)
        if (title == null || title.isEmpty()) {
            title = dataPath
        }
        return Image(this, mContentResolver, id, cursor.position,
            contentUri(id), dataPath, mimeType, dateTaken, title,
            orientation)
    }
}
