package intellibitz.intellidroid.company

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Company
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashSet

class CompanyListFragment :
    IntellibitzUserFragment(),
    CompanyJoinTask.CompanyJoinTaskListener,
    CompanyListTask.CompanyListTaskListener {

    private var tvCompanyName: AutoCompleteTextView? = null
    private var tvCompanyType: AutoCompleteTextView? = null
    private var btnCompany: Button? = null
    private var viewLayout: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var progressDialog: ProgressDialog? = null
    private var recyclerView: RecyclerView? = null
    private val selectedContactItems = HashSet<ContactItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.fragment_companylist, container, false)
        return viewLayout
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            restartLoader()
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        getSnackView()

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin?.setOnClickListener {
            performJoinCompanyTask()
        }
        val btnLogin = view.findViewById<View>(R.id.btn_join)
        btnLogin?.setOnClickListener {
            performJoinCompanyTask()
        }

        val btnSkip = view.findViewById<View>(R.id.btn_skip)
        btnSkip?.setOnClickListener {
            cancelActivity()
        }
    }

    private fun setupRecyclerView(contactItems: Set<ContactItem>?) {
        val view = view ?: return
        if (contactItems == null || contactItems.isEmpty()) {
            recyclerView?.let { rv ->
                Snackbar.make(
                    rv,
                    resources.getText(R.string.note_no_company),
                    Snackbar.LENGTH_SHORT
                ).setAction(android.R.string.ok) {
                    cancelActivity()
                }.show()
            }
            return
        }
        val u = user
        if (u != null && TextUtils.isEmpty(u.companyId)) {
            val contactItem = contactItems.iterator().next()
            u.companyId = contactItem.companyId
            u.companyName = contactItem.companyName
            UserContentProvider.updatesCompanyInDB(u, context)
        }
        val recyclerViewAdapter = RecyclerViewAdapter(contactItems)
        recyclerViewAdapter.setHasStableIds(true)
        val rv = view.findViewById<RecyclerView>(R.id.rv_invites)
        recyclerView = rv
        rv.adapter = recyclerViewAdapter
        recyclerViewAdapter.notifyDataSetChanged()
    }

    private fun restartLoader() {
        val u = user ?: return
        val companyListTask = CompanyListTask(
            u.dataId, u.token,
            u.device, u.deviceRef, u,
            MainApplicationSingleton.AUTH_COMPANY_LIST, context
        )
        companyListTask.requestTimeoutMillis = 30000
        companyListTask.setCompanyListTaskListener(this)
        companyListTask.execute()
    }

    private fun unPackUser() {
        val companyName = user?.countryName
        if (!TextUtils.isEmpty(companyName)) {
            tvCompanyName?.setText(companyName)
        }
        var company = user?.company
        if (null == company) {
            company = Company(
                CompanyPickerFragment.NONE_COMPANY.type,
                CompanyPickerFragment.NONE_COMPANY.typeCode
            )
            setCompanySelected(company)
        } else {
            btnCompany?.text = company.typeCode
            tvCompanyType?.setText(company.type)
        }
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = viewLayout?.findViewById(R.id.username)
        }
        return snackView
    }

    private fun setCompanySelected(company: Company) {
        user?.company = company
        tvCompanyType?.setText(company.type)
        btnCompany?.text = company.typeCode
    }

    private fun packUser(u: ContactItem) {
        user?.name = u.name
        user?.device = u.device
        user?.mobile = u.mobile
        user?.companyName = u.companyName
        user?.companyCode = u.companyCode
        user?.profilePic = u.profilePic
    }

    private fun pickSelectedCompany(): ContactItem? {
        if (selectedContactItems.isNotEmpty()) {
            return selectedContactItems.iterator().next()
        }
        return null
    }

    fun setError(text: String?) {
        if (tvCompanyName != null) {
            tvCompanyName?.error = text
        }
    }

    fun showProgress() {
        progressDialog = ProgressDialog.show(activity, "Create Company", "Connecting to cloud.. Please wait", true)
    }

    fun hideProgress() {
        progressDialog?.dismiss()
    }

    fun performJoinCompanyTask() {
        val act = activity ?: return
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(act)) {
            val contactItem = pickSelectedCompany()
            if (contactItem != null) {
                setError(null)
                showProgress()
                execJoinCompany(contactItem.companyId, contactItem)
            }
        }
    }

    protected fun okActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_OK, intent)
        act.finish()
    }

    override fun onPostCompanyJoinResponse(
        response: JSONObject?,
        invitedCompanyId: String?,
        contactItem: ContactItem?,
        user: ContactItem?
    ) {
        try {
            val status = response?.getInt("status") ?: 0
            if (1 == status) {
                this.user?.companyId = invitedCompanyId
                if (contactItem != null) {
                    this.user?.companyName = contactItem.companyName
                }
                ContactService.asyncUpdateWorkContacts(this.user, context)
                UserContentProvider.updatesCompanyInDB(this.user, context)
                okActivity()
            } else if (99 == status) {
                onPostCompanyJoinErrorResponse(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostCompanyJoinErrorResponse(response)
        }
        hideProgress()
    }

    override fun onPostCompanyJoinErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostCompanyCreateErrorResponse: $response")
        if (null == response) {
            setError("Network fail - Please try again")
        } else {
            setError("Failed to create company - Please try again")
        }
        hideProgress()
    }

    private fun execJoinCompany(invitedCompanyId: String?, contactItem: ContactItem) {
        val u = user ?: return
        val companyJoinTask = CompanyJoinTask(
            invitedCompanyId, contactItem,
            u.dataId, u.token, u.device, u.deviceRef,
            u, MainApplicationSingleton.AUTH_COMPANY_JOIN, context
        )
        companyJoinTask.requestTimeoutMillis = 30000
        companyJoinTask.setCompanyJoinTaskListener(this)
        companyJoinTask.execute()
    }

    override fun onPostCompanyListResponse(response: JSONObject?, user: ContactItem?) {
        try {
            val status = response?.getInt("status") ?: 0
            if (1 == status) {
                val jsonArray = response?.optJSONArray("companies")
                if (jsonArray != null) {
                    val length = jsonArray.length()
                    if (length > 0) {
                        val contactItems = HashSet<ContactItem>(length)
                        for (i in 0 until length) {
                            val company = jsonArray.optJSONObject(i)
                            val cid = company?.optString("company_id")
                            val cname = company?.optString("company_name")
                            val timestamp = company?.optLong("timestamp") ?: 0L
                            val contactItem = ContactItem()
                            contactItem.dataId = cid
                            contactItem.companyId = cid
                            contactItem.companyName = cname
                            contactItem.timestamp = timestamp
                            contactItem.dateTime = MainApplicationSingleton.getDateTimeMillis(timestamp)
                            contactItems.add(contactItem)
                        }
                        setupRecyclerView(contactItems)
                    }
                }
            } else if (99 == status) {
                onPostCompanyListErrorResponse(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostCompanyListErrorResponse(response)
        }
        hideProgress()
    }

    override fun onPostCompanyListErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetInvitesErrorResponse: $response")
        if (null == response) {
            setError("Network fail - Please try again")
        } else {
            setError("Failed to get company invites - Please try again")
        }
        hideProgress()
    }

    private fun setSelectedItems(contactItem: ContactItem, selected: Boolean) {
        if (selected) {
            selectedContactItems.add(contactItem)
        } else {
            selectedContactItems.remove(contactItem)
        }
    }

    inner class RecyclerViewAdapter(items: Collection<ContactItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        private val items: MutableList<ContactItem> = ArrayList(items)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_companylist_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.contactItem = items[position]
            holder.tvName.text = holder.contactItem?.companyName
            holder.tvName.tag = holder.contactItem?.companyId
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            val tvName: TextView = mView.findViewById(R.id.tv_companyname)
            val ctv: CheckBox = mView.findViewById(R.id.cb_companylist)
            var contactItem: ContactItem? = null

            init {
                ctv.setOnClickListener {
                    contactItem?.let { item ->
                        setSelectedItems(item, ctv.isChecked)
                    }
                }
            }

            override fun toString(): String {
                return super.toString() + " '" + contactItem + "'"
            }
        }
    }

    companion object {
        const val TAG = "CompListFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?): CompanyListFragment {
            val fragment = CompanyListFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
