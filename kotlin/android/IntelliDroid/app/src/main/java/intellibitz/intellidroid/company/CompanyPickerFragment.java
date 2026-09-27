package intellibitz.intellidroid.company;

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
import androidx.appcompat.app.AppCompatDialogFragment;

import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.util.*;

public class CompanyPickerFragment extends
        AppCompatDialogFragment implements
        Comparator<Company> {

    public static final Company NONE_COMPANY = new Company("UnRegistered", "None");
    private EditText searchEditText;
    private ListView companyListView;
    //    private CompanyListAdapter adapter;
    private Set<Company> allCompanyTypes;
    private List<Company> selectedCompaniesList;
    private CompanyPickerListener listener;

    public CompanyPickerFragment() {
        super();
        getAllCompanyTypesList();
    }

    /**
     * To support show as dialog
     *
     * @param dialogTitle
     * @return
     */
    public static CompanyPickerFragment newInstance(String dialogTitle) {
        CompanyPickerFragment picker = new CompanyPickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    public Set<Company> getAllCompanyTypes() {
        return allCompanyTypes;
    }

    public void setListener(CompanyPickerListener listener) {
        this.listener = listener;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCompanyListView() {
        return companyListView;
    }

    private List<Company> getAllCompanyTypesList() {
        allCompanyTypes = new HashSet<>();

        for (String typeCode : MainApplicationSingleton.typeCodeMAP.keySet()) {
            Company company = new Company();
            String type = MainApplicationSingleton.typeCodeMAP.get(typeCode);
            company.setType(type);
            company.setTypeCode(typeCode);
            allCompanyTypes.add(company);
        }

        List<Company> sortedList = new ArrayList<>(allCompanyTypes);
        Collections.sort(sortedList, this);

        selectedCompaniesList = new ArrayList<>();
        selectedCompaniesList.addAll(sortedList);

        // Return
        return sortedList;
    }

/*
    private static String readEncodedJsonString(Context context)
			throws java.io.IOException {
		String base64 = context.getResources().getString(R.string.countries_code);
		byte[] data = Base64.decode(base64, Base64.DEFAULT);
		return new String(data, "UTF-8");
	}
*/

    public Company getMatchedCompany(String code) {
        for (Company Company : selectedCompaniesList) {
            if (code.equalsIgnoreCase(Company.getType()))
                return Company;
        }
        return null;
    }

    public Company getFirstMatchedCompany(String code) {
        for (Company Company : allCompanyTypes) {
            if (code.equalsIgnoreCase(Company.getType()))
                return Company;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.company_picker, null);
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
                .findViewById(R.id.company_code_picker_search);
        companyListView = (ListView) view
                .findViewById(R.id.company_code_picker_listview);

        CompanyListAdapter adapter = new CompanyListAdapter(getActivity(), selectedCompaniesList);
        companyListView.setAdapter(adapter);

        companyListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Company Company = selectedCompaniesList.get(position);
                    listener.onSelectCompany(Company);
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
        selectedCompaniesList.clear();

        for (Company Company : allCompanyTypes) {
/*
            if (Company.getType().toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                selectedCompaniesList.add(Company);
            }
*/
            if (Company.getType().contains(text.toLowerCase())) {
                selectedCompaniesList.add(Company);
            }
        }

        companyListView.requestLayout();
//        adapter.notifyDataSetChanged();
    }

    @Override
    public int compare(Company lhs, Company rhs) {
        return lhs.getType().compareTo(rhs.getType());
    }

}
