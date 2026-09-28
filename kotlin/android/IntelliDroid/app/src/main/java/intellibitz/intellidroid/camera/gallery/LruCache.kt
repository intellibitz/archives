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

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.LinkedHashMap

class LruCache<K, V>(capacity: Int) {

    private val mLruMap: LinkedHashMap<K, V> = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
            return size > capacity
        }
    }

    private val mWeakMap = HashMap<K, Entry<K, V>>()
    private var mQueue = ReferenceQueue<V>()

    private fun cleanUpWeakMap() {
        var entry: Entry<K, V>? = mQueue.poll() as Entry<K, V>?
        while (entry != null) {
            mWeakMap.remove(entry.mKey)
            entry = mQueue.poll() as Entry<K, V>?
        }
    }

    @Synchronized
    fun put(key: K, value: V): V? {
        cleanUpWeakMap()
        mLruMap[key] = value
        val entry = mWeakMap.put(key, Entry(key, value, mQueue))
        return entry?.get()
    }

    @Synchronized
    fun get(key: K): V? {
        cleanUpWeakMap()
        val value = mLruMap[key]
        if (value != null) return value
        val entry = mWeakMap[key]
        return entry?.get()
    }

    @Synchronized
    fun clear() {
        mLruMap.clear()
        mWeakMap.clear()
        mQueue = ReferenceQueue()
    }

    private class Entry<K, V>(key: K, value: V, queue: ReferenceQueue<V>) : WeakReference<V>(value, queue) {
        val mKey: K = key
    }
}
