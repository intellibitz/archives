package com.androidworks.navsys.wuffit

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import com.androidworks.navsys.wuffit.content.Tracker

class WuffITApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    fun storeTrackerId(id: String): Boolean {
        val uri = Uri.parse(id)
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        val cursor = try {
            contentResolver.query(uri, projection, null, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts: ${e.message}")
            null
        }

        if (cursor == null || cursor.count == 0) {
            cursor?.close()
            Log.e(TAG, "No Records found in database for tracker id: $id")
            Toast.makeText(this, "No Records found for tracker id: $id", Toast.LENGTH_LONG).show()
            return false
        }

        cursor.moveToFirst()
        val num = cursor.getString(0) ?: ""
        val name = cursor.getString(1) ?: ""
        cursor.close()

        val contentValues = ContentValues().apply {
            put(Tracker.Details.ID, id)
            put(Tracker.Details.NUMBER, num)
            put(Tracker.Details.NAME, name)
        }

        val detailsUri = contentResolver.insert(Tracker.Details.CONTENT_URI, contentValues) ?: return false
        Log.d(TAG, detailsUri.toString())

        val segment = detailsUri.pathSegments.getOrNull(1) ?: return false
        contentValues.clear()
        contentValues.put(Tracker.Setup._FID, segment)

        val setupUri = contentResolver.insert(Tracker.Setup.CONTENT_URI, contentValues)
        Log.d(TAG, setupUri.toString())

        val locUri = contentResolver.insert(Tracker.Locations.CONTENT_URI, contentValues)
        Log.d(TAG, locUri.toString())

        return true
    }

    fun syncTrackers() {
        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.ID),
            null,
            null,
            null
        ) ?: return

        cursor.use { c ->
            if (c.count > 0) {
                c.moveToFirst()
                do {
                    val u = c.getString(1)
                    val phoneCursor = try {
                        contentResolver.query(
                            Uri.parse(u),
                            arrayOf(
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            ),
                            null,
                            null,
                            null
                        )
                    } catch (e: Exception) {
                        null
                    }

                    if (phoneCursor == null || phoneCursor.count == 0) {
                        phoneCursor?.close()
                        Log.e(TAG, "No Records found in database for tracker id: $u")
                    } else {
                        phoneCursor.moveToFirst()
                        val num = phoneCursor.getString(0)
                        val name = phoneCursor.getString(1)
                        phoneCursor.close()

                        val contentValues = ContentValues().apply {
                            put(Tracker.Details.NUMBER, num)
                            put(Tracker.Details.NAME, name)
                        }
                        val where = Tracker.Details._ID + " = ? "
                        val whereArgs = arrayOf(c.getString(0))
                        val count = contentResolver.update(
                            Tracker.Details.CONTENT_URI,
                            contentValues,
                            where,
                            whereArgs
                        )
                        Log.d(TAG, "Updated: $count")
                        Toast.makeText(this, "Updated: $name", Toast.LENGTH_SHORT).show()
                    }
                } while (c.moveToNext())
            }
        }
    }

    fun deleteTracker(id: String) {
        val uri = ContentUris.withAppendedId(Tracker.Details.CONTENT_URI, id.toLong())
        var count = contentResolver.delete(uri, null, null)
        Log.d(TAG, "Deleted: $uri :count= $count")

        val where = Tracker.Locations._FID + " = ? "
        val whereArgs = arrayOf(id)

        count = contentResolver.delete(Tracker.Locations.CONTENT_URI, where, whereArgs)
        Log.d(TAG, "Deleted locations count= $count")

        count = contentResolver.delete(Tracker.Setup.CONTENT_URI, where, whereArgs)
        Log.d(TAG, "Deleted setup count= $count")
    }

    companion object {
        private const val TAG = "WuffITApplication"
    }
}
