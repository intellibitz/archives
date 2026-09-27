package intellibitz.intellidroid.company;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.service.ContactService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


/**
 *
 */
public class GetInvitesFragment extends
        IntellibitzUserFragment implements
        CompanyJoinTask.CompanyJoinTaskListener,
        GetInvitesTask.GetInvitesTaskListener {
    public static final String TAG = "GetInvitesFrag";

    private AutoCompleteTextView tvCompanyName;
    private AutoCompleteTextView tvCompanyType;
    private Button btnCompany;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private HashSet<ContactItem> selectedContactItems = new HashSet<>();

    public GetInvitesFragment() {
        super();
    }

    public static GetInvitesFragment newInstance(ContactItem user) {
        GetInvitesFragment fragment = new GetInvitesFragment();
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
        view = inflater.inflate(R.layout.fragment_getinvites, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            restartLoader();
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }

        getSnackView();

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performJoinCompanyTask();
            }
        });
        View btnLogin = view.findViewById(R.id.btn_join);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performJoinCompanyTask();
            }
        });

        View btnSkip = view.findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelActivity();
            }
        });

//        unPackUser();
//        setupRecyclerView();
    }

    private void setupRecyclerView(Set<ContactItem> contactItems) {
        View view = getView();
        if (null == view) return;
        if (null == contactItems || contactItems.isEmpty()) {
            Snackbar.make(recyclerView,
                    getResources().getText(R.string.note_get_invites),
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            startAddEmailActivity();
                        }
                    }).show();
        }
        RecyclerViewAdapter recyclerViewAdapter =
                new RecyclerViewAdapter(contactItems);
        recyclerViewAdapter.setHasStableIds(true);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_invites);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void restartLoader() {
        execGetInvitesTask();
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

    private ContactItem pickSelectedCompany() {
        if (selectedContactItems != null && !selectedContactItems.isEmpty())
            return selectedContactItems.iterator().next();
        return null;
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

    public void performJoinCompanyTask() {
        Activity activity = getActivity();
        if (null == activity) return;
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            ContactItem contactItem = pickSelectedCompany();
            if (contactItem != null) {
                setError(null);
                showProgress();
                execJoinCompany(contactItem.getCompanyId(), contactItem);
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


    private void execJoinCompany(String invitedCompanyId, ContactItem contactItem) {
        CompanyJoinTask companyJoinTask = new CompanyJoinTask(invitedCompanyId, contactItem,
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                user, MainApplicationSingleton.AUTH_COMPANY_JOIN, getContext());
        companyJoinTask.setRequestTimeoutMillis(30000);
        companyJoinTask.setCompanyJoinTaskListener(this);
        companyJoinTask.execute();
    }

    @Override
    public void onPostCompanyJoinResponse(JSONObject response, String invitedCompanyId,
                                          ContactItem contactItem, ContactItem user) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
//                String cid = response.getString("company_id");
                user.setCompanyId(invitedCompanyId);
                if (contactItem != null)
                    user.setCompanyName(contactItem.getCompanyName());
                ContactService.asyncUpdateWorkContacts(user, getContext());
                UserContentProvider.updatesCompanyInDB(user, getContext());
                okActivity();
            } else if (99 == status) {
                onPostCompanyJoinErrorResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostCompanyJoinErrorResponse(response);
        }
        hideProgress();
    }

    @Override
    public void onPostCompanyJoinErrorResponse(JSONObject response) {
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

    private void execGetInvitesTask() {
        GetInvitesTask getInvitesTask = new GetInvitesTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_COMPANY_GET_INVITES, getContext());
        getInvitesTask.setRequestTimeoutMillis(30000);
        getInvitesTask.setGetInvitesTaskListener(this);
        getInvitesTask.execute();
    }

    @Override
    public void onPostGetInvitesResponse(JSONObject response, ContactItem user) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
                JSONArray jsonArray = response.optJSONArray("invites");
                if (jsonArray != null) {
                    int length = jsonArray.length();
                    if (length > 0) {
                        HashSet<ContactItem> contactItems = new HashSet<>(length);
                        for (int i = 0; i < length; i++) {
                            JSONObject company = jsonArray.optJSONObject(i);
                            String cid = company.optString("company_id");
                            String cname = company.optString("company_name");
                            Long timestamp = company.optLong("timestamp");
                            ContactItem contactItem = new ContactItem();
                            contactItem.setDataId(cid);
                            contactItem.setCompanyId(cid);
                            contactItem.setCompanyName(cname);
                            contactItem.setTimestamp(timestamp);
                            contactItem.setDateTime(MainApplicationSingleton.getDateTimeMillis(timestamp));
                            contactItems.add(contactItem);
                        }
                        setupRecyclerView(contactItems);
                    }
                }
            } else if (99 == status) {
                onPostGetInvitesErrorResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostGetInvitesErrorResponse(response);
        }
        hideProgress();
    }

    @Override
    public void onPostGetInvitesErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetInvitesErrorResponse: " + response);
        if (null == response) {
            setError("Network fail - Please try again");
        } else {
//            setError(response.toString());
            setError("Failed to get company invites - Please try again");
        }
        hideProgress();
    }

    private void setSelectedItems(ContactItem contactItem, boolean selected) {
//        ContactItem contactItem = new ContactItem(intellibitzContactItem);
        if (selected)
            selectedContactItems.add(contactItem);
        else
            selectedContactItems.remove(contactItem);
//        final int count = selectedContactItems.size();
//        setSubTitle(count);
//        setSelectedCount(count);

    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final List<ContactItem> items = new ArrayList<>();

        public RecyclerViewAdapter(Collection<ContactItem> items) {
            this.items.addAll(items);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_getinvites_rv, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.contactItem = items.get(position);
//            holder.tvFrom.setText(items.get(position).id);
//            holder.mContentView.setText(items.get(position).getName());
            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.ll_addinvites_rv);
//            clears old views
            linearLayout.removeAllViews();
            View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                    R.layout.fragment_addinvites_rv_dyn,
                    (ViewGroup) holder.mView.getRootView(), false);
            TextView tvName = (TextView) view.findViewById(R.id.tv_companyname);
            tvName.setText(holder.contactItem.getCompanyName());
            final CheckBox ctv = (CheckBox) view.findViewById(R.id.cb_companylist);
            ctv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedItems(holder.contactItem, ctv.isChecked());
                }
            });
            linearLayout.addView(view);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            //            public final TextView tvName;
//            public final CheckBox ctv;
            public ContactItem contactItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
            }

            @Override
            public String toString() {
                return super.toString() + " '" + contactItem + "'";
            }
        }
    }

}
