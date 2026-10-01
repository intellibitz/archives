package intellibitz.intellidroid

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import intellibitz.intellidroid.util.MainApplicationSingleton

open class IntellibitzPermissionActivity : AppCompatActivity() {
    fun mayRequestReadContacts(view: View?): Boolean {
        if (IntellibitzActivityFragment.isReadContactsPermissionGranted(applicationContext)) {
            return true
        }
        if (IntellibitzActivityFragment.shouldShowContactsRationale(this)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                IntellibitzActivityFragment.requestReadContactsPermissions(this)
            } else {
                IntellibitzActivityFragment.showReadContactsSnack(view, this)
            }
        } else {
            // No explanation needed, we can request the permission.
            IntellibitzActivityFragment.requestReadContactsPermissions(this)
        }
        return false
    }

    fun mayRequestReadPhoneState(view: View?): Boolean {
        if (IntellibitzActivityFragment.isReadPhoneStatePermissionGranted(applicationContext)) {
            return true
        }
        if (IntellibitzActivityFragment.shouldShowReadPhoneStateRationale(this)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                IntellibitzActivityFragment.requestReadPhoneStatePermissions(this)
            } else {
                IntellibitzActivityFragment.showReadPhoneStateSnack(view, this)
            }
        } else {
            // No explanation needed, we can request the permission.
            IntellibitzActivityFragment.requestReadPhoneStatePermissions(this)
        }
        return false
    }

    fun mayRequestCamera(view: View?): Boolean {
        if (IntellibitzActivityFragment.isCameraPermissionGranted(applicationContext)) {
            return true
        }
        if (IntellibitzActivityFragment.shouldShowCameraRationale(this)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                IntellibitzActivityFragment.requestCameraPermissions(this)
            } else {
                IntellibitzActivityFragment.showCameraSnack(view, this)
            }
        } else {
            // No explanation needed, we can request the permission.
            IntellibitzActivityFragment.requestCameraPermissions(this)
        }
        return false
    }

    fun mayRequestReadExternalStorage(view: View?): Boolean {
        if (IntellibitzActivityFragment.isReadExternalStoragePermissionGranted(applicationContext)) {
            return true
        }
        if (IntellibitzActivityFragment.shouldShowReadStorageRationale(this)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                IntellibitzActivityFragment.requestReadExternalStoragePermissions(this)
            } else {
                IntellibitzActivityFragment.showReadExternalStorageSnack(view, this)
            }
        } else {
            // No explanation needed, we can request the permission.
            IntellibitzActivityFragment.requestReadExternalStoragePermissions(this)
        }
        return false
    }

    fun mayRequestWriteExternalStorage(view: View?): Boolean {
        if (IntellibitzActivityFragment.isWriteExternalStoragePermissionGranted(applicationContext)) {
            return true
        }
        if (IntellibitzActivityFragment.shouldShowWriteStorageRationale(this)) {
            if (null == view) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                IntellibitzActivityFragment.requestWriteExternalStoragePermissions(this)
            } else {
                IntellibitzActivityFragment.showWriteExternalStorageSnack(view, this)
            }
        } else {
            // No explanation needed, we can request the permission.
            IntellibitzActivityFragment.requestWriteExternalStoragePermissions(this)
        }
        return false
    }

    protected fun onReadExternalStoragePermissionsGranted() {}
    protected fun onWriteExternalStoragePermissionsGranted() {}
    protected fun onContactsPermissionsGranted() {}
    protected fun onPhoneStatePermissionsGranted() {}
    protected fun onCameraPermissionsGranted() {}
    protected fun onReadExternalStoragePermissionsDenied() {}
    protected fun onWriteExternalStoragePermissionsDenied() {}
    protected fun onContactsPermissionsDenied() {}
    protected fun onPhoneStatePermissionsDenied() {}
    protected fun onCameraPermissionsDenied() {}

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainApplicationSingleton.PERM_READ_PHONE_STATE) {
            if (grantResults.size > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onPhoneStatePermissionsGranted()
            } else {
                // permission denied, boo! Disable the
                onPhoneStatePermissionsDenied()
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_READ_CONTACTS) {
            if (grantResults.size > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onContactsPermissionsGranted()
            } else {
                // permission denied, boo! Disable the
                onContactsPermissionsDenied()
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_CAMERA) {
            if (grantResults.size > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onCameraPermissionsGranted()
            } else {
                // permission denied, boo! Disable the
                onCameraPermissionsDenied()
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_READ_EXTERNAL_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onReadExternalStoragePermissionsGranted()
            } else {
                // permission denied, boo! Disable the
                onReadExternalStoragePermissionsDenied()
            }
            // other 'case' lines to check for other
            // permissions this app might request
        } else if (requestCode == MainApplicationSingleton.PERM_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                onWriteExternalStoragePermissionsGranted()
            } else {
                // permission denied, boo! Disable the
                onWriteExternalStoragePermissionsDenied()
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    companion object {
        private const val TAG = "IntellibitzPermission"
    }
}