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

package intellibitz.intellidroid.camera

import android.graphics.Bitmap
import android.graphics.Matrix

class RotateBitmap(bitmap: Bitmap, rotation: Int = 0) {
    companion object {
        const val TAG = "RotateBitmap"
    }

    var rotation = rotation % 360
        set(value) {
            field = value % 360
        }

    var bitmap: Bitmap? = bitmap

    val rotateMatrix: Matrix
        get() {
            // By default this is an identity matrix.
            val matrix = Matrix()
            if (rotation != 0) {
                // We want to do the rotation at origin, but since the bounding
                // rectangle will be changed after rotation, so the delta values
                // are based on old & new width/height respectively.
                val cx = bitmap?.width?.div(2) ?: 0
                val cy = bitmap?.height?.div(2) ?: 0
                matrix.preTranslate(-cx.toFloat(), -cy.toFloat())
                matrix.postRotate(rotation.toFloat())
                matrix.postTranslate(width / 2f, height / 2f)
            }
            return matrix
        }

    val isOrientationChanged: Boolean
        get() = (rotation / 90) % 2 != 0

    val height: Int
        get() = if (isOrientationChanged) bitmap?.width ?: 0 else bitmap?.height ?: 0

    val width: Int
        get() = if (isOrientationChanged) bitmap?.height ?: 0 else bitmap?.width ?: 0

    fun recycle() {
        bitmap?.recycle()
        bitmap = null
    }
}
