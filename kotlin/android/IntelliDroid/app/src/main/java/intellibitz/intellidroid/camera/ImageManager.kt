/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import intellibitz.intellidroid.camera.gallery.BaseImageList
import intellibitz.intellidroid.camera.gallery.IImage
import intellibitz.intellidroid.camera.gallery.IImageList
import intellibitz.intellidroid.camera.gallery.ImageList
import intellibitz.intellidroid.camera.gallery.ImageListUber
import intellibitz.intellidroid.camera.gallery.SingleImageList
import intellibitz.intellidroid.camera.gallery.VideoList
import intellibitz.intellidroid.camera.gallery.VideoObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap

/**
 * ImageManager is used to retrieve and store images
 * in the media content provider.
 */
class ImageManager {
    // Inclusion
    companion object {
        const val INCLUDE_IMAGES = 1 shl 0
        const val INCLUDE_VIDEOS = 1 shl 1
        // Sort
        const val SORT_ASCENDING = 1
        const val SORT_DESCENDING = 2
        val CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
        val CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME)
        private const val TAG = "ImageManager"
        private val STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI
        private val THUMB_URI = Images.Thumbnails.EXTERNAL_CONTENT_URI
        private val VIDEO_STORAGE_URI = Uri.parse("content://media/external/video/media")

        /**
         * Matches code in MediaProvider.computeBucketValues. Should be a common
         * function.
         */
        fun getBucketId(path: String): String {
            return path.toLowerCase().hashCode().toString()
        }

        /**
         * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
         * imported. This is a temporary fix for bug#1655552.
         */
        fun ensureOSXCompatibleFolder() {
            val nnnAAAAA = File(Environment.getExternalStorageDirectory().toString() + "/DCIM/100ANDRO")
            if (!nnnAAAAA.exists() && !nnnAAAAA.mkdir()) {
                Log.e(TAG, "createFileInES NNNAAAAA file: " + nnnAAAAA.path + " failed")
            }
        }

        /**
         * @return true if the mimetype is an image mimetype.
         */
        fun isImageMimeType(mimeType: String): Boolean {
            return mimeType.startsWith("image/")
        }

        /**
         * @return true if the image is an image.
         */
        fun isImage(image: IImage): Boolean {
            return isImageMimeType(image.mimeType)
        }

        /**
         * @return true if the image is a video.
         */
        fun isVideo(image: IImage): Boolean {
            // This is the right implementation, but we use instanceof for speed.
            //return isVideoMimeType(image.getMimeType());
            return image is VideoObject
        }

        /**
         * @return true if the mimetype is a video mimetype.
         */
        /* This is commented out because isVideo is not calling this now.
        public static boolean isVideoMimeType(String mimeType) {
            return mimeType.startsWith("video/");
        }
        */

        //
        // Stores a bitmap or a jpeg byte array to a file (using the specified
        // directory and filename). Also add an entry to the media store for
        // this picture. The title, dateTaken, location are attributes for the
        // picture. The degree is a one element array which returns the orientation
        // of the picture.
        //
        fun addImage(cr: ContentResolver, title: String, dateTaken: Long, location: Location?, directory: String, filename: String, source: Bitmap?, jpegData: ByteArray, degree: IntArray): Uri? {
            // We should store image data earlier than insert it to ContentProvider, otherwise
            // we may not be able to generate thumbnail in time.
            var outputStream: OutputStream? = null
            val filePath = "$directory/$filename"
            try {
                val dir = File(directory)
                if (!dir.exists()) dir.mkdirs()
                val file = File(directory, filename)
                outputStream = FileOutputStream(file)
                if (source != null) {
                    source.compress(CompressFormat.JPEG, 75, outputStream)
                    degree[0] = 0
                } else {
                    outputStream.write(jpegData)
                    degree[0] = getExifOrientation(filePath)
                }
            } catch (ex: FileNotFoundException) {
                Log.w(TAG, ex)
                return null
            } catch (ex: IOException) {
                Log.w(TAG, ex)
                return null
            } finally {
                Util.closeSilently(outputStream)
            }

            val values = ContentValues(7)
            values.put(Images.Media.TITLE, title)

            // That filename is what will be handed to Gmail when a user shares a
            // photo. Gmail gets the name of the picture attachment from the
            // "DISPLAY_NAME" field.
            values.put(Images.Media.DISPLAY_NAME, filename)
            values.put(Images.Media.DATE_TAKEN, dateTaken)
            values.put(Images.Media.MIME_TYPE, "image/jpeg")
            values.put(Images.Media.ORIENTATION, degree[0])
            values.put(Images.Media.DATA, filePath)

            if (location != null) {
                values.put(Images.Media.LATITUDE, location.latitude)
                values.put(Images.Media.LONGITUDE, location.longitude)
            }

            return cr.insert(STORAGE_URI, values)
        }

        fun getExifOrientation(filepath: String): Int {
            var degree = 0
            var exif: ExifInterface? = null
            try {
                exif = ExifInterface(filepath)
            } catch (ex: IOException) {
                Log.e(TAG, "cannot read exif", ex)
            }
            if (exif != null) {
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
                if (orientation != -1) {
                    // We only recognize a subset of orientation tag values.
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                    }
                }
            }
            return degree
        }

        // This is the factory function to createFileInES an image list.
        fun makeImageList(cr: ContentResolver, param: ImageListParam): IImageList {
            val location = param.mLocation
            val inclusion = param.mInclusion
            val sort = param.mSort
            val bucketId = param.mBucketId
            val singleImageUri = param.mSingleImageUri
            val isEmptyImageList = param.mIsEmptyImageList

            if (isEmptyImageList || cr == null) {
                return EmptyImageList()
            }

            if (singleImageUri != null) {
                return SingleImageList(cr, singleImageUri)
            }

            // false ==> don't require write access
            val haveSdCard = hasStorage(false)

            // use this code to merge videos and stills into the same list
            val l = ArrayList<BaseImageList>()

            if (haveSdCard && location != DataLocation.INTERNAL) {
                if (inclusion and INCLUDE_IMAGES != 0) {
                    l.add(ImageList(cr, STORAGE_URI, sort, bucketId))
                }
                if (inclusion and INCLUDE_VIDEOS != 0) {
                    l.add(VideoList(cr, VIDEO_STORAGE_URI, sort, bucketId))
                }
            }
            if (location == DataLocation.INTERNAL || location == DataLocation.ALL) {
                if (inclusion and INCLUDE_IMAGES != 0) {
                    l.add(ImageList(cr, Images.Media.INTERNAL_CONTENT_URI, sort, bucketId))
                }
            }

            // Optimization: If some of the lists are empty, remove them.
            // If there is only one remaining list, return it directly.
            val iter = l.iterator()
            while (iter.hasNext()) {
                val sublist = iter.next()
                if (sublist.isEmpty) {
                    sublist.close()
                    iter.remove()
                }
            }

            if (l.size == 1) {
                val list = l[0]
                return list
            }

            val uber = ImageListUber(l.toTypedArray(), sort)
            return uber
        }

        // This is a convenience function to createFileInES an image list from a Uri.
        fun makeImageList(cr: ContentResolver, uri: Uri, sort: Int): IImageList {
            val uriString = uri?.toString() ?: ""

            if (uriString.startsWith("content://media/external/video")) {
                return makeImageList(cr, DataLocation.EXTERNAL, INCLUDE_VIDEOS, sort, null)
            } else if (isSingleImageMode(uriString)) {
                return makeSingleImageList(cr, uri)
            } else {
                val bucketId = uri.getQueryParameter("bucketId")
                return makeImageList(cr, DataLocation.ALL, INCLUDE_IMAGES, sort, bucketId)
            }
        }

        fun isSingleImageMode(uriString: String): Boolean {
            return !uriString.startsWith(Images.Media.EXTERNAL_CONTENT_URI.toString()) && !uriString.startsWith(Images.Media.INTERNAL_CONTENT_URI.toString())
        }

        fun getImageListParam(location: DataLocation, inclusion: Int, sort: Int, bucketId: String?): ImageListParam {
            val param = ImageListParam()
            param.mLocation = location
            param.mInclusion = inclusion
            param.mSort = sort
            param.mBucketId = bucketId
            return param
        }

        fun getSingleImageListParam(uri: Uri): ImageListParam {
            val param = ImageListParam()
            param.mSingleImageUri = uri
            return param
        }

        fun getEmptyImageListParam(): ImageListParam {
            val param = ImageListParam()
            param.mIsEmptyImageList = true
            return param
        }

        fun makeImageList(cr: ContentResolver, location: DataLocation, inclusion: Int, sort: Int, bucketId: String?): IImageList {
            val param = getImageListParam(location, inclusion, sort, bucketId)
            return makeImageList(cr, param)
        }

        fun makeEmptyImageList(): IImageList {
            return makeImageList(null, getEmptyImageListParam())
        }

        fun makeSingleImageList(cr: ContentResolver, uri: Uri): IImageList {
            return makeImageList(cr, getSingleImageListParam(uri))
        }

        private fun checkFsWritable(): Boolean {
            // Create a temporary file to see whether a volume is really writeable.
            // It's important not to put it in the root directory which may have a
            // limit on the number of files.
            val directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM"
            val directory = File(directoryName)
            if (!directory.isDirectory) {
                if (!directory.mkdirs()) {
                    return false
                }
            }
            val f = File(directoryName, ".probe")
            try {
                // Remove stale file if any
                if (f.exists()) {
                    f.delete()
                }
                if (!f.createNewFile()) {
                    return false
                }
                f.delete()
                return true
            } catch (ex: IOException) {
                return false
            }
        }

        fun hasStorage(): Boolean {
            return hasStorage(true)
        }

        fun hasStorage(requireWriteAccess: Boolean): Boolean {
            val state = Environment.getExternalStorageState()

            if (Environment.MEDIA_MOUNTED == state) {
                if (requireWriteAccess) {
                    val writable = checkFsWritable()
                    return writable
                } else {
                    return true
                }
            } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY == state) {
                return true
            }
            return false
        }

        private fun query(resolver: ContentResolver, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
            try {
                if (resolver == null) {
                    return null
                }
                return resolver.query(uri, projection, selection, selectionArgs, sortOrder)
            } catch (ex: UnsupportedOperationException) {
                return null
            }
        }

        fun isMediaScannerScanning(cr: ContentResolver): Boolean {
            var result = false
            val cursor = query(cr, MediaStore.getMediaScannerUri(), arrayOf(MediaStore.MEDIA_SCANNER_VOLUME), null, null, null)
            if (cursor != null) {
                if (cursor.count == 1) {
                    result = "external" == cursor.getString(0)
                }
                cursor.close()
            }

            return result
        }
    }

    // Location
    enum class DataLocation {
        NONE, INTERNAL, EXTERNAL, ALL
    }

    // ImageListParam specifies all the parameters we need to createFileInES an image
    // list (we also need a ContentResolver).
    class ImageListParam : Parcelable {
        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<ImageListParam> {
                override fun createFromParcel(`in`: Parcel): ImageListParam {
                    return ImageListParam(`in`)
                }

                override fun newArray(size: Int): Array<ImageListParam?> {
                    return arrayOfNulls(size)
                }
            }
        }

        var mLocation: DataLocation? = null
        var mInclusion = 0
        var mSort = 0
        var mBucketId: String? = null
        // This is only used if we are creating a single image list.
        var mSingleImageUri: Uri? = null
        // This is only used if we are creating an empty image list.
        var mIsEmptyImageList = false

        constructor()

        private constructor(`in`: Parcel) {
            mLocation = DataLocation.values()[`in`.readInt()]
            mInclusion = `in`.readInt()
            mSort = `in`.readInt()
            mBucketId = `in`.readString()
            mSingleImageUri = `in`.readParcelable(null)
            mIsEmptyImageList = `in`.readInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeInt(mLocation!!.ordinal)
            out.writeInt(mInclusion)
            out.writeInt(mSort)
            out.writeString(mBucketId)
            out.writeParcelable(mSingleImageUri, flags)
            out.writeInt(if (mIsEmptyImageList) 1 else 0)
        }

        override fun toString(): String {
            return String.format("ImageListParam{loc=%s,inc=%d,sort=%d," + "bucket=%s,empty=%b,single=%s}", mLocation, mInclusion, mSort, mBucketId, mIsEmptyImageList, mSingleImageUri)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    private class EmptyImageList : IImageList {
        override fun close() {
        }

        override fun getBucketIds(): HashMap<String, String> {
            return HashMap()
        }

        override fun getCount(): Int {
            return 0
        }

        override fun isEmpty(): Boolean {
            return true
        }

        override fun getImageAt(i: Int): IImage? {
            return null
        }

        override fun getImageForUri(uri: Uri): IImage? {
            return null
        }

        override fun removeImage(image: IImage): Boolean {
            return false
        }

        override fun removeImageAt(i: Int): Boolean {
            return false
        }

        override fun getImageIndex(image: IImage): Int {
            throw UnsupportedOperationException()
        }
    }
}
