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
import android.net.Uri
import java.util.HashMap

/**
 * An implementation of interface [IImageList] which contains only
 * one image.
 */
class SingleImageList(resolver: ContentResolver, uri: Uri) : IImageList {

    @Suppress("unused")
    private val TAG = "BaseImageList"

    private var singleImage: IImage? = UriImage(this, resolver, uri)
    private var uri: Uri? = uri

    override fun getBucketIds(): HashMap<String, String> {
        throw UnsupportedOperationException()
    }

    override fun getCount(): Int = 1

    override fun isEmpty(): Boolean = false

    override fun getImageIndex(image: IImage): Int = if (image == singleImage) 0 else -1

    override fun getImageAt(i: Int): IImage? = if (i == 0) singleImage else null

    override fun removeImage(image: IImage): Boolean = false

    override fun removeImageAt(index: Int): Boolean = false

    override fun getImageForUri(uri: Uri): IImage? = if (uri == this.uri) singleImage else null

    override fun close() {
        singleImage = null
        uri = null
    }
}
