package intellibitz.intellidroid.company

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
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
import android.widget.TextView
import androidx.annotation.Nullable
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Company
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class CompanyCreateFragment : IntellibitzUserFragment(), CompanyCreateTask.CompanyCreateTaskListener {

    private var tvCompanyName: AutoCompleteTextView? = null
    private var tvCompanyType: AutoCompleteTextView? = null
    private var btnCompany: Button? = null
    private var viewLayout: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.fragment_companycreate, container, false)
        return viewLayout
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        val btnLogin = view.findViewById<View>(R.id.btn_login)
        btnLogin?.setOnClickListener {
            performCreateCompanyTask()
        }
        getSnackView()

        btnCompany = view.findViewById<Button>(R.id.btnCompany)
        val companyPickerFragment = CompanyPickerFragment.newInstance("Select Company")

        tvCompanyType = view.findViewById<AutoCompleteTextView>(R.id.tv_companytype)
        val company = Company(
            CompanyPickerFragment.NONE_COMPANY.type,
            CompanyPickerFragment.NONE_COMPANY.typeCode
        )
        btnCompany?.text = company.typeCode
        tvCompanyType?.setText(company.type)

        val allCompanyTypes = ArrayList<Company>(companyPickerFragment.getAllCompanyTypes() ?: emptyList())
        val companyArrayAdapter = MyArrayAdapter(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, allCompanyTypes
        )
        tvCompanyType?.setAdapter(companyArrayAdapter)
        tvCompanyType?.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
            val code = tvCompanyType?.text.toString()
            if (code.isEmpty()) {
                btnCompany?.setText(R.string.usa)
            } else {
                val currentCompany = user?.company
                val current = companyPickerFragment.getMatchedCompany(code)
                if (current != null && currentCompany == current) {
                    setCompanySelected(current)
                } else {
                    val first = companyPickerFragment.getFirstMatchedCompany(code)
                    if (null == first) {
                        btnCompany?.setText(R.string.usa)
                    } else {
                        setCompanySelected(first)
                    }
                }
            }
        }
        tvCompanyName = view.findViewById<AutoCompleteTextView>(R.id.tv_companyname)
        tvCompanyName?.dropDownWidth = resources.displayMetrics.widthPixels
        tvCompanyName?.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
            val userCompany = user?.company
            if (userCompany != null) {
                tvCompanyName?.setText(
                    MainApplicationSingleton.parseFormatNoCCPhoneNumberByISO(
                        tvCompanyName?.text.toString(), userCompany.typeCode
                    )
                )
            }
        }
        tvCompanyName?.setOnKeyListener { _, keyCode, event ->
            val text = tvCompanyName?.text.toString()
            if (event.action == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_ENTER) {
            }
            false
        }
        val onEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performCreateCompanyTask()
                true
            } else {
                false
            }
        }
        tvCompanyName?.setOnEditorActionListener(onEditorActionListener)

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin?.setOnClickListener {
            performCreateCompanyTask()
        }

        val onClickListener = View.OnClickListener {
            companyPickerFragment.setListener(object : CompanyPickerListener {
                override fun onSelectCompany(company: Company?) {
                    if (company != null) {
                        setCompanySelected(company)
                    }
                    companyPickerFragment.dismiss()
                }
            })
            companyPickerFragment.show(requireActivity().supportFragmentManager, "COUNTRY_CODE_PICKER")
        }
        val llCompany = view.findViewById<View>(R.id.ll_company)
        llCompany?.setOnClickListener(onClickListener)
        btnCompany?.setOnClickListener(onClickListener)

        tvCompanyName?.requestFocus()
        unPackUser()
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

    private fun packUser(): Boolean {
        val companyName = tvCompanyName?.text.toString()
        if (TextUtils.isEmpty(companyName)) {
            setError("Company name is required")
            return false
        }
        user?.companyName = companyName
        return true
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

    fun performCreateCompanyTask() {
        val act = activity ?: return
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(act)) {
            if (packUser()) {
                setError(null)
                showProgress()
                execCreateCompany()
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

    override fun onPostCompanyCreateResponse(response: JSONObject?, companyName: String?, user: ContactItem?) {
        try {
            val status = response?.getInt("status") ?: 0
            if (1 == status) {
                val cid = response?.getString("company_id")
                this.user?.companyId = cid
                UserContentProvider.updatesCompanyInDB(this.user, context)
                okActivity()
            } else if (99 == status) {
                onPostCompanyCreateErrorResponse(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostCompanyCreateErrorResponse(response)
        }
        hideProgress()
    }

    override fun onPostCompanyCreateErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostCompanyCreateErrorResponse: $response")
        if (null == response) {
            setError("Network fail - Please try again")
        } else {
            setError("Failed to create company - Please try again")
        }
        hideProgress()
    }

    private fun execCreateCompany() {
        val u = user ?: return
        val companyCreateTask = CompanyCreateTask(
            u.companyName, u.dataId, u.token,
            u.device, u.deviceRef, u, MainApplicationSingleton.AUTH_COMPANY_CREATE, context
        )
        companyCreateTask.requestTimeoutMillis = 30000
        companyCreateTask.setCompanyCreateTaskListener(this)
        companyCreateTask.execute()
    }

    internal class MyArrayAdapter(
        private val mContext: Context,
        private val mLayoutResourceId: Int,
        allCompaniesList: List<Company>
    ) : ArrayAdapter<Company>(mContext, mLayoutResourceId, allCompaniesList) {

        private val originalList: List<Company> = allCompaniesList
        private val suggestions = ArrayList<Any>()
        private val companies: MutableList<Company> = ArrayList(allCompaniesList)
        private val companiesAll: List<Company> = ArrayList(allCompaniesList)
        private val companiesSuggestion: MutableList<Company> = ArrayList()

        override fun getCount(): Int {
            return companies.size
        }

        override fun getItem(position: Int): Company? {
            return companies[position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var row = convertView
            try {
                if (row == null) {
                    val inflater = (mContext as Activity).layoutInflater
                    row = inflater.inflate(mLayoutResourceId, parent, false)
                }
                val department = getItem(position)
                val name = row?.findViewById<TextView>(android.R.id.text1)
                name?.text = department?.type
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return row!!
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun convertResultToString(resultValue: Any?): CharSequence {
                    return (resultValue as? Company)?.type ?: ""
                }

                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filterResults = FilterResults()
                    if (!TextUtils.isEmpty(constraint)) {
                        companiesSuggestion.clear()
                        for (department in companiesAll) {
                            if (department.type?.lowercase()?.startsWith(
                                    constraint.toString().lowercase()
                                ) == true
                            ) {
                                companiesSuggestion.add(department)
                            }
                        }
                        filterResults.values = companiesSuggestion
                        filterResults.count = companiesSuggestion.size
                    }
                    return filterResults
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    companies.clear()
                    if (results != null && results.count > 0) {
                        val result = results.values as? List<*>
                        if (result != null) {
                            for (obj in result) {
                                if (obj is Company) {
                                    companies.add(obj)
                                }
                            }
                        }
                    } else if (TextUtils.isEmpty(constraint)) {
                        companies.addAll(companiesAll)
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        const val TAG = "CompCreateFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?): CompanyCreateFragment {
            val fragment = CompanyCreateFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
