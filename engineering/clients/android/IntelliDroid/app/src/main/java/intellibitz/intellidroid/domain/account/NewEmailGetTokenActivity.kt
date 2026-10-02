package intellibitz.intellidroid.domain.account

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.GetTokenTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NewEmailGetTokenActivity : AppCompatActivity(), GetTokenTask.GetTokenTaskListener {

    private var mProgressView: View? = null
    private var user: ContactItem? = null
    private var emailGoogleTokenTask: GetTokenTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_token)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        mProgressView = findViewById(R.id.token_progress)

        val currentUser = user
        if (currentUser != null && MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(this)) {
            showProgress(true)
            val task = GetTokenTask(
                currentUser.email, currentUser.emailCode,
                currentUser.dataId, currentUser.token, currentUser.device, currentUser.deviceRef,
                MainApplicationSingleton.AUTH_EMAIL_GOOGLE_TOKEN
            )
            task.setGetTokenTaskListener(this)
            emailGoogleTokenTask = task
            task.execute()
        }
    }

    private fun showProgress(show: Boolean) {
        val progressView = mProgressView ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
            progressView.visibility = if (show) View.VISIBLE else View.GONE
            progressView.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha(if (show) 1f else 0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progressView.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            progressView.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun addEmailAccount() {
        val currentUser = user ?: return
        try {
            Log.d(TAG, currentUser.toString())
            val email = currentUser.email
            currentUser.addEmail(email, currentUser.emailCode)
            val values = ContentValues().apply {
                put(ContactItem.USER_CONTACT, MainApplicationSingleton.Serializer.serialize(currentUser))
            }
            val contentResolver = contentResolver
            val uri = contentResolver.insert(
                Uri.withAppendedPath(UserEmailContentProvider.CONTENT_URI, email),
                values
            )
            Log.e(TAG, "SUCCESS - User: $currentUser")
            Log.e(TAG, "SUCCESS - Email insert: $uri")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onPostGetTokenTaskExecute(response: JSONObject?) {
        if (response == null) {
            onPostGetTokenTaskExecuteFail(null)
            return
        }
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status) {
                val intent = intent.apply {
                    putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                }
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            } else if (1 == status) {
                val email = response.getString("email")
                if (!TextUtils.isEmpty(email)) {
                    user?.email = email.trim()
                }

                addEmailAccount()

                val registrationComplete = Intent(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED).apply {
                    putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)

                val intent = intent.apply {
                    putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        finish()
        showProgress(false)
    }

    override fun onPostGetTokenTaskExecuteFail(response: JSONObject?) {
        val intent = intent.apply {
            putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        }
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
        showProgress(false)
    }

    override fun setGetTokenTaskToNull() {
        emailGoogleTokenTask = null
    }

    companion object {
        private const val TAG = "GETToken"
    }
}
