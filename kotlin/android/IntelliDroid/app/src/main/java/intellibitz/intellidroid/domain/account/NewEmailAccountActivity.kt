package intellibitz.intellidroid.domain.account

import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.AddEmailTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class NewEmailAccountActivity :
    AppCompatActivity(),
    AddEmailTask.AddEmailTaskListener {

    private var addEmailTask: AddEmailTask? = null

    private var tvEmail: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null
    private var user: ContactItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)

        tvEmail = findViewById<AutoCompleteTextView>(R.id.email).apply {
            populateAutoComplete()
        }

        mPasswordView = findViewById<EditText>(R.id.password).apply {
            setOnEditorActionListener { _, id, _ ->
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin()
                    true
                } else {
                    false
                }
            }
        }

        val mEmailSignInButton = findViewById<Button>(R.id.email_sign_in_button)
        mEmailSignInButton?.setOnClickListener { attemptLogin() }

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)

        val bundle = intent.extras
        if (bundle != null) {
            tvEmail?.setText(bundle.getString("email"))
            setError(bundle.getString("error"), "Action")
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack?.setOnClickListener { finish() }
    }

    private fun setError(title: String?, action: String) {
        val loginForm = mLoginFormView ?: return
        if (title != null) {
            Snackbar.make(loginForm, title, Snackbar.LENGTH_LONG)
                .setAction(action, null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_addemail_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == resources.getString(R.string.menu_title_cancel)) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateAutoComplete() {
        if (mayRequestContacts()) {
            SetupEmailAutoCompleteTask().execute()
        }
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
            tvEmail?.let {
                Snackbar.make(it, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
                    }.show()
            }
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private fun attemptLogin() {
        if (addEmailTask != null) return

        tvEmail?.error = null
        mPasswordView?.error = null

        var email = tvEmail?.text?.toString() ?: ""
        if (!TextUtils.isEmpty(email)) {
            user?.email = email.trim()
            email = user?.email ?: ""
        }
        val password = mPasswordView?.text?.toString() ?: ""

        var cancel = false
        var focusView: View? = null

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView?.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        if (TextUtils.isEmpty(email)) {
            tvEmail?.error = getString(R.string.error_field_required)
            focusView = tvEmail
            cancel = true
        } else if (!isEmailValid(email)) {
            tvEmail?.error = getString(R.string.error_invalid_email)
            focusView = tvEmail
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(this)) {
                showProgress(true)
                execAddEmailTask(password)
            } else {
                tvEmail?.error = "No network connection available."
                setError("No network connection available.", "Action")
            }
        }
    }

    private fun execAddEmailTask(password: String) {
        val currentUser = user ?: return
        val task = AddEmailTask(
            currentUser.email, password,
            currentUser.dataId, currentUser.token, currentUser.device, currentUser.deviceRef,
            MainApplicationSingleton.AUTH_ADD_EMAIL
        )
        task.setAddEmailTaskListener(this)
        addEmailTask = task
        task.execute()
    }

    private fun isEmailValid(email: String): Boolean = email.contains("@")

    private fun isPasswordValid(password: String): Boolean = password.length > 4

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        val loginForm = mLoginFormView ?: return
        val progress = mProgressView ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            loginForm.visibility = if (show) View.GONE else View.VISIBLE
            loginForm.animate().setDuration(shortAnimTime).alpha(
                if (show) 0f else 1f
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loginForm.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            progress.visibility = if (show) View.VISIBLE else View.GONE
            progress.animate().setDuration(shortAnimTime).alpha(
                if (show) 1f else 0f
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progress.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            progress.visibility = if (show) View.VISIBLE else View.GONE
            loginForm.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        val adapter = ArrayAdapter(
            this@NewEmailAccountActivity,
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection
        )
        tvEmail?.setAdapter(adapter)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (GET_CODE_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT)
                val intent = Intent(this, NewEmailGetTokenActivity::class.java).apply {
                    putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                }
                startActivityForResult(intent, GET_TOKEN_RESULT)
            }
        } else if (GET_TOKEN_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT)
                val intent = intent.apply {
                    putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onPostAddEmailTaskExecute(response: JSONObject?) {
        addEmailTask = null
        showProgress(false)

        if (response != null) {
            mPasswordView?.error = response.toString()
            setError(response.toString(), "Action")
            try {
                val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
                if (1 == status) {
                    val url = response.getString(MainApplicationSingleton.URL_PARAM)
                    user?.emailURL = url
                    val intent = Intent(this@NewEmailAccountActivity, WebViewActivity::class.java).apply {
                        putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                    }
                    startActivityForResult(intent, GET_CODE_RESULT)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPostAddEmailTaskExecuteFail(response: JSONObject?) {
        addEmailTask = null
        showProgress(false)
        mPasswordView?.requestFocus()
    }

    override fun setAddEmailTaskToNull() {
        addEmailTask = null
    }

    @Suppress("DEPRECATION")
    private inner class SetupEmailAutoCompleteTask : AsyncTask<Void, Void, List<String>>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg voids: Void?): List<String> {
            val emailAddressCollection = ArrayList<String>()
            val cr: ContentResolver = contentResolver
            val emailCur: Cursor? = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                null, null, null
            )
            if (emailCur != null) {
                while (emailCur.moveToNext()) {
                    val email = emailCur.getString(
                        emailCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)
                    )
                    emailAddressCollection.add(email)
                }
                emailCur.close()
            }
            return emailAddressCollection
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(emailAddressCollection: List<String>) {
            addEmailsToAutoComplete(emailAddressCollection)
        }
    }

    companion object {
        private const val REQUEST_READ_CONTACTS = 21
        private const val GET_CODE_RESULT = 11
        private const val GET_TOKEN_RESULT = 12
    }
}
