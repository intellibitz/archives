package intellibitz.intellidroid.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import intellibitz.intellidroid.bean.Country;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.Country;

import intellibitz.intellidroid.bean.Country;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.appcompat.app.AppCompatDialogFragment;

public class CountryPicker extends AppCompatDialogFragment implements
        Comparator<Country> {

    private EditText searchEditText;
    private ListView countryListView;
    private CountryListAdapter adapter;
    private List<Country> allCountriesList;
    private List<Country> selectedCountriesList;
    private CountryPickerListener listener;

    public CountryPicker() {
        super();
        getAllCountries();
    }

    public static Currency getCurrencyCode(String countryCode) {
        try {
            return Currency.getInstance(new Locale("en", countryCode));
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * To support show as dialog
     *
     * @param dialogTitle
     * @return
     */
    public static CountryPicker newInstance(String dialogTitle) {
        CountryPicker picker = new CountryPicker();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    public List<Country> getAllCountriesList() {
        return allCountriesList;
    }

    public void setListener(CountryPickerListener listener) {
        this.listener = listener;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCountryListView() {
        return countryListView;
    }

    private List<Country> getAllCountries() {
        if (allCountriesList == null) {
            allCountriesList = new ArrayList<>();

            Set<String> supportedRegions = PhoneNumberUtil.getInstance().getSupportedRegions();

            for (String countryIso : supportedRegions) {
                Country country = new Country(countryIso);
                allCountriesList.add(country);
            }

            Collections.sort(allCountriesList, this);

            selectedCountriesList = new ArrayList<>();
            selectedCountriesList.addAll(allCountriesList);

            // Return
            return allCountriesList;
        }
        return null;
    }

/*
    private static String readEncodedJsonString(Context context)
			throws java.io.IOException {
		String base64 = context.getResources().getString(R.string.countries_code);
		byte[] data = Base64.decode(base64, Base64.DEFAULT);
		return new String(data, "UTF-8");
	}
*/

    private List<Country> getAllCountries_() {
        if (allCountriesList == null) {
            try {
                allCountriesList = new ArrayList<>();

//				String allCountriesCode = readEncodedJsonString(getActivity());

//				JSONArray countrArray = new JSONArray(allCountriesCode);
                JSONArray countrArray = new JSONArray();

                for (int i = 0; i < countrArray.length(); i++) {
                    JSONObject jsonObject = countrArray.getJSONObject(i);
                    String countryName = jsonObject.getString("name");
                    String countryDialCode = jsonObject.getString("dial_code");
                    String countryCode = jsonObject.getString("code");

                    Country country = new Country();
                    country.setIsoCode(countryCode);
                    country.setName(countryName);
                    country.setDialCode(countryDialCode);
                    allCountriesList.add(country);
                }

                Collections.sort(allCountriesList, this);

                selectedCountriesList = new ArrayList<Country>();
                selectedCountriesList.addAll(allCountriesList);

                // Return
                return allCountriesList;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Country getMatchedCountry(String code) {
        for (Country country : selectedCountriesList) {
            if (code.equalsIgnoreCase(country.getDialCode()))
                return country;
        }
        return null;
    }

    public Country getFirstMatchedCountry(String code) {
        for (Country country : allCountriesList) {
            if (code.equalsIgnoreCase(country.getDialCode()))
                return country;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.country_picker, null);
        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString("dialogTitle");
            getDialog().setTitle(dialogTitle);
/*

            int width = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_width);
            int height = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_height);
*/
            getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
        }

        searchEditText = (EditText) view
                .findViewById(R.id.country_code_picker_search);
        countryListView = (ListView) view
                .findViewById(R.id.country_code_picker_listview);

        adapter = new CountryListAdapter(getActivity(), selectedCountriesList);
        countryListView.setAdapter(adapter);

        countryListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Country country = selectedCountriesList.get(position);
                    listener.onSelectCountry(country);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCountriesList.clear();

        for (Country country : allCountriesList) {
            if (country.getName().toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
            if (country.getDialCode().contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public int compare(Country lhs, Country rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }

}
