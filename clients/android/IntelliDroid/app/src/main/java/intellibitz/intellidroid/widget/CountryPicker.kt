package intellibitz.intellidroid.widget

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.i18n.phonenumbers.PhoneNumberUtil
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Country
import java.util.ArrayList
import java.util.Collections
import java.util.Currency
import java.util.Locale

class CountryPicker : AppCompatDialogFragment(), Comparator<Country> {

    var searchEditText: EditText? = null
        private set
    var countryListView: ListView? = null
        private set
    private var adapter: CountryListAdapter? = null
    var allCountriesList: MutableList<Country>? = null
        private set
    private var selectedCountriesList: MutableList<Country>? = null
    private var listener: CountryPickerListener? = null

    init {
        allCountries()
    }

    fun setListener(listener: CountryPickerListener?) {
        this.listener = listener
    }

    private fun allCountries(): List<Country>? {
        if (allCountriesList == null) {
            val all = ArrayList<Country>()
            val supportedRegions = PhoneNumberUtil.getInstance().supportedRegions

            for (countryIso in supportedRegions) {
                val country = Country(countryIso)
                all.add(country)
            }

            Collections.sort(all, this)

            val selected = ArrayList<Country>()
            selected.addAll(all)

            allCountriesList = all
            selectedCountriesList = selected

            return all
        }
        return null
    }

    fun getMatchedCountry(code: String): Country? {
        selectedCountriesList?.let {
            for (country in it) {
                if (code.equals(country.dialCode, ignoreCase = true)) {
                    return country
                }
            }
        }
        return null
    }

    fun getFirstMatchedCountry(code: String): Country? {
        allCountriesList?.let {
            for (country in it) {
                if (code.equals(country.dialCode, ignoreCase = true)) {
                    return country
                }
            }
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.country_picker, container, false)
        val args = arguments
        if (args != null) {
            val dialogTitle = args.getString("dialogTitle")
            dialog?.setTitle(dialogTitle)
            dialog?.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        searchEditText = view.findViewById(R.id.country_code_picker_search)
        countryListView = view.findViewById(R.id.country_code_picker_listview)

        adapter = CountryListAdapter(requireActivity(), selectedCountriesList ?: ArrayList())
        countryListView?.adapter = adapter

        countryListView?.setOnItemClickListener { _, _, position, _ ->
            selectedCountriesList?.let { list ->
                if (position in list.indices) {
                    val country = list[position]
                    listener?.onSelectCountry(country)
                }
            }
        }

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                search(s.toString())
            }
        })

        return view
    }

    @SuppressLint("DefaultLocale")
    private fun search(text: String) {
        val selected = selectedCountriesList ?: return
        val all = allCountriesList ?: return

        selected.clear()
        val query = text.lowercase(Locale.ENGLISH)

        for (country in all) {
            val name = country.name?.lowercase(Locale.ENGLISH) ?: ""
            val dialCode = country.dialCode?.lowercase(Locale.ENGLISH) ?: ""
            if (name.contains(query) || dialCode.contains(query)) {
                selected.add(country)
            }
        }

        adapter?.notifyDataSetChanged()
    }

    override fun compare(lhs: Country, rhs: Country): Int {
        val lName = lhs.name ?: ""
        val rName = rhs.name ?: ""
        return lName.compareTo(rName)
    }

    companion object {
        @JvmStatic
        fun getCurrencyCode(countryCode: String): Currency? {
            return try {
                Currency.getInstance(Locale("en", countryCode))
            } catch (e: Exception) {
                null
            }
        }

        @JvmStatic
        fun newInstance(dialogTitle: String): CountryPicker {
            val picker = CountryPicker()
            val bundle = Bundle()
            bundle.putString("dialogTitle", dialogTitle)
            picker.arguments = bundle
            return picker
        }
    }
}
