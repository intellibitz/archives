package intellibitz.intellidroid.company;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class CompanyCreateFragment extends
        IntellibitzUserFragment implements
        CompanyCreateTask.CompanyCreateTaskListener {
    public static final String TAG = "CompCreateFrag";

    private AutoCompleteTextView tvCompanyName;
    private AutoCompleteTextView tvCompanyType;
    private Button btnCompany;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private ProgressDialog progressDialog;

    public CompanyCreateFragment() {
        super();
    }

    public static CompanyCreateFragment newInstance(ContactItem user) {
        CompanyCreateFragment fragment = new CompanyCreateFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_companycreate, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }

        View btnLogin = view.findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCreateCompanyTask();
            }
        });
        getSnackView();

        btnCompany = (Button) view.findViewById(R.id.btnCompany);
        final CompanyPickerFragment companyPickerFragment = CompanyPickerFragment.newInstance("Select Company");

        tvCompanyType = (AutoCompleteTextView) view.findViewById(R.id.tv_companytype);
//                sets the companyName btnContactsPermission text anyways
        Company company = new Company(CompanyPickerFragment.NONE_COMPANY.getType(),
                CompanyPickerFragment.NONE_COMPANY.getTypeCode());
//                btnCompany.setText(companyName.toString());
        btnCompany.setText(company.getTypeCode());
        tvCompanyType.setText(company.getType());
        final List<Company> allCompanyTypes = new ArrayList<>(companyPickerFragment.getAllCompanyTypes());
        MyArrayAdapter companyArrayAdapter = new MyArrayAdapter(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, allCompanyTypes
        );
        tvCompanyType.setAdapter(companyArrayAdapter);
        tvCompanyType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String code = tvCompanyType.getText().toString();
                if (code.length() == 0) {
                    btnCompany.setText(R.string.usa);
                } else {
                    Company company = user.getCompany();
                    Company current = companyPickerFragment.getMatchedCompany(code);
                    if (current != null && company.equals(current)) {
//                            the user is editing the current companyName code..
                        setCompanySelected(current);
                    } else {
                        Company first = companyPickerFragment.getFirstMatchedCompany(code);
                        if (null == first) {
                            btnCompany.setText(R.string.usa);
                        } else {
                            setCompanySelected(first);
                        }
                    }
                }
            }
        });
        tvCompanyName = (AutoCompleteTextView) view.findViewById(R.id.tv_companyname);
        tvCompanyName.setDropDownWidth(getResources().getDisplayMetrics().widthPixels);
        tvCompanyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final Company userCompany = user.getCompany();
                if (userCompany != null) {
                    tvCompanyName.setText(MainApplicationSingleton.
                            parseFormatNoCCPhoneNumberByISO(
                                    tvCompanyName.getText().toString(), userCompany.getTypeCode()));
                }
            }
        });
        tvCompanyName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.d(TAG, keyCode + " " + event);
                String text = tvCompanyName.getText().toString();
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_ENTER) {
                }
                return false;
            }
        });
        TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    performCreateCompanyTask();
                    return true;
                }
                return false;
            }
        };
        tvCompanyName.setOnEditorActionListener(onEditorActionListener);

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCreateCompanyTask();
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                companyPickerFragment.setListener(new CompanyPickerListener() {
                    @Override
                    public void onSelectCompany(Company company) {
                        setCompanySelected(company);
                        companyPickerFragment.dismiss();
                    }
                });

                companyPickerFragment.show(getActivity().getSupportFragmentManager(), "COUNTRY_CODE_PICKER");
            }
        };
        View llCompany = view.findViewById(R.id.ll_company);
        llCompany.setOnClickListener(onClickListener);
        btnCompany.setOnClickListener(onClickListener);

        tvCompanyName.requestFocus();
        unPackUser();
    }

    private void unPackUser() {
        final String companyName = user.getCountryName();
        if (!TextUtils.isEmpty(companyName))
            tvCompanyName.setText(companyName);
        Company company = user.getCompany();
        if (null == company) {
            company = new Company(CompanyPickerFragment.NONE_COMPANY.getType(),
                    CompanyPickerFragment.NONE_COMPANY.getTypeCode());
            setCompanySelected(company);
        } else {
//            btnCompany.setText(companyName.toString());
            btnCompany.setText(company.getTypeCode());
            tvCompanyType.setText(company.getType());
        }
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
    }

    private void setCompanySelected(Company company) {
        user.setCompany(company);
        tvCompanyType.setText(company.getType());
//        btnCompany.setText(companyName.toString());
        btnCompany.setText(company.getTypeCode());
    }

    private void packUser(ContactItem u) {
        user.setName(u.getName());
        user.setDevice(u.getDevice());
        user.setMobile(u.getMobile());
        user.setCompanyName(u.getCompanyName());
        user.setCompanyCode(u.getCompanyCode());
        user.setProfilePic(u.getProfilePic());
    }

    private boolean packUser() {
        String companyName = tvCompanyName.getText().toString();
        if (TextUtils.isEmpty(companyName)) {
            setError("Company name is required");
            return false;
        }
        user.setCompanyName(companyName);
        return true;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (tvCompanyName != null)
            tvCompanyName.setError(text);
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Create Company", "Connecting to cloud.. Please wait", true);
    }

    public void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void performCreateCompanyTask() {
        Activity activity = getActivity();
        if (null == activity) return;
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            if (packUser()) {
                setError(null);
                showProgress();
                execCreateCompany();
            }
        }
    }

    protected void okActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }


    @Override
    public void onPostCompanyCreateResponse(JSONObject response, String companyName, ContactItem user) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
                String cid = response.getString("company_id");
                user.setCompanyId(cid);
                UserContentProvider.updatesCompanyInDB(user, getContext());
                okActivity();
            } else if (99 == status) {
                onPostCompanyCreateErrorResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostCompanyCreateErrorResponse(response);
        }
        hideProgress();
    }

    @Override
    public void onPostCompanyCreateErrorResponse(JSONObject response) {
//                    // TODO: 02-03-2016
//                    handles error
        Log.e(TAG, "onPostCompanyCreateErrorResponse: " + response);
        if (null == response) {
            setError("Network fail - Please try again");
        } else {
//            setError(response.toString());
            setError("Failed to create company - Please try again");
        }
        hideProgress();

    }

    private void execCreateCompany() {
        CompanyCreateTask companyCreateTask = new CompanyCreateTask(user.getCompanyName(), user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), user, MainApplicationSingleton.AUTH_COMPANY_CREATE, getContext());
        companyCreateTask.setRequestTimeoutMillis(30000);
        companyCreateTask.setCompanyCreateTaskListener(this);
        companyCreateTask.execute();
    }

    class MyArrayAdapter extends ArrayAdapter<Company> {
        //        private Context context;
        private List<Company> originalList;
        private ArrayList<Object> suggestions = new ArrayList<>();
        private Context mContext;
        private List<Company> companies;
        private List<Company> companiesAll;
        private List<Company> companiesSuggestion;
        private int mLayoutResourceId;

        public MyArrayAdapter(Context context, int simple_dropdown_item_1line,
                              List<Company> allCompaniesList) {
            super(context, simple_dropdown_item_1line, allCompaniesList);
            this.originalList = allCompaniesList;
            this.mContext = context;
            this.mLayoutResourceId = simple_dropdown_item_1line;
            this.companies = new ArrayList<>(allCompaniesList);
            this.companiesAll = new ArrayList<>(allCompaniesList);
            this.companiesSuggestion = new ArrayList<>();
        }

        public int getCount() {
            return companies.size();
        }

        public Company getItem(int position) {
            return companies.get(position);
        }

        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    convertView = inflater.inflate(mLayoutResourceId, parent, false);
                }
                Company department = getItem(position);
                TextView name = (TextView) convertView.findViewById(android.R.id.text1);
                name.setText(department.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                public String convertResultToString(Object resultValue) {
                    return ((Company) resultValue).getType();
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (!TextUtils.isEmpty(constraint)) {
                        companiesSuggestion.clear();
                        for (Company department : companiesAll) {
                            if (department.getType().toLowerCase().startsWith(
                                    constraint.toString().toLowerCase())) {
                                companiesSuggestion.add(department);
                            }
                        }
                        filterResults.values = companiesSuggestion;
                        filterResults.count = companiesSuggestion.size();
                    } else {
//                        return new FilterResults();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    companies.clear();
                    if (results != null && results.count > 0) {
                        // avoids unchecked cast warning when using companies.addAll((ArrayList<Department>) results.values);
                        List<?> result = (List<?>) results.values;
                        for (Object object : result) {
                            if (object instanceof Company) {
                                companies.add((Company) object);
                            }
                        }
                    } else if (TextUtils.isEmpty(constraint)) {
                        // no filter, add entire original list back in
                        companies.addAll(companiesAll);
                    }
                    notifyDataSetChanged();
                }
            };
        }
/*
        @Override
        public Filter getFilter() {
//                return super.getFilter();
            return filter;
        }
*/

    }

}
