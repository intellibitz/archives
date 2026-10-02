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

import android.net.Uri
import intellibitz.intellidroid.camera.ImageManager
import intellibitz.intellidroid.camera.Util
import java.util.Arrays
import java.util.Comparator
import java.util.HashMap
import java.util.PriorityQueue

/**
 * A union of different <code>IImageList</code>. This class can merge several
 * <code>IImageList</code> into one list and sort them according to the
 * timestamp (The sorting must be same as all the given lists).
 */
class ImageListUber(private val sublist: Array<IImageList>, sort: Int) : IImageList {
    @Suppress("unused")
    private val TAG = "ImageListUber"

    private val mSubList: Array<IImageList> = sublist.clone()
    private val mQueue: PriorityQueue<MergeSlot> = PriorityQueue(4,
            if (sort == ImageManager.SORT_ASCENDING) AscendingComparator() else DescendingComparator())
    private var mSkipList: LongArray = LongArray(16)
    private var mSkipListSize: Int = 0
    private val mSkipCounts: IntArray = IntArray(mSubList.size)
    private var mLastListIndex: Int = -1

    init {
        mQueue.clear()
        for (i in mSubList.indices) {
            val list = mSubList[i]
            val slot = MergeSlot(list, i)
            if (slot.next()) mQueue.add(slot)
        }
    }

    override fun getBucketIds(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        for (list in mSubList) {
            hashMap.putAll(list.getBucketIds())
        }
        return hashMap
    }

    override fun getCount(): Int {
        var count = 0
        for (subList in mSubList) {
            count += subList.getCount()
        }
        return count
    }

    override fun isEmpty(): Boolean {
        for (subList in mSubList) {
            if (!subList.isEmpty()) return false
        }
        return true
    }

    // mSkipCounts is used to tally the counts as we traverse
    // the mSkipList.  It's a member variable only so that
    // we don't have to allocate each time through.  Otherwise
    // it could just as easily be a local.

    override fun getImageAt(index: Int): IImage? {
        if (index < 0 || index > getCount()) {
            throw IndexOutOfBoundsException(
                    "index $index out of range max is ${getCount()}")
        }

        val skipCounts = mSkipCounts
        // zero out the mSkipCounts since that's only used for the
        // duration of the function call.
        Arrays.fill(skipCounts, 0)

        // a counter of how many images we've skipped in
        // trying to get to index.  alternatively we could
        // have decremented index but, alas, I liked this
        // way more.
        var skipCount = 0

        // scan the existing mSkipList to see if we've computed
        // enough to just return the answer
        for (i in 0 until mSkipListSize) {
            val v = mSkipList[i]

            val offset = (v and 0xFFFFFFFF).toInt()
            val which = (v shr 32).toInt()
            if (skipCount + offset > index) {
                val subindex = mSkipCounts[which] + (index - skipCount)
                return mSubList[which].getImageAt(subindex)
            }
            skipCount += offset
            mSkipCounts[which] += offset
        }

        while (true) {
            val slot = nextMergeSlot() ?: return null
            if (skipCount == index) {
                val result = slot.mImage
                if (slot.next()) mQueue.add(slot)
                return result
            }
            if (slot.next()) mQueue.add(slot)
            skipCount++
        }
    }

    private fun nextMergeSlot(): MergeSlot? {
        val slot = mQueue.poll() ?: return null
        if (slot.mListIndex == mLastListIndex) {
            val lastIndex = mSkipListSize - 1
            ++mSkipList[lastIndex]
        } else {
            mLastListIndex = slot.mListIndex
            if (mSkipList.size == mSkipListSize) {
                val temp = LongArray(mSkipListSize * 2)
                System.arraycopy(mSkipList, 0, temp, 0, mSkipListSize)
                mSkipList = temp
            }
            mSkipList[mSkipListSize++] = ((mLastListIndex.toLong() shl 32) or 1)
        }
        return slot
    }

    override fun getImageForUri(uri: Uri): IImage? {
        for (sublist in mSubList) {
            val image = sublist.getImageForUri(uri)
            if (image != null) return image
        }
        return null
    }

    /**
     * Modify the skip list when an image is deleted by finding
     * the relevant entry in mSkipList and decrementing the
     * counter.  This is simple because deletion can never
     * cause change the order of images.
     */
    private fun modifySkipCountForDeletedImage(index: Int) {
        var skipCount = 0
        for (i in 0 until mSkipListSize) {
            val v = mSkipList[i]
            val offset = (v and 0xFFFFFFFF).toInt()
            if (skipCount + offset > index) {
                mSkipList[i] = v - 1
                break
            }
            skipCount += offset
        }
    }

    private fun removeImage(image: IImage, index: Int): Boolean {
        val list = image.getContainer()
        if (list != null && list.removeImage(image)) {
            modifySkipCountForDeletedImage(index)
            return true
        }
        return false
    }

    override fun removeImage(image: IImage): Boolean {
        return removeImage(image, getImageIndex(image))
    }

    override fun removeImageAt(index: Int): Boolean {
        val image = getImageAt(index) ?: return false
        return removeImage(image, index)
    }

    override fun getImageIndex(image: IImage): Int {
        val list = image.getContainer()
        val listIndex = Util.indexOf(mSubList, list)
        if (listIndex == -1) {
            throw IllegalArgumentException()
        }
        var listOffset = list.getImageIndex(image)

        // Similar algorithm as getImageAt(int index)
        var skipCount = 0
        for (i in 0 until mSkipListSize) {
            val value = mSkipList[i]
            val offset = (value and 0xFFFFFFFF).toInt()
            val which = (value shr 32).toInt()
            if (which == listIndex) {
                if (listOffset < offset) {
                    return skipCount + listOffset
                }
                listOffset -= offset
            }
            skipCount += offset
        }

        while (true) {
            val slot = nextMergeSlot() ?: return -1
            if (slot.mImage == image) {
                if (slot.next()) mQueue.add(slot)
                return skipCount
            }
            if (slot.next()) mQueue.add(slot)
            skipCount++
        }
    }

    override fun close() {
        for (i in mSubList.indices) {
            mSubList[i].close()
        }
    }

    private class DescendingComparator : Comparator<MergeSlot> {

        override fun compare(m1: MergeSlot, m2: MergeSlot): Int {
            if (m1.mDateTaken != m2.mDateTaken) {
                return if (m1.mDateTaken < m2.mDateTaken) 1 else -1
            }
            return m1.mListIndex - m2.mListIndex
        }
    }

    private class AscendingComparator : Comparator<MergeSlot> {

        override fun compare(m1: MergeSlot, m2: MergeSlot): Int {
            if (m1.mDateTaken != m2.mDateTaken) {
                return if (m1.mDateTaken < m2.mDateTaken) -1 else 1
            }
            return m1.mListIndex - m2.mListIndex
        }
    }

    /**
     * A merging slot is used to trace the current position of a sublist. For
     * each given sub list, there will be one corresponding merge slot. We
     * use merge-sort-like algorithm to build the merged list. At begining,
     * we put all the slots in a sorted heap (by timestamp). Each time, we
     * pop the slot with earliest timestamp out, get the image, and then move
     * the index forward, and put it back to the heap.
     */
    private class MergeSlot(val mList: IImageList, val mListIndex: Int) {
        var mDateTaken: Long = 0
        var mImage: IImage? = null
        private var mOffset = -1

        fun next(): Boolean {
            if (mOffset >= mList.getCount() - 1) return false
            mImage = mList.getImageAt(++mOffset)
            mDateTaken = mImage?.getDateTaken() ?: 0
            return true
        }
    }
}
