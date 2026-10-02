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
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video.Media

/**
 * A collection of all the [VideoObject] in gallery.
 */
class VideoList(
    resolver: ContentResolver,
    uri: Uri,
    sort: Int,
    bucketId: String?
) : BaseImageList(resolver, uri, sort, bucketId) {

    companion object {
        @Suppress("unused")
        private const val TAG = "BaseImageList"

        private val VIDEO_PROJECTION = arrayOf(
            Media._ID,
            Media.DATA,
            Media.DATE_TAKEN,
            Media.TITLE,
            Media.MINI_THUMB_MAGIC,
            Media.MIME_TYPE,
            Media.DATE_MODIFIED
        )

        private const val INDEX_ID = 0
        private const val INDEX_DATA_PATH = 1
        private const val INDEX_DATE_TAKEN = 2
        private const val INDEX_TITLE = 3
        private const val INDEX_MIMI_THUMB_MAGIC = 4
        private const val INDEX_MIME_TYPE = 5
        private const val INDEX_DATE_MODIFIED = 6
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
        val miniThumbMagic = cursor.getLong(INDEX_MIMI_THUMB_MAGIC)
        var title = cursor.getString(INDEX_TITLE)
        val mimeType = cursor.getString(INDEX_MIME_TYPE)
        if (title == null || title.isEmpty()) {
            title = dataPath
        }
        return VideoObject(
            this, mContentResolver,
            id, cursor.position, contentUri(id), dataPath,
            mimeType, dateTaken, title
        )
    }

    fun getBucketIds(): HashMap<String, String> {
        val uri = mBaseUri.buildUpon()
            .appendQueryParameter("distinct", "true").build()
        val c = Images.Media.query(
            mContentResolver, uri,
            arrayOf(
                Media.BUCKET_DISPLAY_NAME,
                Media.BUCKET_ID
            ),
            whereClause(), whereClauseArgs(), sortOrder()
        )
        return try {
            val hash = HashMap<String, String>()
            while (c.moveToNext()) {
                hash[c.getString(1)] = c.getString(0)
            }
            hash
        } finally {
            c.close()
        }
    }

    protected fun whereClause(): String? {
        return if (mBucketId != null) {
            Images.Media.BUCKET_ID + " = '" + mBucketId + "'"
        } else {
            null
        }
    }

    protected fun whereClauseArgs(): Array<String>? {
        return null
    }

    override fun createCursor(): Cursor {
        return Images.Media.query(
            mContentResolver, mBaseUri, VIDEO_PROJECTION,
            whereClause(), whereClauseArgs(), sortOrder()
        )
    }
}
