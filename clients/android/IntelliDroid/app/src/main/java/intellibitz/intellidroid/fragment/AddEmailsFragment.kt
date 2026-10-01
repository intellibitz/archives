package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.AddEmailActivity
import intellibitz.intellidroid.activity.EmailAccountDetailActivity
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.UserEmailJoinColumns
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.util.ArrayList
import java.util.Collection

/**
 *
 */
class AddEmailsFragment : IntellibitzUserFragment() {
    private var btnNext: Button? = null
    private var btnBack: ImageButton? = null

    private var view: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var progressBar: View? = null
    private var recyclerView: RecyclerView? = null
    var emailAccountRemovedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupRecyclerView()
        }
    }
    var emailAccountAddedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupRecyclerView()
        }
    }

    constructor() : super()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onPause() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(emailAccountAddedReceiver);
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(emailAccountRemovedReceiver);
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        /*
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(emailAccountAddedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(emailAccountRemovedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED));
*/
        setupRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_addemails, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments!!.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        btnBack = view.findViewById<View>(R.id.btn_back) as ImageButton
        btnBack!!.setOnClickListener { activity!!.onBackPressed() }
        btnNext = view.findViewById<View>(R.id.btn_next) as Button
        btnNext!!.setOnClickListener { startAddEmailActivity() }
        getSnackView()

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin.setOnClickListener { startAddEmailActivity() }
        recyclerView = view.findViewById<View>(R.id.rv_emails) as RecyclerView
        setupRecyclerView()
    }

    fun getSnackView(): View? {
        if (null == snackView) //            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view!!.findViewById(R.id.username)
        return snackView
    }

    private fun setupRecyclerView() {
        val emails = user!!.contactItems
        if (null == emails || emails.isEmpty()) {
//                EmailSyncHandler.SyncEmailsFromCloud();
//            starts the sync from cloud
            /*
            Context context = AddEmailsFragment.this;
            Intent intent = new Intent(context,
                    UserEmailIntentService.class);
            intent.setAction(UserEmailIntentService.ACTION_SYNC_MESSAGE_ATTACHMENTS);
            intent.putExtra(ContactItem.TAG, (Serializable) user);
            context.startService(intent);
*/
            Snackbar.make(
                recyclerView!!,
//                    "Please click add button to add new email account",
                getResources().getText(R.string.add_email_snack),
                Snackbar.LENGTH_SHORT
            )
                .setAction(android.R.string.ok) { startAddEmailActivity() }.show()
//            startNewAccountActivity();
//            startAddEmailActivity();
        } else {
            /*
            items = new ArrayList<>(emails.size());
            for (String email : emails) {
                items.add(new ContactItem(email, email, email));
            }
*/
        }
        recyclerView!!.adapter = RecyclerViewAdapter(user!!.contactItems)
    }

    private fun packUserEmailsFromDB() {
        val cursor = activity!!.contentResolver.query(
            ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI, user!!._id),
            null, null, null, null
        )
        if (null == cursor) return
        if (cursor.count > 0) {
            packUserEmailsFromCursor(cursor)
            cursor.close()
        }
    }

    private fun packUserEmailsFromCursor(cursor: Cursor) {
        do {
            val email = cursor.getString(
                cursor.getColumnIndex(
                    UserEmailJoinColumns.KEY_EMAIL
                )
            )
            if (email != null) {
                val emid = cursor.getLong(cursor.getColumnIndex("emid"))
                val name = cursor.getString(cursor.getColumnIndex("emname"))
                val item = ContactItem(email, name, email)
                item.set_id(emid)
                user!!.addEmail(item)
            }
        } while (cursor.moveToNext())
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
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE)

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    private fun startEmailAccountDetailActivity(user: ContactItem?, userEmailItem: ContactItem?) {
        val intent = Intent(activity, EmailAccountDetailActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
        intent.putExtra(ContactItem.TAG, userEmailItem as Parcelable?)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE)
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
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val activity = activity
                    val intent = activity!!.intent
                    intent.putExtra(ContactItem.USER_CONTACT, item as Parcelable?)
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val activity = activity
                val intent = activity!!.intent
                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
                activity.setResult(Activity.RESULT_CANCELED, intent)
                activity.finish()
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }
        if (MainApplicationSingleton.ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    user = data.getParcelableExtra(ContactItem.USER_CONTACT)
                    setupRecyclerView()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: cancelled - ")
            }
        }
    }

    inner class RecyclerViewAdapter(items: Collection<ContactItem>?) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        private val items: MutableList<ContactItem> = ArrayList()

        init {
            this.items.addAll(items!!)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_addemails_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.userEmailItem = items[position]
//            holder.tvFrom.setText(items.get(position).id);
//            holder.mContentView.setText(items.get(position).getName());
            val rootView = holder.mView.rootView as ViewGroup
            val linearLayout = rootView.findViewById<View>(R.id.ll_addemails_rv) as LinearLayout
//            clears old views
            linearLayout.removeAllViews()
            val view = LayoutInflater.from(holder.mView.context).inflate(
                R.layout.fragment_addemails_rv_dyn,
                holder.mView.rootView as ViewGroup, false
            )
            val tv = view.findViewById<View>(R.id.tv_addemails_rv_dyn) as TextView
            tv.text = holder.userEmailItem!!.name
            linearLayout.addView(view)

            holder.mView.setOnClickListener {
                user!!.email = holder.userEmailItem!!.email
                startEmailAccountDetailActivity(user, holder.userEmailItem)
                /*
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EmailAccountDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());
                        user.setEmailItem(holder.userEmailItem.getEmail());
                        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.emailaccount_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EmailAccountDetailActivity.class);
                        user.setEmailItem(holder.userEmailItem.getEmail());
                        intent.putExtra(ContactItem.TAG, (Parcelable) user);
                        intent.putExtra(EmailAccountDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());

                        context.startActivity(intent);
                    }
*/
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mView: View = view
            var userEmailItem: ContactItem? = null

            override fun toString(): String {
                return super.toString() + " '" + userEmailItem + "'"
            }
        }
    }

    companion object {
        private const val TAG = "AddEmailsFragment"

        fun newInstance(user: ContactItem?): AddEmailsFragment {
            val fragment = AddEmailsFragment()
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
