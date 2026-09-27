package intellibitz.intellidroid.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import androidx.annotation.NonNull;

@SuppressWarnings("UnusedDeclaration")
public class MediaPickerFile {

    private static final String TAG = "MediaPickerFile";

    /**
     * Create a file in the devices external storage.
     *
     * @param directory Target directory.
     * @param name      Target file name.
     * @param suffix    Target file suffix.
     * @return Created file.
     * @throws IOException
     */
    public static File
    createFileInES(final String directory, final String name, final String suffix) throws
            IOException {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        final File dir = createsDir(directory, externalStorageDirectory);
        return createsFile(name + suffix, dir);
    }

    public static File
    createFileInES(final String directory, final String name) throws
            IOException {
        final File dir = createsDir(directory, Environment.getExternalStorageDirectory());
        return createsFile(name, dir);
    }

    public static File createSoundFileInESPublicDir(String fileName, String suffix) throws
            IOException {
        return createTempFileInESPublicDir(Environment.DIRECTORY_PODCASTS, fileName, suffix);
    }

    public static File createImageFileInESPublicDir(String fileName, String suffix) throws
            IOException {
        return createTempFileInESPublicDir(Environment.DIRECTORY_PICTURES, fileName, suffix);
    }

    public static File createTempFileInESPublicDir(final String directory,
                                                   final String fileName,
                                                   final String suffix) throws IOException {
        String externalStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            File storageDir = Environment.getExternalStoragePublicDirectory(directory);
            return createsFile(fileName, suffix, storageDir);
        } else {
            Log.e(TAG, "File create FAIL: Storage state returns: " + externalStorageState);
        }
        return null;
    }

    /**
     * @see #createFileInES(String, String, String)
     */
    public static File create(final String directory, final String name) throws IOException {
        return createFileInES(directory, name, null);
    }

    /**
     * @see #create(String, String)
     */
    public static File create(final String directory) throws IOException {
        return create(directory, UUID.randomUUID().toString());
    }

    /**
     * @see #create(String)
     */
    public static File create() throws IOException {
        return create(MainApplicationSingleton.INTELLIBITZ_STORAGE_DIR);
    }

    /**
     * @see #createFileInES(String, String, String)
     */
    public static File createWithSuffix(final String suffix) throws IOException {
        return createFileInES(MainApplicationSingleton.INTELLIBITZ_STORAGE_DIR,
                UUID.randomUUID().toString(), suffix);
    }

    @NonNull
    public static File createsDir(String dir, File parentDir) throws IOException {
        return createsDir(parentDir + File.separator + dir);
    }

    @NonNull
    public static File createsDir(String dir) throws IOException {
        final File file = new File(dir);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            if (result) {
                Log.e(TAG, "createsDir: " + dir);
            } else {
                throw new IOException("createsDir: Unable to create directory." + dir);
            }
        }
        return file;
    }

    @NonNull
    public static File createsFile(String fileName, String suffix, File dir) throws IOException {
        return createsFile(fileName + suffix, dir);
    }

    @NonNull
    public static File createsFile(String fileName, File dir) throws IOException {
        File file = null;
        try {
            file = new File(dir, fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG, "createsFile: Unable to create file, does not exist." +
                            fileName + ":" + dir);
                }
            }
        } catch (IOException ignored) {
            Log.e(TAG, "createsFile: Unable to create file, does not exist." +
                    fileName + ":" + dir + " - " + ignored.getMessage());
        }
        return file;
    }

}
