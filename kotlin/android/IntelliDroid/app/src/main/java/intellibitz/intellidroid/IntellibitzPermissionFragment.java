package intellibitz.intellidroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntellibitzPermissionFragment extends Fragment {

    public IntellibitzPermissionFragment() {
        // Required empty public constructor
        super();
    }

    public static boolean isReadContactsPermissionGranted(Context context) {
        if (null == context) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isReadPhoneStatePermissionGranted(Context context) {
        if (null == context) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_PHONE_STATE) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isCameraPermissionGranted(Context context) {
        if (null == context) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isReadExternalStoragePermissionGranted(Context context) {
        if (null == context) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalStoragePermissionGranted(Context context) {
        if (null == context) return false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static void requestReadContactsPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{READ_CONTACTS},
                MainApplicationSingleton.PERM_READ_CONTACTS);
    }

    public static void requestReadPhoneStatePermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{READ_PHONE_STATE},
                MainApplicationSingleton.PERM_READ_PHONE_STATE);
    }

    public static void requestCameraPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{CAMERA},
                MainApplicationSingleton.PERM_CAMERA);
    }

    public static void requestReadExternalStoragePermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{READ_EXTERNAL_STORAGE},
                MainApplicationSingleton.PERM_READ_EXTERNAL_STORAGE);
    }

    public static void requestWriteExternalStoragePermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{WRITE_EXTERNAL_STORAGE},
                MainApplicationSingleton.PERM_WRITE_EXTERNAL_STORAGE);
    }

    public static Snackbar showReadContactsSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = makeReadContactsSnack(view, context);
        ;
        snackbar.show();
        return snackbar;
    }

    public static Snackbar showReadContactsSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = makeReadContactsSnack(snackbar, view, context);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar makeReadContactsSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newReadContactsSnackbar(view);
        snackbar = setReadContactsActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeReadContactsSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = newReadContactsSnackbar(view);
        snackbar = setReadContactsActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeReadExternalStorageSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newReadExternalStorageSnackbar(view);
        snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeReadExternalStorageSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = newReadExternalStorageSnackbar(view);
        snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeWriteExternalStorageSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newWriteExternalStorageSnackbar(view);
        snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeWriteExternalStorageSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = newWriteExternalStorageSnackbar(view);
        snackbar = setWriteExternalStorageActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    @NonNull
    public static Snackbar setReadContactsActionOnSnackbar(Snackbar snackbar, final Activity context) {
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReadContactsPermissions(context);
            }
        });
        return snackbar;
    }

    public static Snackbar setReadExternalStorageActionOnSnackbar(Snackbar snackbar, final Activity context) {
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReadExternalStoragePermissions(context);
            }
        });
        return snackbar;
    }

    public static Snackbar setWriteExternalStorageActionOnSnackbar(Snackbar snackbar, final Activity context) {
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWriteExternalStoragePermissions(context);
            }
        });
        return snackbar;
    }

    public static Snackbar showReadPhoneStateSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = makeReadPhoneStateSnack(view, context);
        ;
        snackbar.show();
        return snackbar;
    }

    public static Snackbar showReadPhoneStateSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = makeReadPhoneStateSnack(snackbar, view, context);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar makeReadPhoneStateSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newReadPhoneStateSnackbar(view);
        snackbar = setReadPhoneStateActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    public static Snackbar makeReadPhoneStateSnack(Snackbar snackbar, View view, final Activity context) {
        if (null == snackbar) return null;
        snackbar = newReadPhoneStateSnackbar(view);
        snackbar = setReadPhoneStateActionOnSnackbar(snackbar, context);
        return snackbar;
    }

    @NonNull
    public static Snackbar setReadPhoneStateActionOnSnackbar(Snackbar snackbar, final Activity context) {
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReadPhoneStatePermissions(context);
            }
        });
        return snackbar;
    }

    public static Snackbar showCameraSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newCameraSnackbar(view);
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermissions(context);
            }
        });
        snackbar.show();
        return snackbar;
    }

    public static Snackbar showReadExternalStorageSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newReadExternalStorageSnackbar(view);
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReadExternalStoragePermissions(context);
            }
        });
        snackbar.show();
        return snackbar;
    }

    public static Snackbar showWriteExternalStorageSnack(View view, final Activity context) {
        if (null == view) return null;
        Snackbar snackbar = newWriteExternalStorageSnackbar(view);
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWriteExternalStoragePermissions(context);
            }
        });
        snackbar.show();
        return snackbar;
    }

    @NonNull
    public static Snackbar newReadContactsSnackbar(View view) {
        return Snackbar.make(view,
//                R.string.contacts_autocomplete_permission,
                R.string.app_title,
                Snackbar.LENGTH_INDEFINITE);
    }

    @NonNull
    public static Snackbar newReadPhoneStateSnackbar(View view) {
        return Snackbar.make(view,
//                R.string.device_id_permission,
                R.string.app_title,
                Snackbar.LENGTH_INDEFINITE);
    }

    @NonNull
    public static Snackbar newCameraSnackbar(View view) {
        return Snackbar.make(view,
                "Camera required for Pics",
                Snackbar.LENGTH_INDEFINITE);
    }

    @NonNull
    public static Snackbar newReadExternalStorageSnackbar(View view) {
        return Snackbar.make(view,
                "Read Storage required for Pics",
                Snackbar.LENGTH_INDEFINITE);
    }

    @NonNull
    public static Snackbar newWriteExternalStorageSnackbar(View view) {
        return Snackbar.make(view,
                "Write Storage required for Pics",
                Snackbar.LENGTH_INDEFINITE);
    }

    public static boolean shouldShowContactsRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_CONTACTS);
    }

    public static boolean shouldShowReadPhoneStateRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_PHONE_STATE);
    }

    public static boolean shouldShowCameraRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA);
    }

    public static boolean shouldShowReadStorageRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean shouldShowWriteStorageRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean mayRequestCamera(View view, Activity activity) {
        if (isCameraPermissionGranted(activity.getApplicationContext())) {
            return true;
        }
        if (shouldShowCameraRationale(activity)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestCameraPermissions(activity);
            } else {
                showCameraSnack(view, activity);
            }
        } else {
            // No explanation needed, we can request the permission.
            requestCameraPermissions(activity);
        }
        return false;
    }

/*
    public static boolean mayRequestReadPhoneState(View view, Context context, Activity activity) {
        if (isReadPhoneStatePermissionGranted(context)) {
            return true;
        }
        if (shouldShowReadPhoneStateRationale(activity)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadPhoneStatePermissions(activity);
            } else {
                showReadPhoneStateSnack(view, activity);
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadPhoneStatePermissions(activity);
        }
        return false;
    }
*/

    public static boolean mayRequestReadExternalStorage(View view, Activity activity) {
        if (isReadExternalStoragePermissionGranted(activity.getApplicationContext())) {
            return true;
        }
        if (shouldShowReadStorageRationale(activity)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadExternalStoragePermissions(activity);
            } else {
                showReadExternalStorageSnack(view, activity);
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadExternalStoragePermissions(activity);
        }
        return false;
    }

    public static boolean mayRequestWriteExternalStorage(View view, Activity activity) {
        if (isWriteExternalStoragePermissionGranted(activity.getApplicationContext())) {
            return true;
        }
        if (shouldShowWriteStorageRationale(activity)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestWriteExternalStoragePermissions(activity);
            } else {
                showWriteExternalStorageSnack(view, activity);
            }
        } else {
            // No explanation needed, we can request the permission.
            requestWriteExternalStoragePermissions(activity);
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public boolean mayRequestReadContacts(Snackbar snackbar, View view) {
        if (isReadContactsPermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowContactsRationale(getActivity())) {
            if (null == snackbar) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadContactsPermissions(getActivity());
            } else {
                makeReadContactsSnack(snackbar, view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadContactsPermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestReadExternalStorage(Snackbar snackbar, View view) {
        if (isReadExternalStoragePermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowReadStorageRationale(getActivity())) {
            if (null == snackbar) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadExternalStoragePermissions(getActivity());
            } else {
                makeReadExternalStorageSnack(snackbar, view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadExternalStoragePermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestWriteExternalStorage(Snackbar snackbar, View view) {
        if (isWriteExternalStoragePermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowWriteStorageRationale(getActivity())) {
            if (null == snackbar) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestWriteExternalStoragePermissions(getActivity());
            } else {
                makeWriteExternalStorageSnack(snackbar, view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestWriteExternalStoragePermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestReadContacts(View view) {
        if (isReadContactsPermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowContactsRationale(getActivity())) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadContactsPermissions(getActivity());
            } else {
                showReadContactsSnack(view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadContactsPermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestReadPhoneState(Snackbar snackbar, View view) {
        if (isReadPhoneStatePermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowReadPhoneStateRationale(getActivity())) {
            if (null == snackbar) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadPhoneStatePermissions(getActivity());
            } else {
                makeReadPhoneStateSnack(snackbar, view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadPhoneStatePermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestReadPhoneState(View view) {
        if (isReadPhoneStatePermissionGranted(getContext())) {
            return true;
        }
        if (shouldShowReadPhoneStateRationale(getActivity())) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestReadPhoneStatePermissions(getActivity());
            } else {
                showReadPhoneStateSnack(view, getActivity());
            }
        } else {
            // No explanation needed, we can request the permission.
            requestReadPhoneStatePermissions(getActivity());
        }
        return false;
    }

    public boolean mayRequestCamera(View view) {
        return mayRequestCamera(view, getActivity());
    }

    public boolean mayRequestReadExternalStorage(View view) {
        return mayRequestReadExternalStorage(view, getActivity());
    }

    public boolean mayRequestWriteExternalStorage(View view) {
        return mayRequestWriteExternalStorage(view, getActivity());
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MainApplicationSingleton.PERM_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onReadPhoneStatePermissionsGranted();
            } else {
                // permission denied, boo! Disable the
                onPhoneReadStatePermissionsDenied();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onContactsPermissionsGranted();
            } else {
                // permission denied, boo! Disable the
                onContactsPermissionsDenied();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onCameraPermissionsGranted();
            } else {
                // permission denied, boo! Disable the
                onCameraPermissionsDenied();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onReadExternalStoragePermissionsGranted();
            } else {
                // permission denied, boo! Disable the
                onReadExternalStoragePermissionsDenied();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onWriteExternalStoragePermissionsGranted();
            } else {
                // permission denied, boo! Disable the
                onWriteExternalStoragePermissionsDenied();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected void onReadExternalStoragePermissionsGranted() {
    }

    protected void onReadExternalStoragePermissionsDenied() {
    }

    protected void onWriteExternalStoragePermissionsDenied() {
    }

    protected void onWriteExternalStoragePermissionsGranted() {
    }

    protected void onContactsPermissionsGranted() {
    }

    protected void onContactsPermissionsDenied() {
    }

    protected void onPhoneReadStatePermissionsDenied() {
    }

    protected void onReadPhoneStatePermissionsGranted() {
    }

    protected void onCameraPermissionsGranted() {
    }

    protected void onCameraPermissionsDenied() {
    }

}
