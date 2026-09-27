package intellibitz.intellidroid.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File chooser library: https://github.com/iPaulPro/aFileChooser
 */
@SuppressWarnings("UnusedDeclaration")
public class MediaPickerUri {

    private static final String TAG = "MediaPickerUri";

    private static final String AUTHORITY_GOOGLE_PHOTOS =
            "com.google.android.apps.photos.content";
    private static final String AUTHORITY_EXTERNAL_STORAGE =
            "com.android.externalstorage.documents";
    private static final String AUTHORITY_DOWNLOADS_DOCUMENT =
            "com.android.providers.downloads.documents";
    private static final String AUTHORITY_MEDIA_DOCUMENT =
            "com.android.providers.media.documents";
    private static final String AUTHORITY_GOOGLE_PHOTOS_CONTENTPROVIDER =
            "com.google.android.apps.photos.contentprovider";
    private static final String AUTHORITY_GOOGLE_DOCS_STORAGE =
            "com.google.android.apps.docs.storage";

    /**
     * Convert a Uri into a file if possible.
     *
     * @param context Android application or activity context.
     * @param uri     Source Uri.
     * @return Associated file on device.
     * @throws IOException
     * @see #getPath(Context, Uri)
     */
    public static File resolveToFile(Context context, Uri uri) throws IOException {
        if (context == null) {
            throw new IOException("A valid android application context is required.");
        }
        if (uri == null) {
            throw new IOException("File URI cannot be null.");
        }
        String path = null;
//        // TODO: 27-02-2016
//        new content uri, to work with google photos
        if (AUTHORITY_GOOGLE_DOCS_STORAGE.equals(uri.getAuthority())) {
            throw new IOException("Google Docs Storage not supported - Future release only");
        }
//        // TODO: 27-02-2016
//        new content uri, to work with google photos
        if (AUTHORITY_GOOGLE_PHOTOS_CONTENTPROVIDER.equals(uri.getAuthority())) {
            String imageUrlWithAuthority = getImageUrlWithAuthority(context, uri);
            if (imageUrlWithAuthority != null)
                path = getPath(context, Uri.parse(imageUrlWithAuthority));
        }
//        // TODO: 14-06-2016
//        this must be done last, to support "/" local storage..
        if (path == null) {
            path = getPath(context, uri);
        }
        if (path == null) {
//            tries local
            path = uri.toString();
        }
        if (path == null) {
            throw new IOException("File path was not found.");
        }
        if (!isLocal(path)) {
            throw new IOException("File path was found, but path must be a local URI.");
        }
        final File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File path was found, but file does not exist." +
                    file.getAbsolutePath());
        }
        return new File(path);
    }

    /**
     * @see #resolveToFile(Context, Uri)
     */
    public static File resolveToFile(final Context context, final Intent intentUri, final String name) throws IOException {

        final Uri uri = intentUri.getParcelableExtra(name);

        return resolveToFile(context, uri);
    }

    /**
     * @see #resolveToFile(Context, Uri)
     */
    public static File resolveToFile(final Context context, final Intent intentUri) throws IOException {

        return resolveToFile(context, intentUri, Intent.EXTRA_STREAM);
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     * <p/>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     */
    private static String getPath(final Context context, final Uri uri) throws IOException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {

                if (uri.getAuthority() == null) {

                    throw new IOException("Uri authority cannot be null.");
                }

                final String documentId = DocumentsContract.getDocumentId(uri);

                switch (uri.getAuthority()) {

                    case AUTHORITY_EXTERNAL_STORAGE: {

                        final String[] split = documentId.split(":");
                        final String type = split[0];

                        if ("primary".equalsIgnoreCase(type)) {

                            return Environment.getExternalStorageDirectory() + "/" + split[1];
                        }

                        throw new IOException("Unable to handle non-primary external storage volumes.");
                    }
                    case AUTHORITY_DOWNLOADS_DOCUMENT: {

                        final Uri contentUri = Uri.parse("content://downloads/public_downloads");
                        final Uri contentUriAppended = ContentUris.withAppendedId(contentUri, Long.valueOf(documentId));

                        return getDataColumn(context, contentUriAppended, null, null);
                    }
                    case AUTHORITY_MEDIA_DOCUMENT: {

                        final String[] split = documentId.split(":");
                        final String type = split[0];

                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[]{
                                split[1]
                        };

                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                    default:

                        throw new IOException("Unknown URI document authority encountered: " + uri.getAuthority());
                }
            }
        }

        if (uri.getScheme() == null) {
            return null;
        }

        switch (uri.getScheme()) {
            case "content":
                if (AUTHORITY_GOOGLE_PHOTOS.equals(uri.getAuthority())) {
                    return uri.getLastPathSegment();
                }
                return getDataColumn(context, uri, null, null);
            case "file":
                return uri.getPath();
            default:
                throw new IOException("Unknown URI scheme encountered: " + uri.getScheme());
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;

        final String column = "_data";
        final String[] projection = {
                column
        };

        try {

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {

//                Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static String toFileUriString(File file) {
        return toFileUriString(file.getAbsolutePath());
    }

    public static String toFileUriString(String file) {
        return "file:" + file;
    }

    /**
     * Checks if a target URL is local.
     *
     * @param url Source URL.
     * @return Whether the URL is a local one.
     */
    private static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                Uri pathUri = writeToTempImageAndGetPathUri(context, bmp);
                if (null == pathUri) return null;
                return pathUri.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void bitmapToFile(Context context, Bitmap bitmap, File file) throws
            IOException {
//Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException ignored) {
            Log.e(TAG, "bitmapToFile:IOEXcepton: " + file + " : " + ignored.getMessage());
        }
    }

    public static void bitmapToFile(Context context, Bitmap bitmap, String filename,
                                    String suffix, File dir) throws IOException {
        bitmapToFile(context, bitmap, File.createTempFile(filename, suffix, dir));

    }

    public static void bitmapToFile(Context context, Bitmap bitmap, String filename,
                                    String suffix) throws IOException {
        bitmapToFile(context, bitmap, filename, suffix, context.getCacheDir());

    }

    public static void bitmapToFile(Context context, Bitmap bitmap, String filename) throws
            IOException {
//createFileInES a file to write bitmap data
        bitmapToFile(context, bitmap, filename, ".jpg", context.getCacheDir());
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
/*
                        Bitmap bm = MediaStore.Images.Media.getBitmap(
                                getContext().getContentResolver(), uri);
                        String path = MediaStore.Images.Media.insertImage(
                                getContext().getContentResolver(),
                                bm, "profile", "user profile");
*/
        if (null == inContext || null == inImage) return null;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Grabs metadata for a document specified by URI, logs it to the screen.
     *
     * @param uri The uri for the document whose metadata should be printed.
     */
    public static void dumpImageMetaData(Context context, Uri uri) {
        // BEGIN_INCLUDE (dump_metadata)

        // The query, since it only applies to a single document, will only return one row.
        // no need to filter, sort, or select fields, since we want all fields for one
        // document.
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is provider-specific, and
                // might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an int can't be
                // null in java, the behavior is implementation-specific, which is just a fancy
                // term for "unpredictable".  So as a rule, check if it's null before assigning
                // to an int.  This will happen often:  The storage API allows for remote
                // files, whose size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString will do the
                    // conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // END_INCLUDE (dump_metadata)
    }

/*
    public static String getPath(final Context context, final Uri uri) throws IOException {
        boolean isAfterKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        Log.e(TAG,"uri:" + uri.getAuthority());
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(
                    uri.getAuthority())) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else {
                    return "/stroage/" + type +  "/" + split[1];
                }
            }else if ("com.android.providers.downloads.documents".equals(
                    uri.getAuthority())) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }else if ("com.android.providers.media.documents".equals(
                    uri.getAuthority())) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                contentUri = MediaStore.Files.getContentUri("external");
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {//MediaStore
            return getDataColumn(context, uri, null, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int cindex = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

*/

}
