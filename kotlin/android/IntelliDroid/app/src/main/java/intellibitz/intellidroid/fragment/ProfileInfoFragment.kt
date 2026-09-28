package intellibitz.intellidroid.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.AddEmailActivity
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ProfileTopicListener
import intellibitz.intellidroid.task.UpdateProfileTask
import intellibitz.intellidroid.task.UploadProfilePicTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPicker
import intellibitz.intellidroid.util.MediaPickerRequest
import intellibitz.intellidroid.util.MediaPickerUri
import intellibitz.intellidroid.util.NetworkImageView
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 *
 */
class ProfileInfoFragment : IntellibitzUserFragment(),
    UpdateProfileTask.UpdateProfileTaskListener,
    UploadProfilePicTask.UploadProfilePicTaskListener {

    companion object {
        private const val TAG = "ProfileInfoFragment"
        private const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1

        fun newInstance(user: ContactItem): ProfileInfoFragment {
            val fragment = ProfileInfoFragment()
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }

    private var btnContinue: Button? = null
    private var updateProfileTask: UpdateProfileTask? = null
    private var uploadProfilePicTask: UploadProfilePicTask? = null
    private var imageView: ImageView? = null
    private var profileTopicListener: ProfileTopicListener? = null
    private var etFirstname: EditText? = null
    private var etLastname: EditText? = null
    private var view: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var btnPermission: Button? = null
    private var viewPermission: View? = null
    private var progressDialog: ProgressDialog? = null

    fun setProfileTopicListener(profileTopicListener: ProfileTopicListener?) {
        this.profileTopicListener = profileTopicListener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProfileTopicListener) profileTopicListener = context
    }

    override fun onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted()
        if (null == user?.profilePic) {
            initPic()
        }
    }

    fun initPic() {
        if (MainApplicationSingleton.isAPI21()) {
            val contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI,
                Uri.encode(user?.mobile)
            )
            val cursor = context?.contentResolver?.query(
                contactUri, null, null, null, null
            )
            if (null == cursor) return
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    // Display photo URI.
                    val columnIndex = cursor.getColumnIndex(
                        ContactsContract.PhoneLookup.PHOTO_URI
                    )
                    if (columnIndex != -1 && cursor.getString(columnIndex) != null) {
                        val uri = Uri.parse(cursor.getString(columnIndex))
                        if (uri != null) {
                            try {
                                val bm = MediaStore.Images.Media.getBitmap(
                                    context?.contentResolver, uri
                                )
                                val path = MediaStore.Images.Media.insertImage(
                                    context?.contentResolver, bm, user?.dataId, user?.name
                                )
                                val f = MediaPickerUri.resolveToFile(context, Uri.parse(path))
                                profileImageChanged(f)
                                user?.profilePic = f.absolutePath
                                break
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            break
                        }
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) ;
    }

    fun alertReadContacts(): Boolean {
        if (null == snackbar) {
            val activity = activity
            if (null == activity) return false
            snackbar = makeReadContactsSnack(snackView, activity)
        }
        if (isReadContactsPermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            return true
        } else {
            viewPermission?.visibility = View.VISIBLE
            return mayRequestReadContacts(snackbar, snackView)
        }
    }

    fun alertReadStorage(): Boolean {
        if (null == snackbar) {
            val activity = activity
            if (null == activity) return false
            snackbar = makeReadExternalStorageSnack(snackView, activity)
        }
        if (isReadExternalStoragePermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            return true
        } else {
            viewPermission?.visibility = View.VISIBLE
            return mayRequestReadExternalStorage(snackbar, snackView)
        }
    }

    fun alertWriteStorage(): Boolean {
        if (null == snackbar) {
            val activity = activity
            if (null == activity) return false
            snackbar = makeWriteExternalStorageSnack(snackView, activity)
        }
        if (isWriteExternalStoragePermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            return true
        } else {
            viewPermission?.visibility = View.VISIBLE
            return mayRequestWriteExternalStorage(snackbar, snackView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profileinfo, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        imageView = view.findViewById(R.id.profile_imageView)

        btnContinue = view.findViewById(R.id.btn_continue)
        btnContinue?.setOnClickListener { performSend() }
        btnPermission = view.findViewById(R.id.btn_perm)
        btnPermission?.setOnClickListener {
            val activity = activity
            if (null == activity) return@setOnClickListener
            requestReadContactsPermissions(activity)
        }
        viewPermission = view.findViewById(R.id.ll_perm)
        snackView
        etFirstname = view.findViewById(R.id.et_firstname)
        etLastname = view.findViewById(R.id.et_lastname)

        etLastname?.setOnEditorActionListener { v, actionId, event ->
            // If the event is a key-down event on the "enter" button
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Perform action on key press
                if (isPhoneNumberValid()) {
                    performSend()
                    return@setOnEditorActionListener true
                }
            }
            false
        }
        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin.setOnClickListener { performSend() }

        etFirstname?.requestFocus()
        unPackUser()
        displayImage(user?.profilePic)
        if (!IntellibitzPermissionFragment.isReadContactsPermissionGranted(context)) {
            mayRequestReadContacts(snackView)
        }
        setupMediaChooserOnPermissions(view)
    }

    private fun performSend() {
        val firstname = etFirstname?.text.toString()
        if (TextUtils.isEmpty(firstname)) {
            etFirstname?.error = "First Name is required for Intellibitz"
        }
        val lastname = etLastname?.text.toString()
        if (TextUtils.isEmpty(lastname)) {
            etLastname?.error = "Last Name is required for Intellibitz"
        }
        etFirstname?.error = null
        etLastname?.error = null
        user?.name = firstname
        user?.firstName = firstname
        user?.lastName = firstname
        user?.displayName = "$firstname $lastname"
        showProgress()
        execUpdateProfileTask()
    }

    private fun execUpdateProfileTask() {
        updateProfileTask = UpdateProfileTask(
            user?.dataId, user?.token,
            user?.device, user?.deviceRef, user?.name, user?.status,
            MainApplicationSingleton.AUTH_UPDATE_PROFILE
        )
        updateProfileTask?.setUpdateProfileTaskListener(this)
        updateProfileTask?.execute()
    }

    override fun onPostUpdateProfileExecute(response: JSONObject) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status) {
                onPostUpdateProfileExecuteFail(response)
            } else if (-1 == status) {
                onPostUpdateProfileExecuteFail(response)
            } else if (1 == status) {
                // SUCCESS
                UserContentProvider.updatesProfileInDB(user, activity)
                hideProgress()
                val activity = activity
                val intent = activity?.intent
                intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.finish()
                Log.e(TAG, "Profile Update SUCCESS - $response")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onPostUpdateProfileExecuteFail(response: JSONObject) {
        hideProgress()
        // ERROR
        Log.e(TAG, "Profile Update ERROR - $response")
    }

    override fun setUpdateProfileTaskToNull() {
        updateProfileTask = null
        hideProgress()
    }

    private fun setupMediaChooserOnPermissions(view: View) {
        if (IntellibitzPermissionFragment.isCameraPermissionGranted(context) &&
            IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context) &&
            IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(context)
        ) {
            setupMediaChooser(view)
        } else {
            mayRequestCamera(snackView)
            mayRequestReadExternalStorage(snackView)
            mayRequestWriteExternalStorage(snackView)
            setupMediaChooser(view)
        }
    }

    private fun setupMediaChooser(view: View) {
        if (null == view) {
            Log.e(TAG, "FAILED to setupMediaChooser: view is NULL")
            return
        }
        if (null == imageView) imageView = view.findViewById(R.id.profile_imageView)
        imageView?.setOnClickListener { openMediaChooser() }
        val v = view.findViewById<View>(R.id.ll_avatar)
        v.setOnClickListener { openMediaChooser() }
        val v2 = view.findViewById<View>(R.id.ll_profileimage)
        v2.setOnClickListener { openMediaChooser() }
        val v3 = view.findViewById<View>(R.id.tv_profileinfo)
        v3.setOnClickListener { openMediaChooser() }
    }

    private fun openMediaChooser() {
        MediaPicker.openMediaChooser(
            this,
            "Choose now",
            object : MediaPicker.OnError {
                override fun onError(e: IOException) {
                    Log.e("MediaPicker", "Open chooser error.", e)
                }
            }
        )
    }

    val snackView: View?
        get() {
            if (null == snackView) snackView = view?.findViewById(R.id.username)
            return snackView
        }

    val isContactsPermitted: Boolean
        get() = IntellibitzPermissionFragment.isReadContactsPermissionGranted(activity)

    private fun unPackUser() {
        val firstName = user?.firstName
        if (!TextUtils.isEmpty(firstName)) etFirstname?.setText(firstName)
        val lastName = user?.lastName
        if (!TextUtils.isEmpty(lastName)) etLastname?.setText(lastName)
    }

    /**
     * unpacks the user from the previous state, or arguments onto the ui view
     *
     * @param savedInstanceState the state from which to restore the user from
     */
    private fun unPackUser(@Nullable savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (null == user) {
                // first time.. gets from the arguments
                user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            } else {
                // restores user.. from previous states
                val u = savedInstanceState.getParcelable<ContactItem>(ContactItem.USER_CONTACT)
                if (u != null) {
                    packUser(u)
                }
            }
            if (user != null) {
                // unpacks the user onto the view
                unPackUser()
            } else {
                etFirstname?.setText(user?.firstName)
                etLastname?.setText(user?.lastName)
            }
        }
    }

    private fun packUser(@Nullable savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable(ContactItem.USER_CONTACT, packUser())
        }
    }

    private fun packUser(u: ContactItem) {
        user?.name = u.name
        user?.device = u.device
        user?.mobile = u.mobile
        user?.countryName = u.countryName
        user?.countryCode = u.countryCode
        user?.profilePic = u.profilePic
    }

    fun packUser(): ContactItem {
        val firstname = etFirstname?.text.toString()
        val lastname = etLastname?.text.toString()
        if (!TextUtils.isEmpty(firstname)) user?.firstName = firstname
        if (!TextUtils.isEmpty(lastname)) user?.lastName = lastname
        return user!!
    }

    private fun setTvUsername(cursor: Cursor) {
        try {
            val nameIndex = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            if (nameIndex != -1) {
                try {
                    val `val` = cursor.getString(nameIndex)
                    user?.name = `val`
                } catch (ignored: Throwable) {
                    Log.e(TAG, ignored.message)
                }
            }
        } catch (ignore: Throwable) {
            Log.e(TAG, "Name cannot be retrieved from Cursor: ${ignore.message}")
        }
    }

    private fun getFormattedPhoneNumber(): String? {
        var text: String? = null
        text = if (etFirstname != null) etFirstname?.text.toString() else ""
        return MainApplicationSingleton.parseFormatPhoneNumberByISO(
            text, user?.country?.isoCode
        )
    }

    private fun invalidPhoneNumberAlert(v: View?) {
        if (null == v || null == v.resources) return
        setError(v.resources.getString(R.string.enter_phone_number))
    }

    protected fun isPhoneNumberValid(): Boolean {
        // Phone Number not entered
        if (etFirstname != null && etFirstname?.text.toString() == "") {
            invalidPhoneNumberAlert(etFirstname)
            return false
        } else {
            val phoneNumber = getFormattedPhoneNumber()
            // Phone Number invalid
            if (phoneNumber == null) {
                invalidPhoneNumberAlert(etFirstname)
                return false
            }
        }
        setError(null)
        return true
    }

    fun setError(text: String?) {
        // clears the error message and the icon
        if (etFirstname != null) etFirstname?.error = text
    }

    private fun getSimCountryIso(): String? {
        val activity = activity
        if (null == activity) return null
        return MainApplicationSingleton.getSimCountryIso(activity)
    }

    fun showProgress() {
        progressDialog = ProgressDialog.show(activity, "Profile", "Saving Profile info", true)
    }

    fun hideProgress() {
        if (progressDialog != null) {
            val activity = activity
            if (activity != null && !activity.isFinishing) {
                progressDialog?.dismiss()
            }
        }
    }

    private fun displayImage(file: String?) {
        if (file != null && !file.isEmpty()) {
            Log.d(TAG, "Profile image change: $file")
            if (MainApplicationSingleton.isAPI16()) {
                imageView?.background = null
            } else {
                imageView?.background = null
            }
            (imageView as NetworkImageView).setImageUrl(
                file,
                MainApplicationSingleton.getInstance(activity).imageLoader
            )
        }
    }

    fun profileImageChanged(file: File) {
        profileTopicListener?.onProfilePicChanged(file)
        onProfilePicChanged(file)
    }

    fun onProfilePicChanged(file: File) {
        uploadProfilePicTask = UploadProfilePicTask(
            user?.dataId, user?.token,
            user?.device, user?.deviceRef,
            MainApplicationSingleton.AUTH_UPLOAD_PROFILE_PIC
        )
        uploadProfilePicTask?.setUploadProfilePicTaskListener(this)
        uploadProfilePicTask?.execute(file)
    }

    override fun onPostUploadProfilePicExecute(response: JSONObject, filename: String) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status || -1 == status) {
                onPostUploadProfilePicExecuteFail(response, filename)
            } else if (1 == status) {
                // SUCCESS
                user?.profilePic = filename
                UserContentProvider.updatesProfileInDB(user, activity)
                Log.e(TAG, "Profile Upload SUCCESS - $response")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onPostUploadProfilePicExecuteFail(response: JSONObject, filename: String) {
        // ERROR
        Log.e(TAG, "onPostUploadProfilePicExecuteFail: ERROR - $response :file: $filename")
    }

    override fun setUploadProfilePicTaskToNull() {
        uploadProfilePicTask = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMediaChooser(view!!)
            }
        }
    }

    override fun onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted()
        setupMediaChooser(view!!)
    }

    override fun onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted()
        setupMediaChooser(view!!)
    }

    override fun onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted()
        setupMediaChooser(view!!)
    }

    override fun onReadExternalStoragePermissionsDenied() {
        super.onReadExternalStoragePermissionsDenied()
    }

    override fun onWriteExternalStoragePermissionsDenied() {
        super.onWriteExternalStoragePermissionsDenied()
    }

    override fun onContactsPermissionsDenied() {
        super.onContactsPermissionsDenied()
    }

    override fun onCameraPermissionsDenied() {
        super.onCameraPermissionsDenied()
    }

    /**
     * You are calling startActivityForResult() from your Fragment. When you do this,
     * the requestCode is changed by the Activity that owns the Fragment.
     * If you want to get the correct resultCode in your activity try this:
     * Change:
     * startActivityForResult(intent, 1);
     * To:
     * getActivity().startActivityForResult(intent, 1);
     * Just a note: if you use startActivityForResult in a fragment and expect the result from
     * onActivityResult in that fragment, just make sure you call super.onActivityResult in the
     * host activity (in case you override that method there).
     * This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
     * Also, note that the request code, when it travels through the activity's onActivityResult,
     * is altered
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
     */
    private fun startAddEmailActivity() {
        val intent = Intent(activity, AddEmailActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        // NOTE: there is a significant difference between the following two calls

        // THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
        // MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
        // TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE)

        // THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
        // MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
        // TO BE DELIVERED TO THIS FRAGMENT
        // getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    /**
     * You are calling startActivityForResult() from your Fragment. When you do this,
     * the requestCode is changed by the Activity that owns the Fragment.
     * If you want to get the correct resultCode in your activity try this:
     * Change:
     * startActivityForResult(intent, 1);
     * To:
     * getActivity().startActivityForResult(intent, 1);
     * Just a note: if you use startActivityForResult in a fragment and expect the result from
     * onActivityResult in that fragment, just make sure you call super.onActivityResult in the
     * host activity (in case you override that method there).
     * This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
     * Also, note that the request code, when it travels through the activity's onActivityResult,
     * is altered
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
     *
     * @param requestCode the request code with which the activity started
     * @param resultCode the result code send back by the activity
     * @param data the intent data with extras
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                    // contacts selected for chat
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val activity = activity
                    val intent = activity?.intent
                    intent?.putExtra(ContactItem.USER_CONTACT, item as Parcelable)
                    activity?.setResult(Activity.RESULT_OK, intent)
                    activity?.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val activity = activity
                val intent = activity?.intent
                intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                activity?.setResult(Activity.RESULT_CANCELED, intent)
                activity?.finish()
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }

        MediaPicker.handleActivityResult(
            activity,
            requestCode,
            resultCode,
            data,
            object : MediaPicker.OnResult {
                override fun onError(e: IOException) {
                    Log.e("MediaPicker", "Got file error.", e)
                }

                override fun onSuccess(mediaFile: File, request: MediaPickerRequest) {
                    Log.e("MediaPicker", "Got file result: $mediaFile for code: $request")
                    if (request != MediaPickerRequest.REQUEST_CROP) {
                        val paramColor = ContextCompat.getColor(activity!!, android.R.color.black)
                        val paramWidth = 128
                        val paramHeight = 128
                        MediaPicker.startForImageCrop(
                            this@ProfileInfoFragment,
                            mediaFile,
                            paramWidth,
                            paramHeight,
                            paramColor,
                            object : MediaPicker.OnError {
                                override fun onError(e: IOException) {
                                    Log.e("MediaPicker", "Open cropper error.", e)
                                }
                            }
                        )
                    } else {
                        user?.profilePic = mediaFile.absolutePath
                        // When we are done cropping, display it in the ImageView.
                        displayImage(user?.profilePic)
                        // notify the listener, the parent activity in this case
                        // syncs to the cloud
                        profileImageChanged(mediaFile)
                    }
                }

                override fun onCancelled() {
                    Log.e("MediaPicker", "Got cancelled event.")
                }
            }
        )
    }
}
