

package intellibitz.intellidroid

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.R
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE

open class IntellibitzPermissionFragment : Fragment() {
    companion object {
        fun isReadContactsPermissionGranted(context: Context): Boolean {
            if ((null == context)) {
                return false
            }
            return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED))
        }
        fun isReadPhoneStatePermissionGranted(context: Context): Boolean {
            if ((null == context)) {
                return false
            }
            return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PermissionChecker.PERMISSION_GRANTED))
        }
        fun isCameraPermissionGranted(context: Context): Boolean {
            if ((null == context)) {
                return false
            }
            return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED))
        }
        fun isReadExternalStoragePermissionGranted(context: Context): Boolean {
            if ((null == context)) {
                return false
            }
            return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED))
        }
        fun isWriteExternalStoragePermissionGranted(context: Context): Boolean {
            if ((null == context)) {
                return false
            }
            return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED))
        }
        fun requestReadContactsPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(READ_CONTACTS), MainApplicationSingleton.PERM_READ_CONTACTS)
        }
        fun requestReadPhoneStatePermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(READ_PHONE_STATE), MainApplicationSingleton.PERM_READ_PHONE_STATE)
        }
        fun requestCameraPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(CAMERA), MainApplicationSingleton.PERM_CAMERA)
        }
        fun requestReadExternalStoragePermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(READ_EXTERNAL_STORAGE), MainApplicationSingleton.PERM_READ_EXTERNAL_STORAGE)
        }
        fun requestWriteExternalStoragePermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(WRITE_EXTERNAL_STORAGE), MainApplicationSingleton.PERM_WRITE_EXTERNAL_STORAGE)
        }
        fun showReadContactsSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = makeReadContactsSnack(view, context)
            snackbar.show()
            return snackbar
        }
        fun showReadContactsSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = makeReadContactsSnack(snackbar, view, context)
            snackbar.show()
            return snackbar
        }
        fun makeReadContactsSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newReadContactsSnackbar(view)
            snackbar = setReadContactsActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeReadContactsSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = newReadContactsSnackbar(view)
            snackbar = setReadContactsActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeReadExternalStorageSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newReadExternalStorageSnackbar(view)
            snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeReadExternalStorageSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = newReadExternalStorageSnackbar(view)
            snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeWriteExternalStorageSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newWriteExternalStorageSnackbar(view)
            snackbar = setReadExternalStorageActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeWriteExternalStorageSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = newWriteExternalStorageSnackbar(view)
            snackbar = setWriteExternalStorageActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun setReadContactsActionOnSnackbar(snackbar: Snackbar, context: Activity): Snackbar {
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestReadContactsPermissions(context)
        }
    })
            return snackbar
        }
        fun setReadExternalStorageActionOnSnackbar(snackbar: Snackbar, context: Activity): Snackbar {
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestReadExternalStoragePermissions(context)
        }
    })
            return snackbar
        }
        fun setWriteExternalStorageActionOnSnackbar(snackbar: Snackbar, context: Activity): Snackbar {
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestWriteExternalStoragePermissions(context)
        }
    })
            return snackbar
        }
        fun showReadPhoneStateSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = makeReadPhoneStateSnack(view, context)
            snackbar.show()
            return snackbar
        }
        fun showReadPhoneStateSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = makeReadPhoneStateSnack(snackbar, view, context)
            snackbar.show()
            return snackbar
        }
        fun makeReadPhoneStateSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newReadPhoneStateSnackbar(view)
            snackbar = setReadPhoneStateActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun makeReadPhoneStateSnack(snackbar: Snackbar, view: View, context: Activity): Snackbar {
            if ((null == snackbar)) {
                return null
            }
            snackbar = newReadPhoneStateSnackbar(view)
            snackbar = setReadPhoneStateActionOnSnackbar(snackbar, context)
            return snackbar
        }
        fun setReadPhoneStateActionOnSnackbar(snackbar: Snackbar, context: Activity): Snackbar {
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestReadPhoneStatePermissions(context)
        }
    })
            return snackbar
        }
        fun showCameraSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newCameraSnackbar(view)
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestCameraPermissions(context)
        }
    })
            snackbar.show()
            return snackbar
        }
        fun showReadExternalStorageSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newReadExternalStorageSnackbar(view)
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestReadExternalStoragePermissions(context)
        }
    })
            snackbar.show()
            return snackbar
        }
        fun showWriteExternalStorageSnack(view: View, context: Activity): Snackbar {
            if ((null == view)) {
                return null
            }
            var snackbar: Snackbar = newWriteExternalStorageSnackbar(view)
            snackbar.setAction("Grant Permission", object : View.OnClickListener() {    override fun onClick(v: View) {
            requestWriteExternalStoragePermissions(context)
        }
    })
            snackbar.show()
            return snackbar
        }
        fun newReadContactsSnackbar(view: View): Snackbar {
            return Snackbar.make(view, R.string.app_title, Snackbar.LENGTH_INDEFINITE)
        }
        fun newReadPhoneStateSnackbar(view: View): Snackbar {
            return Snackbar.make(view, R.string.app_title, Snackbar.LENGTH_INDEFINITE)
        }
        fun newCameraSnackbar(view: View): Snackbar {
            return Snackbar.make(view, "Camera required for Pics", Snackbar.LENGTH_INDEFINITE)
        }
        fun newReadExternalStorageSnackbar(view: View): Snackbar {
            return Snackbar.make(view, "Read Storage required for Pics", Snackbar.LENGTH_INDEFINITE)
        }
        fun newWriteExternalStorageSnackbar(view: View): Snackbar {
            return Snackbar.make(view, "Write Storage required for Pics", Snackbar.LENGTH_INDEFINITE)
        }
        fun shouldShowContactsRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)
        }
        fun shouldShowReadPhoneStateRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)
        }
        fun shouldShowCameraRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        }
        fun shouldShowReadStorageRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        fun shouldShowWriteStorageRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        fun mayRequestCamera(view: View, activity: Activity): Boolean {
            if (isCameraPermissionGranted(activity.getApplicationContext())) {
                return true
            }
            if (shouldShowCameraRationale(activity)) {
                if ((null == view)) {
                    requestCameraPermissions(activity)
                }
                else {
                    showCameraSnack(view, activity)
                }
            }
            else {
                requestCameraPermissions(activity)
            }
            return false
        }
        fun mayRequestReadExternalStorage(view: View, activity: Activity): Boolean {
            if (isReadExternalStoragePermissionGranted(activity.getApplicationContext())) {
                return true
            }
            if (shouldShowReadStorageRationale(activity)) {
                if ((null == view)) {
                    requestReadExternalStoragePermissions(activity)
                }
                else {
                    showReadExternalStorageSnack(view, activity)
                }
            }
            else {
                requestReadExternalStoragePermissions(activity)
            }
            return false
        }
        fun mayRequestWriteExternalStorage(view: View, activity: Activity): Boolean {
            if (isWriteExternalStoragePermissionGranted(activity.getApplicationContext())) {
                return true
            }
            if (shouldShowWriteStorageRationale(activity)) {
                if ((null == view)) {
                    requestWriteExternalStoragePermissions(activity)
                }
                else {
                    showWriteExternalStorageSnack(view, activity)
                }
            }
            else {
                requestWriteExternalStoragePermissions(activity)
            }
            return false
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    fun mayRequestReadContacts(snackbar: Snackbar, view: View): Boolean {
        if (isReadContactsPermissionGranted(getContext())) {
            return true
        }
        if (shouldShowContactsRationale(getActivity())) {
            if ((null == snackbar)) {
                requestReadContactsPermissions(getActivity())
            }
            else {
                makeReadContactsSnack(snackbar, view, getActivity())
            }
        }
        else {
            requestReadContactsPermissions(getActivity())
        }
        return false
    }
    fun mayRequestReadExternalStorage(snackbar: Snackbar, view: View): Boolean {
        if (isReadExternalStoragePermissionGranted(getContext())) {
            return true
        }
        if (shouldShowReadStorageRationale(getActivity())) {
            if ((null == snackbar)) {
                requestReadExternalStoragePermissions(getActivity())
            }
            else {
                makeReadExternalStorageSnack(snackbar, view, getActivity())
            }
        }
        else {
            requestReadExternalStoragePermissions(getActivity())
        }
        return false
    }
    fun mayRequestWriteExternalStorage(snackbar: Snackbar, view: View): Boolean {
        if (isWriteExternalStoragePermissionGranted(getContext())) {
            return true
        }
        if (shouldShowWriteStorageRationale(getActivity())) {
            if ((null == snackbar)) {
                requestWriteExternalStoragePermissions(getActivity())
            }
            else {
                makeWriteExternalStorageSnack(snackbar, view, getActivity())
            }
        }
        else {
            requestWriteExternalStoragePermissions(getActivity())
        }
        return false
    }
    fun mayRequestReadContacts(view: View): Boolean {
        if (isReadContactsPermissionGranted(getContext())) {
            return true
        }
        if (shouldShowContactsRationale(getActivity())) {
            if ((null == view)) {
                requestReadContactsPermissions(getActivity())
            }
            else {
                showReadContactsSnack(view, getActivity())
            }
        }
        else {
            requestReadContactsPermissions(getActivity())
        }
        return false
    }
    fun mayRequestReadPhoneState(snackbar: Snackbar, view: View): Boolean {
        if (isReadPhoneStatePermissionGranted(getContext())) {
            return true
        }
        if (shouldShowReadPhoneStateRationale(getActivity())) {
            if ((null == snackbar)) {
                requestReadPhoneStatePermissions(getActivity())
            }
            else {
                makeReadPhoneStateSnack(snackbar, view, getActivity())
            }
        }
        else {
            requestReadPhoneStatePermissions(getActivity())
        }
        return false
    }
    fun mayRequestReadPhoneState(view: View): Boolean {
        if (isReadPhoneStatePermissionGranted(getContext())) {
            return true
        }
        if (shouldShowReadPhoneStateRationale(getActivity())) {
            if ((null == view)) {
                requestReadPhoneStatePermissions(getActivity())
            }
            else {
                showReadPhoneStateSnack(view, getActivity())
            }
        }
        else {
            requestReadPhoneStatePermissions(getActivity())
        }
        return false
    }
    fun mayRequestCamera(view: View): Boolean {
        return mayRequestCamera(view, getActivity())
    }
    fun mayRequestReadExternalStorage(view: View): Boolean {
        return mayRequestReadExternalStorage(view, getActivity())
    }
    fun mayRequestWriteExternalStorage(view: View): Boolean {
        return mayRequestWriteExternalStorage(view, getActivity())
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: Array<Int>) {
        if ((requestCode == MainApplicationSingleton.PERM_READ_PHONE_STATE)) {
            if (((grantResults.size > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED))) {
                onReadPhoneStatePermissionsGranted()
            }
            else {
                onPhoneReadStatePermissionsDenied()
            }
        }
        else {
            if ((requestCode == MainApplicationSingleton.PERM_READ_CONTACTS)) {
                if (((grantResults.size > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED))) {
                    onContactsPermissionsGranted()
                }
                else {
                    onContactsPermissionsDenied()
                }
            }
            else {
                if ((requestCode == MainApplicationSingleton.PERM_CAMERA)) {
                    if (((grantResults.size > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED))) {
                        onCameraPermissionsGranted()
                    }
                    else {
                        onCameraPermissionsDenied()
                    }
                }
                else {
                    if ((requestCode == MainApplicationSingleton.PERM_READ_EXTERNAL_STORAGE)) {
                        if (((grantResults.size > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED))) {
                            onReadExternalStoragePermissionsGranted()
                        }
                        else {
                            onReadExternalStoragePermissionsDenied()
                        }
                    }
                    else {
                        if ((requestCode == MainApplicationSingleton.PERM_WRITE_EXTERNAL_STORAGE)) {
                            if (((grantResults.size > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED))) {
                                onWriteExternalStoragePermissionsGranted()
                            }
                            else {
                                onWriteExternalStoragePermissionsDenied()
                            }
                        }
                    }
                }
            }
        }
    }
    protected fun onReadExternalStoragePermissionsGranted() {

    }
    protected fun onReadExternalStoragePermissionsDenied() {

    }
    protected fun onWriteExternalStoragePermissionsDenied() {

    }
    protected fun onWriteExternalStoragePermissionsGranted() {

    }
    protected fun onContactsPermissionsGranted() {

    }
    protected fun onContactsPermissionsDenied() {

    }
    protected fun onPhoneReadStatePermissionsDenied() {

    }
    protected fun onReadPhoneStatePermissionsGranted() {

    }
    protected fun onCameraPermissionsGranted() {

    }
    protected fun onCameraPermissionsDenied() {

    }
}
