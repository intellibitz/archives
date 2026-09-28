package intellibitz.intellidroid.domain.account

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.EmailAccountDetailActivity
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.UserEmailJoinColumns
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.util.ArrayList

class EmailAccountListActivity : AppCompatActivity() {

    private var mTwoPane: Boolean = false
    private var recyclerView: RecyclerView? = null
    private var user: ContactItem? = null

    private val emailAccountRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupRecyclerView()
        }
    }

    private val emailAccountAddedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupRecyclerView()
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountAddedReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountRemovedReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            emailAccountAddedReceiver,
            IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            emailAccountRemovedReceiver,
            IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED)
        )
        setupRecyclerView()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emailaccount_list)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)

        val toolbar = findViewById<Toolbar>(R.id.email_list_toolbar)
        toolbar?.setTitle(R.string.toolbar_emailaccounts_title)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            startNewAccountActivity()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (findViewById<View>(R.id.emailaccount_detail_container) != null) {
            mTwoPane = true
        }

        recyclerView = findViewById(R.id.emailaccount_list)
        setupRecyclerView()
    }

    private fun startNewAccountActivity() {
        val intent = Intent(this, NewEmailAccountActivity::class.java).apply {
            putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        }
        startActivityForResult(intent, EMAIL_ACCOUNT_RESULT)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (EMAIL_ACCOUNT_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT)
                setupRecyclerView()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val currentUser = user ?: return
        val rv = recyclerView ?: return
        val emails = currentUser.contactItems
        if (emails == null || emails.isEmpty()) {
            Snackbar.make(
                rv,
                "Please click add button to add new email account",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok, null).show()
        }
        rv.adapter = RecyclerViewAdapter(currentUser.contactItems)
    }

    private fun packUserEmailsFromDB() {
        val currentUser = user ?: return
        val cursor = contentResolver.query(
            ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI, currentUser._id),
            null, null, null, null
        ) ?: return
        if (cursor.count > 0) {
            packUserEmailsFromCursor(cursor)
            cursor.close()
        }
    }

    private fun packUserEmailsFromCursor(cursor: Cursor) {
        val currentUser = user ?: return
        do {
            val email = cursor.getString(cursor.getColumnIndexOrThrow(UserEmailJoinColumns.KEY_EMAIL))
            if (email != null) {
                val emid = cursor.getLong(cursor.getColumnIndexOrThrow("emid"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("emname"))
                val item = ContactItem(email, name, email)
                item._id = emid
                currentUser.addEmail(item)
            }
        } while (cursor.moveToNext())
    }

    inner class RecyclerViewAdapter(items: Collection<ContactItem>?) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        private val items: MutableList<ContactItem> = ArrayList()

        init {
            if (items != null) {
                this.items.addAll(items)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.emailaccount_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.mItem = item
            holder.mContentView.text = item.name

            holder.mView.setOnClickListener { v ->
                val currentUser = user ?: return@setOnClickListener
                if (mTwoPane) {
                    val arguments = Bundle()
                    currentUser.email = holder.mItem?.email
                    val fragment = EmailAccountDetailFragment().apply {
                        this.arguments = arguments
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.emailaccount_detail_container, fragment)
                        .commit()
                } else {
                    val context = v.context
                    val intent = Intent(context, EmailAccountDetailActivity::class.java).apply {
                        currentUser.email = holder.mItem?.email
                        putExtra(ContactItem.USER_CONTACT, currentUser as Parcelable)
                    }
                    context.startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView? = mView.findViewById(R.id.tv_id)
            val mContentView: TextView = mView.findViewById(R.id.content)
            var mItem: ContactItem? = null

            override fun toString(): String {
                return super.toString() + " '" + mContentView.text + "'"
            }
        }
    }

    companion object {
        private const val EMAIL_ACCOUNT_RESULT = 210
    }
}
