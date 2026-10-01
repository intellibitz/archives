package intellibitz.intellidroid.fragment

import android.annotation.TargetApi
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.MyAccountActivity
import intellibitz.intellidroid.activity.ProfileInfoActivity
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ProfileListener
import intellibitz.intellidroid.listener.ProfileTopicListener
import intellibitz.intellidroid.task.SetPwdTask
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
class ProfileItemFragment : IntellibitzActivityFragment(),
    UpdateProfileTask.UpdateProfileTaskListener,
    UploadProfilePicTask.UploadProfilePicTaskListener,
    SetPwdTask.SetPwdTaskListener {

    companion object {
        const val TAG = "ProfileItemFragment"
        private const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1

        fun newInstance(listener: ProfileListener, user: ContactItem): ProfileItemFragment {
            val fragment = ProfileItemFragment()
            if (listener is ProfileTopicListener) {
                fragment.setProfileTopicListener(listener)
            }
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }

    private var snackView: View? = null
    private var etName: TextInputEditText? = null
    private var etStatus: TextInputEditText? = null
    private var updateProfileTask: UpdateProfileTask? = null
    private var uploadProfilePicTask: UploadProfilePicTask? = null
    private var btnSend: Button? = null
    private var btnNewPwd: Button? = null
    private var etNewPwd: TextInputEditText? = null
    private var setPwdTask: SetPwdTask? = null
    private var profileTopicListener: ProfileTopicListener? = null
    private var imageView: ImageView? = null

    fun setProfileTopicListener(profileTopicListener: ProfileTopicListener) {
        this.profileTopicListener = profileTopicListener
    }

    override fun onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted()
        if (user?.profilePic == null) {
            initPic()
        }
    }

    @TargetApi(21)
    fun initPic() {
        if (MainApplicationSingleton.isAPI21()) {
            val contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI,
                Uri.encode(user?.mobile)
            )
            val cursor = context?.contentResolver?.query(
                contactUri, null, null, null, null
            )
            cursor?.use {
                if (it.count > 0) {
                    it.moveToFirst()
                    do {
                        val columnIndex = it.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
                        if (columnIndex != -1 && it.getString(columnIndex) != null) {
                            val uri = Uri.parse(it.getString(columnIndex))
                            uri?.let { photoUri ->
                                try {
                                    val bm = MediaStore.Images.Media.getBitmap(
                                        context?.contentResolver, photoUri
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
                            }
                        }
                    } while (it.moveToNext())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        displayImage(user?.profilePic)
        if (!IntellibitzPermissionFragment.isReadContactsPermissionGranted(context)) {
            mayRequestReadContacts(snackView)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        profileTopicListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_item_content, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        snackView = view.findViewById(R.id.cl)
        etName = view.findViewById(R.id.username)
        etStatus = view.findViewById(R.id.et_status)
        imageView = view.findViewById(R.id.profile_imageView)
        imageView?.setOnClickListener {
            startProfileInfoActivity()
        }
        btnSend = view.findViewById(R.id.btn_send)
        etNewPwd = view.findViewById(R.id.tiet_newpwd)
        btnNewPwd = view.findViewById(R.id.btn_newpwd)
        if (savedInstanceState == null) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            btnSend?.setOnClickListener {
                if ("UPDATE".equals(btnSend?.text.toString(), ignoreCase = true)) performSend()
                btnSend?.text = if ("CHANGE".equals(btnSend?.text.toString(), ignoreCase = true)) "UPDATE" else "CHANGE"
                etName?.isEnabled = !(etName?.isEnabled ?: false)
                etStatus?.isEnabled = !(etStatus?.isEnabled ?: false)
            }
            btnNewPwd?.setOnClickListener {
                if ("Update".equals(btnNewPwd?.text.toString(), ignoreCase = true)) execSetPwdTask()
                btnNewPwd?.text = if ("Change Web Password".equals(btnNewPwd?.text.toString(), ignoreCase = true)) "Update" else "Change Web Password"
                etNewPwd?.isEnabled = !(etNewPwd?.isEnabled ?: false)
            }
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        val userName = user?.name
        etName?.setText(userName)
        if (userName == null || userName.isEmpty()) {
            btnSend?.text = "UPDATE"
            etName?.isEnabled = true
            etStatus?.isEnabled = true
        }
        etStatus?.setText(user?.status)
        val myac = view.findViewById<View>(R.id.ll_myaccount)
        myac.setOnClickListener {
            startMyAccountActivity()
        }
    }

    private fun execSetPwdTask() {
        val newpwd = etNewPwd?.text.toString()
        if (TextUtils.isEmpty(newpwd)) return
        setPwdTask = SetPwdTask(
            user?.dataId, user?.token, user?.device,
            user?.deviceRef, newpwd, MainApplicationSingleton.AUTH_SET_PWD
        )
        setPwdTask?.setSetPwdTaskListener(this)
        setPwdTask?.execute()
    }

    override fun onPostSetPwdExecute(response: JSONObject?, newpwd: String) {
        val status = response?.optInt("status") ?: 0
        if (response == null || 99 == status || -1 == status) {
            onPostSetPwdExecuteFail(response, newpwd)
        } else {
            Log.e(TAG, "onPostSetPwdExecute: - SUCCESS - $newpwd")
        }
    }

    override fun onPostSetPwdExecuteFail(response: JSONObject?, newpwd: String) {
        Log.e(TAG, "onPostSetPwdExecuteFail: - FAIL - $response")
    }

    override fun setSetPwdTaskToNull() {
        setPwdTask = null
    }

    fun onNewMenuClicked() {
        btnSend?.callOnClick()
    }

    private fun performSend() {
        val name = etName?.text.toString()
        if (name.isEmpty()) {
            etName?.error = "Name is required for Intellibitz"
        } else {
            etName?.error = null
            user?.name = name
            user?.status = etStatus?.text.toString()
            Log.d(TAG, "perform send")
            updateProfileTask = UpdateProfileTask(
                user?.dataId, user?.token,
                user?.device, user?.deviceRef, user?.name, user?.status,
                MainApplicationSingleton.AUTH_UPDATE_PROFILE
            )
            updateProfileTask?.setUpdateProfileTaskListener(this)
            updateProfileTask?.execute()
        }
    }

    override fun onPostUpdateProfileExecute(response: JSONObject) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status) {
                onPostUpdateProfileExecuteFail(response)
            } else if (-1 == status) {
                onPostUpdateProfileExecuteFail(response)
            } else if (1 == status) {
                UserContentProvider.updatesProfileInDB(user, getMainActivity())
                Log.e(TAG, "Profile Update SUCCESS - $response")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onPostUpdateProfileExecuteFail(response: JSONObject) {
        Log.e(TAG, "Profile Update ERROR - $response")
    }

    override fun setUpdateProfileTaskToNull() {
        updateProfileTask = null
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
        }
    }

    private fun setupMediaChooser(view: View) {
        view.setOnClickListener {
            MediaPicker.openMediaChooser(
                this@ProfileItemFragment,
                "Choose now"
            ) { e ->
                Log.e("MediaPicker", "Open chooser error.", e)
            }
        }
    }

    private fun displayImage(file: String?) {
        if (!file.isNullOrEmpty()) {
            Log.d(TAG, "Profile image change: $file")
            (imageView as? NetworkImageView)?.setImageUrl(
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
                user?.profilePic = filename
                UserContentProvider.updatesProfileInDB(user, getMainActivity())
                Log.e(TAG, "Profile Upload SUCCESS - $response")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onPostUploadProfilePicExecuteFail(response: JSONObject, filename: String) {
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
                setupMediaChooser(imageView!!)
            }
        }
    }

    override fun onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted()
        setupMediaChooser(imageView!!)
    }

    override fun onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted()
        setupMediaChooser(imageView!!)
    }

    override fun onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted()
        setupMediaChooser(imageView!!)
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

    private fun startProfileInfoActivity() {
        val intent = Intent(activity, ProfileInfoActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE)
    }

    private fun startMyAccountActivity() {
        val intent = Intent(activity, MyAccountActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MYACCOUNT_RQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
                            this@ProfileItemFragment,
                            mediaFile,
                            paramWidth,
                            paramHeight,
                            paramColor
                        ) { e ->
                            Log.e("MediaPicker", "Open cropper error.", e)
                        }
                    } else {
                        user?.profilePic = mediaFile.absolutePath
                        displayImage(user?.profilePic)
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
