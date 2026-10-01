package intellibitz.intellidroid.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Filter
import android.widget.ImageView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.snackbar.Snackbar
import com.google.i18n.phonenumbers.PhoneNumberUtil
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.OTPActivity
import intellibitz.intellidroid.bean.Country
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.service.ContactFetch
import intellibitz.intellidroid.task.MobileGetCodeTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPickerFile
import intellibitz.intellidroid.util.MediaPickerUri
import intellibitz.intellidroid.widget.CountryPicker
import intellibitz.intellidroid.widget.CountryPickerListener
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.ArrayList

/**
 *
 */
class LoginFragment : IntellibitzUserFragment(), LoaderManager.LoaderCallbacks<Cursor>, MobileGetCodeTask.MobileGetCodeTaskListener {

    private var mobileGetCodeListener: MobileGetCodeListener? = null
    private var mobileGetCodeTask: MobileGetCodeTask? = null

    private lateinit var tvMobile: AutoCompleteTextView
    private lateinit var tvCountry: AutoCompleteTextView
    private lateinit var btnCountry: Button
    private lateinit var view: View
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private lateinit var btnPermission: Button
    private lateinit var viewPermission: View
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val TAG = "LoginFragment"

        fun newInstance(user: ContactItem): LoginFragment {
            val fragment = LoginFragment()
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MobileGetCodeListener) {
            mobileGetCodeListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) {
            // Do nothing
        }
        if (isContactsPermitted()) {
            setPermissionsGranted()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    fun alertReadContacts(): Boolean {
        if (snackbar == null) {
            val activity = activity ?: return false
            snackbar = makeReadContactsSnack(getSnackView(), activity)
        }
        if (isReadContactsPermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission.visibility = View.GONE
            return true
        } else {
            viewPermission.visibility = View.VISIBLE
            return mayRequestReadContacts(snackbar!!, getSnackView())
        }
    }

    fun alertReadStorage(): Boolean {
        if (snackbar == null) {
            val activity = activity ?: return false
            snackbar = makeReadExternalStorageSnack(getSnackView(), activity)
        }
        if (isReadExternalStoragePermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission.visibility = View.GONE
            return true
        } else {
            viewPermission.visibility = View.VISIBLE
            return mayRequestReadExternalStorage(snackbar!!, getSnackView())
        }
    }

    fun alertWriteStorage(): Boolean {
        if (snackbar == null) {
            val activity = activity ?: return false
            snackbar = makeWriteExternalStorageSnack(getSnackView(), activity)
        }
        if (isWriteExternalStoragePermissionGranted(activity)) {
            snackbar?.dismiss()
            viewPermission.visibility = View.GONE
            return true
        } else {
            viewPermission.visibility = View.VISIBLE
            return mayRequestWriteExternalStorage(snackbar!!, getSnackView())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        val simpleCursorAdapter: SimpleCursorAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            simpleCursorAdapter = SimpleCursorAdapter(
                activity,
                R.layout.autocomplete_contact,
                null,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                ),
                intArrayOf(R.id.text1, R.id.text2, R.id.imageView),
                0
            )
        } else {
            simpleCursorAdapter = SimpleCursorAdapter(
                activity,
                R.layout.autocomplete_contact,
                null,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactFetch.PHOTO_THUMBNAIL_URI
                ),
                intArrayOf(R.id.text1, R.id.text2, R.id.imageView)
            )
        }
        simpleCursorAdapter.setStringConversionColumn(1)
        simpleCursorAdapter.setViewBinder { view, cursor, columnIndex ->
            if (R.id.imageView == view.id) {
                try {
                    if (cursor != null && !cursor.isClosed && cursor.count > 0) {
                        val photo = cursor.getString(cursor.getColumnIndex(ContactFetch.PHOTO_THUMBNAIL_URI))
                        if (photo != null) {
                            view.visibility = View.VISIBLE
                            (view as ImageView).setImageBitmap(
                                MediaStore.Images.Media.getBitmap(
                                    activity?.contentResolver,
                                    Uri.parse(photo)
                                )
                            )
                        } else {
                            view.visibility = View.INVISIBLE
                        }
                        return@setViewBinder true
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    view.visibility = View.INVISIBLE
                }
            }
            false
        }

        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener { performGetCodeTask(simpleCursorAdapter) }
        btnPermission = view.findViewById(R.id.btn_perm)
        btnPermission.setOnClickListener {
            val activity = activity ?: return@setOnClickListener
            requestReadContactsPermissions(activity)
        }
        viewPermission = view.findViewById(R.id.ll_perm)
        getSnackView()

        btnCountry = view.findViewById(R.id.btnCountry)
        val picker = CountryPicker.newInstance("Select Country")

        tvCountry = view.findViewById(R.id.country)
        val country = Country(getSimCountryIso())
        btnCountry.text = country.isoCode
        tvCountry.setText(country.dialCode)
        val allCountriesList = picker.allCountriesList
        val countryArrayAdapter = MyArrayAdapter(
            activity,
            android.R.layout.simple_spinner_dropdown_item,
            allCountriesList
        )
        tvCountry.setAdapter(countryArrayAdapter)
        tvCountry.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            val code = tvCountry.text.toString()
            if (code.isEmpty()) {
                btnCountry.setText(R.string.usa)
            } else {
                val country = user.country
                val current = picker.getMatchedCountry(code)
                if (current != null && country == current) {
                    setCountrySelected(current)
                } else {
                    val first = picker.getFirstMatchedCountry(code)
                    if (first == null) {
                        btnCountry.setText(R.string.usa)
                    } else {
                        setCountrySelected(first)
                    }
                }
            }
        }
        tvMobile = view.findViewById(R.id.mobile)
        tvMobile.dropDownWidth = resources.displayMetrics.widthPixels
        tvMobile.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            val userCountry = user.country
            if (userCountry != null) {
                tvMobile.setText(
                    MainApplicationSingleton.parseFormatNoCCPhoneNumberByISO(
                        tvMobile.text.toString(),
                        userCountry.isoCode
                    )
                )
            }
        }
        tvMobile.setOnKeyListener { _, keyCode, event ->
            val text = tvMobile.text.toString()
            if (event.action == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_ENTER) {
                if (isContactsPermitted() && !TextUtils.isEmpty(text)) {
                    loaderManager.restartLoader(MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID, null, this)
                }
            }
            false
        }
        val onEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (isPhoneNumberValid()) {
                    performGetCodeTask(simpleCursorAdapter)
                    return@OnEditorActionListener true
                }
            }
            false
        }
        tvMobile.setOnEditorActionListener(onEditorActionListener)
        tvMobile.setAdapter(simpleCursorAdapter)

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin.setOnClickListener { performGetCodeTask(simpleCursorAdapter) }
        val llCountry = view.findViewById<View>(R.id.ll_country)
        llCountry.setOnClickListener {
            picker.setListener(object : CountryPickerListener {
                override fun onSelectCountry(country: Country) {
                    setCountrySelected(country)
                    picker.dismiss()
                }
            })
            picker.show(fragmentManager, "COUNTRY_CODE_PICKER")
        }
        btnCountry.setOnClickListener {
            picker.setListener(object : CountryPickerListener {
                override fun onSelectCountry(country: Country) {
                    setCountrySelected(country)
                    picker.dismiss()
                }
            })
            picker.show(fragmentManager, "COUNTRY_CODE_PICKER")
        }

        tvMobile.requestFocus()
        unPackUser()
    }

    private fun unPackUser() {
        val mobile = user.mobile
        if (!TextUtils.isEmpty(mobile)) {
            tvMobile.setText(mobile)
        }
        var country = user.country
        if (country == null) {
            country = Country(getSimCountryIso())
            setCountrySelected(country)
        } else {
            btnCountry.text = country.isoCode
            tvCountry.setText(country.dialCode)
        }
    }

    fun getSnackView(): View {
        if (snackView == null) {
            snackView = view.findViewById(R.id.username)
        }
        return snackView!!
    }

    fun isContactsPermitted(): Boolean {
        return IntellibitzPermissionFragment.isReadContactsPermissionGranted(activity)
    }

    private fun setPermissionsGranted() {
        loaderManager.initLoader(MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID, null, this)
    }

    override fun onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted()
        setPermissionsGranted()
    }

    override fun onContactsPermissionsDenied() {
        super.onContactsPermissionsGranted()
        val activity = activity ?: return
        snackbar = makeReadContactsSnack(snackbar, getSnackView(), activity)
    }

    private fun setCountrySelected(country: Country) {
        user.country = country
        tvCountry.setText(country.dialCode)
        btnCountry.text = country.isoCode
    }

    private fun packUser(u: ContactItem) {
        user.name = u.name
        user.device = u.device
        user.mobile = u.mobile
        user.countryName = u.countryName
        user.countryCode = u.countryCode
        user.profilePic = u.profilePic
    }

    fun packUser(simpleCursorAdapter: SimpleCursorAdapter): ContactItem {
        val mobile = MainApplicationSingleton.parseFormatNoCCPhoneNumberByISO(
            tvMobile.text.toString(),
            user.country.isoCode
        )
        user.mobile = mobile
        user.countryName = btnCountry.text.toString()
        user.countryCode = tvCountry.text.toString()
        if (simpleCursorAdapter != null) {
            try {
                val cursor = simpleCursorAdapter.cursor
                if (cursor != null && !cursor.isClosed && cursor.count > 0) {
                    setTvUsername(cursor)
                    var photoIndex = -1
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                    } else {
                        photoIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID)
                    }
                    if (photoIndex != -1) {
                        val photo = cursor.getString(photoIndex)
                        if (user.profilePic == null && photo != null) {
                            try {
                                val bm = MediaStore.Images.Media.getBitmap(
                                    activity?.contentResolver,
                                    Uri.parse(photo)
                                )
                                val f = MediaPickerFile.createImageFileInESPublicDir(user.name, ".jpg")
                                if (f == null) {
                                    // Do nothing
                                } else {
                                    MediaPickerUri.bitmapToFile(activity, bm, f)
                                    user.profilePic = f.absolutePath
                                }
                                Log.d(TAG, "User Profile pic: " + user.profilePic)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                Log.e(TAG, e.message)
                            }
                        }
                    }
                }
            } catch (ignored: Throwable) {
                Log.e(TAG, ignored.message)
            }
        }
        return user
    }

    private fun setTvUsername(cursor: Cursor) {
        try {
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            if (nameIndex != -1) {
                try {
                    val name = cursor.getString(nameIndex)
                    var lastName = ""
                    var firstName = ""
                    if (name.split("\\w+".toRegex()).size > 1) {
                        lastName = name.substring(name.lastIndexOf(" ") + 1)
                        firstName = name.substring(0, name.lastIndexOf(' '))
                    } else {
                        firstName = name
                    }
                    user.name = name
                    user.firstName = firstName
                    user.lastName = lastName
                } catch (ignored: Throwable) {
                    Log.e(TAG, ignored.message)
                }
            }
        } catch (ignore: Throwable) {
            Log.e(TAG, "Name cannot be retrieved from Cursor: " + ignore.message)
        }
    }

    private fun getFormattedPhoneNumber(): String? {
        val text = tvMobile?.text?.toString() ?: ""
        return MainApplicationSingleton.parseFormatPhoneNumberByISO(text, user.country.isoCode)
    }

    private fun invalidPhoneNumberAlert(v: View?) {
        if (v == null || v.resources == null) return
        setError(v.resources.getString(R.string.enter_phone_number))
    }

    protected fun isPhoneNumberValid(): Boolean {
        if (tvMobile != null && tvMobile.text.toString().isEmpty()) {
            invalidPhoneNumberAlert(tvMobile)
            return false
        } else {
            val phoneNumber = getFormattedPhoneNumber()
            if (phoneNumber == null) {
                invalidPhoneNumberAlert(tvMobile)
                return false
            }
        }
        setError(null)
        return true
    }

    fun setError(text: String?) {
        tvMobile?.error = text
    }

    private fun getSimCountryIso(): String? {
        val activity = activity ?: return null
        return MainApplicationSingleton.getSimCountryIso(activity)
    }

    fun showProgress() {
        progressDialog = ProgressDialog.show(activity, "Login", "Requesting for OTP", true)
    }

    fun hideProgress() {
        progressDialog?.dismiss()
    }

    fun performGetCodeTask(simpleCursorAdapter: SimpleCursorAdapter) {
        val activity = activity ?: return
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            if (isPhoneNumberValid()) {
                packUser(simpleCursorAdapter)
                setError(null)
                showProgress()
                execGetOTP()
            }
        }
    }

    override fun onPostMobileGetCodeExecute(response: JSONObject) {
        try {
            val status = response.getInt("status")
            if (1 == status) {
                mobileGetCodeListener?.onAccountExists(response, user)
                finishActivity()
            } else if (2 == status) {
                mobileGetCodeListener?.onNewAccount(response, user)
                finishActivity()
            } else if (99 == status) {
                onPostMobileGetCodeExecuteFail(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostMobileGetCodeExecuteFail(response)
        }
        hideProgress()
    }

    private fun finishActivity() {
        val activity = activity
        val intent = activity?.intent
        intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    override fun onPostMobileGetCodeExecuteFail(response: JSONObject) {
        Log.e(TAG, "onPostMobileGetCodeExecuteFail: $response")
        if (response == null) {
            setError("Network fail - Please try again")
        } else {
            setError("Failed to generate OTP - Please try again")
        }
        hideProgress()
    }

    override fun setMobileGetCodeTaskToNull() {
        mobileGetCodeTask = null
    }

    private fun execGetOTP() {
        mobileGetCodeTask = MobileGetCodeTask(user.device, getUserMobileWithCC(), MainApplicationSingleton.MOBILE_GETCODE_URL)
        mobileGetCodeTask?.setMobileGetCodeTaskListener(this)
        mobileGetCodeTask?.execute()
    }

    private fun normalizeUserMobile(): String {
        user.mobile = PhoneNumberUtil.normalizeDigitsOnly(user.mobile)
        return user.mobile
    }

    @NonNull
    private fun getUserMobileWithCC(): String {
        normalizeUserMobile()
        return user.country.dialCode + user.mobile
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == id) {
            var uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val s = tvMobile.text.toString()
            var selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " like ? "
            var selectionArgs = arrayOf("%%")
            if (!TextUtils.isEmpty(s)) {
                selectionArgs = arrayOf("%$s%")
            }
            val CONTENT_PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactFetch.PHOTO_THUMBNAIL_URI,
                ContactFetch.PHOTO_URI,
                ContactFetch.PHOTO_FILE_ID
            )
            return CursorLoader(activity, uri, CONTENT_PROJECTION, selection, selectionArgs, null)
        }
        return null
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        if (activity != null && !activity!!.isFinishing) {
            if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == loader.id) {
                val simpleCursorAdapter = tvMobile.adapter as? SimpleCursorAdapter
                if (simpleCursorAdapter != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        if (!cursor.isClosed) {
                            simpleCursorAdapter.swapCursor(cursor)
                        }
                    } else {
                        if (!cursor.isClosed) {
                            simpleCursorAdapter.changeCursor(cursor)
                        }
                    }
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (activity != null && !activity!!.isFinishing) {
            if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == loader.id) {
                val simpleCursorAdapter = tvMobile.adapter as? SimpleCursorAdapter
                if (simpleCursorAdapter != null) {
                    val data = simpleCursorAdapter.cursor
                    if (data != null && !data.isClosed) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            simpleCursorAdapter.swapCursor(null)
                        } else {
                            simpleCursorAdapter.changeCursor(null)
                        }
                    }
                }
            }
        }
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
    private fun startOTPActivity() {
        val intent = Intent(activity, OTPActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE)
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
     * @param resultCode  the result code send back by the activity
     * @param data        the intent data with extras
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
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
    }

    interface MobileGetCodeListener {
        fun onAccountExists(response: JSONObject, user: ContactItem)
        fun onNewAccount(response: JSONObject, user: ContactItem)
    }

    inner class MyArrayAdapter(context: Context?, resource: Int, objects: List<Country>) : ArrayAdapter<Country>(context, resource, objects) {
        private val originalList: List<Country> = objects
        private val suggestions: ArrayList<Any> = ArrayList()
        private val mContext: Context? = context
        private val countries: MutableList<Country> = ArrayList(objects)
        private val countriesAll: MutableList<Country> = ArrayList(objects)
        private val countriesSuggestion: MutableList<Country> = ArrayList()
        private val mLayoutResourceId: Int = resource

        override fun getCount(): Int {
            return countries.size
        }

        override fun getItem(position: Int): Country? {
            return countries[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            try {
                if (convertView == null) {
                    val inflater = (mContext as Activity).layoutInflater
                    convertView = inflater.inflate(mLayoutResourceId, parent, false)
                }
                val department = getItem(position)
                val name = convertView?.findViewById<TextView>(android.R.id.text1)
                name?.text = department?.dialCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return convertView!!
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun convertResultToString(resultValue: Any): CharSequence {
                    return (resultValue as Country).dialCode
                }

                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filterResults = FilterResults()
                    if (!TextUtils.isEmpty(constraint)) {
                        countriesSuggestion.clear()
                        for (department in countriesAll) {
                            if (department.dialCode.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                                countriesSuggestion.add(department)
                            }
                        }
                        filterResults.values = countriesSuggestion
                        filterResults.count = countriesSuggestion.size
                    }
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                    countries.clear()
                    if (results != null && results.count > 0) {
                        val result = results.values as List<*>
                        for (obj in result) {
                            if (obj is Country) {
                                countries.add(obj)
                            }
                        }
                    } else if (TextUtils.isEmpty(constraint)) {
                        countries.addAll(countriesAll)
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }
}
