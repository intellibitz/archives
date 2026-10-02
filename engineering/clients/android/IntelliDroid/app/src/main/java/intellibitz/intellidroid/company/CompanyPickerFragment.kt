package intellibitz.intellidroid.company

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatDialogFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Company
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.util.Collections

class CompanyPickerFragment : AppCompatDialogFragment(), Comparator<Company> {

    private var searchEditText: EditText? = null
    private var companyListView: ListView? = null
    private var allCompanyTypes: MutableSet<Company>? = null
    private var selectedCompaniesList: MutableList<Company>? = null
    private var listener: CompanyPickerListener? = null

    init {
        getAllCompanyTypesList()
    }

    fun getAllCompanyTypes(): Set<Company>? {
        return allCompanyTypes
    }

    fun setListener(listener: CompanyPickerListener?) {
        this.listener = listener
    }

    fun getSearchEditText(): EditText? {
        return searchEditText
    }

    fun getCompanyListView(): ListView? {
        return companyListView
    }

    private fun getAllCompanyTypesList(): List<Company> {
        allCompanyTypes = HashSet()

        for (typeCode in MainApplicationSingleton.typeCodeMAP.keys) {
            val company = Company()
            val type = MainApplicationSingleton.typeCodeMAP[typeCode]
            company.type = type
            company.typeCode = typeCode
            allCompanyTypes?.add(company)
        }

        val sortedList = ArrayList(allCompanyTypes!!)
        Collections.sort(sortedList, this)

        selectedCompaniesList = ArrayList()
        selectedCompaniesList?.addAll(sortedList)

        return sortedList
    }

    fun getMatchedCompany(code: String?): Company? {
        selectedCompaniesList?.let { list ->
            for (company in list) {
                if (code.equals(company.type, ignoreCase = true)) {
                    return company
                }
            }
        }
        return null
    }

    fun getFirstMatchedCompany(code: String?): Company? {
        allCompanyTypes?.let { set ->
            for (company in set) {
                if (code.equals(company.type, ignoreCase = true)) {
                    return company
                }
            }
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.company_picker, null)
        val args = arguments
        if (args != null) {
            val dialogTitle = args.getString("dialogTitle")
            dialog?.setTitle(dialogTitle)
            dialog?.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        searchEditText = view.findViewById(R.id.company_code_picker_search)
        companyListView = view.findViewById(R.id.company_code_picker_listview)

        val adapter = CompanyListAdapter(activity, selectedCompaniesList ?: emptyList())
        companyListView?.adapter = adapter

        companyListView?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (listener != null) {
                    val company = selectedCompaniesList?.get(position)
                    listener?.onSelectCompany(company)
                }
            }

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                search(s.toString())
            }
        })

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun search(text: String) {
        selectedCompaniesList?.clear()

        allCompanyTypes?.let { set ->
            for (company in set) {
                if (company.type?.contains(text.lowercase()) == true) {
                    selectedCompaniesList?.add(company)
                }
            }
        }

        companyListView?.requestLayout()
    }

    override fun compare(lhs: Company?, rhs: Company?): Int {
        val l = lhs?.type ?: ""
        val r = rhs?.type ?: ""
        return l.compareTo(r)
    }

    companion object {
        @JvmField
        val NONE_COMPANY = Company("UnRegistered", "None")

        @JvmStatic
        fun newInstance(dialogTitle: String?): CompanyPickerFragment {
            val picker = CompanyPickerFragment()
            val bundle = Bundle()
            bundle.putString("dialogTitle", dialogTitle)
            picker.arguments = bundle
            return picker
        }
    }
}
