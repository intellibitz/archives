package intellibitz.intellidroid.util

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.UUID

object MediaPickerFile {
    private const val TAG = "MediaPickerFile"

    @JvmStatic
    @Throws(IOException::class)
    fun createFileInES(directory: String, name: String, suffix: String?): File {
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val dir = createsDir(directory, externalStorageDirectory)
        return createsFile(name + (suffix ?: ""), dir)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createFileInES(directory: String, name: String): File {
        val dir = createsDir(directory, Environment.getExternalStorageDirectory())
        return createsFile(name, dir)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createSoundFileInESPublicDir(fileName: String, suffix: String): File? {
        return createTempFileInESPublicDir(Environment.DIRECTORY_PODCASTS, fileName, suffix)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createImageFileInESPublicDir(fileName: String, suffix: String): File? {
        return createTempFileInESPublicDir(Environment.DIRECTORY_PICTURES, fileName, suffix)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createTempFileInESPublicDir(
        directory: String,
        fileName: String,
        suffix: String
    ): File? {
        val externalStorageState = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == externalStorageState) {
            val storageDir = Environment.getExternalStoragePublicDirectory(directory)
            return createsFile(fileName, suffix, storageDir)
        } else {
            Log.e(TAG, "File create FAIL: Storage state returns: $externalStorageState")
        }
        return null
    }

    @JvmStatic
    @Throws(IOException::class)
    fun create(directory: String, name: String): File {
        return createFileInES(directory, name, null)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun create(directory: String): File {
        return create(directory, UUID.randomUUID().toString())
    }

    @JvmStatic
    @Throws(IOException::class)
    fun create(): File {
        return create(MainApplicationSingleton.INTELLIBITZ_STORAGE_DIR)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createWithSuffix(suffix: String): File {
        return createFileInES(
            MainApplicationSingleton.INTELLIBITZ_STORAGE_DIR,
            UUID.randomUUID().toString(), suffix
        )
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createsDir(dir: String, parentDir: File): File {
        return createsDir(parentDir.toString() + File.separator + dir)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createsDir(dir: String): File {
        val file = File(dir)
        if (!file.exists()) {
            val result = file.mkdirs()
            if (result) {
                Log.e(TAG, "createsDir: $dir")
            } else {
                throw IOException("createsDir: Unable to create directory.$dir")
            }
        }
        return file
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createsFile(fileName: String, suffix: String?, dir: File): File {
        return createsFile(fileName + (suffix ?: ""), dir)
    }

    @JvmStatic
    fun createsFile(fileName: String, dir: File): File {
        var file: File? = null
        try {
            file = File(dir, fileName)
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(
                        TAG, "createsFile: Unable to create file, does not exist." +
                                fileName + ":" + dir
                    )
                }
            }
        } catch (ignored: IOException) {
            Log.e(
                TAG, "createsFile: Unable to create file, does not exist." +
                        fileName + ":" + dir + " - " + ignored.message
            )
        }
        return file ?: File(dir, fileName)
    }
}
