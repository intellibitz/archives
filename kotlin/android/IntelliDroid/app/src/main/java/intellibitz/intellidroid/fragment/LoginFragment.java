package intellibitz.intellidroid.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.bean.Country;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.MobileGetCodeTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.OTPActivity;
import intellibitz.intellidroid.bean.Country;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactFetch;
import intellibitz.intellidroid.task.MobileGetCodeTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.widget.CountryPicker;
import intellibitz.intellidroid.widget.CountryPickerListener;

import intellibitz.intellidroid.service.ContactFetch;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


/**
 *
 */
public class LoginFragment extends
        IntellibitzUserFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MobileGetCodeTask.MobileGetCodeTaskListener {

    private static final String TAG = "LoginFragment";
    private MobileGetCodeListener mobileGetCodeListener;
    private MobileGetCodeTask mobileGetCodeTask = null;

    private AutoCompleteTextView tvMobile;
    private AutoCompleteTextView tvCountry;
    private Button btnCountry;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private Button btnPermission;
    private View viewPermission;
    private ProgressDialog progressDialog;

    //    private SimpleCursorAdapter simpleCursorAdapter;
    public LoginFragment() {
        super();
    }

    public static LoginFragment newInstance(ContactItem user) {
        LoginFragment fragment = new LoginFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MobileGetCodeListener)
            mobileGetCodeListener = (MobileGetCodeListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
//        unPackUser(getArguments());
        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) ;
        if (isContactsPermitted()) {
            setPermissionsGranted();
        }
    }

    @Override
    public void onPause() {
/*
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) tvMobile.getAdapter();
        if (adapter != null) {
            Cursor cursor = (adapter).getCursor();
            if (cursor != null) cursor.close();
        }
*/
        super.onPause();
    }

    @Override
    public void onStop() {
/*
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) tvMobile.getAdapter();
        if (adapter != null) {
            Cursor cursor = (adapter).getCursor();
            if (cursor != null) cursor.close();
        }
*/
        super.onStop();
    }

    public boolean alertReadContacts() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeReadContactsSnack(getSnackView(), activity);
        }
        if (isReadContactsPermissionGranted(getActivity())) {
            snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestReadContacts(snackbar, getSnackView());
        }
    }

    public boolean alertReadStorage() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeReadExternalStorageSnack(getSnackView(), activity);
        }
        if (isReadExternalStoragePermissionGranted(getActivity())) {
            snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestReadExternalStorage(snackbar, getSnackView());
        }
    }

    public boolean alertWriteStorage() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeWriteExternalStorageSnack(getSnackView(), activity);
        }
        if (isWriteExternalStoragePermissionGranted(getActivity())) {
            snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestWriteExternalStorage(snackbar, getSnackView());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
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


        final SimpleCursorAdapter simpleCursorAdapter;

//        new PhoneAutoCompleteTask().execute(null, null);
//        ContentResolver content = getActivity().getContentResolver();
//        Cursor cursor = content.query(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
//                CONTENT_PROJECTION, null, null, null);

//        tvMobile.setAdapter(new ContactListAdapter(getActivity(), cursor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            simpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.autocomplete_contact, null,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI},
                    new int[]{R.id.text1, R.id.text2, R.id.imageView}, 0);
        } else {
            simpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.autocomplete_contact, null,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactFetch.PHOTO_THUMBNAIL_URI},
                    new int[]{R.id.text1, R.id.text2, R.id.imageView});
        }
        simpleCursorAdapter.setStringConversionColumn(1);
/*
            simpleCursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                @Override
                public CharSequence convertToString(Cursor cursor) {
                    user.setName(cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    return cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            });
*/
        simpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (R.id.imageView == view.getId()) {
                    try {
                        if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
                            String photo = cursor.getString(cursor.getColumnIndex(
                                    ContactFetch.PHOTO_THUMBNAIL_URI));
                            if (photo != null) {
                                view.setVisibility(View.VISIBLE);
                                ((ImageView) view).setImageBitmap(MediaStore.Images.Media.getBitmap(
                                        getActivity().getContentResolver(), Uri.parse(photo)));
                            } else {
                                view.setVisibility(View.INVISIBLE);
                            }
                            return true;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        view.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });

        Button btnLogin = (Button) view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performGetCodeTask(simpleCursorAdapter);
            }
        });
        btnPermission = (Button) view.findViewById(R.id.btn_perm);
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (null == activity) return;
                requestReadContactsPermissions(activity);
            }
        });
        viewPermission = view.findViewById(R.id.ll_perm);
        getSnackView();
//        View view2 = view.findViewById(R.id.cl);
//        mayRequestReadContacts(view2);

        btnCountry = (Button) view.findViewById(R.id.btnCountry);
        final CountryPicker picker = CountryPicker.newInstance("Select Country");

        tvCountry = (AutoCompleteTextView) view.findViewById(R.id.country);
//                sets the country btnContactsPermission text anyways
        Country country = new Country(getSimCountryIso());
//                btnCountry.setText(country.toString());
        btnCountry.setText(country.getIsoCode());
        tvCountry.setText(country.getDialCode());
        final List<Country> allCountriesList = picker.getAllCountriesList();
        MyArrayAdapter countryArrayAdapter = new MyArrayAdapter(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, allCountriesList
        );
        tvCountry.setAdapter(countryArrayAdapter);
        tvCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String code = tvCountry.getText().toString();
                if (code.length() == 0) {
                    btnCountry.setText(R.string.usa);
                } else {
                    Country country = user.getCountry();
                    Country current = picker.getMatchedCountry(code);
                    if (current != null && country.equals(current)) {
//                            the user is editing the current country code..
                        setCountrySelected(current);
                    } else {
                        Country first = picker.getFirstMatchedCountry(code);
                        if (null == first) {
                            btnCountry.setText(R.string.usa);
                        } else {
                            setCountrySelected(first);
                        }
                    }
                }
            }
        });
        tvMobile = (AutoCompleteTextView) view.findViewById(R.id.mobile);
        tvMobile.setDropDownWidth(getResources().getDisplayMetrics().widthPixels);
        tvMobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final Country userCountry = user.getCountry();
                if (userCountry != null) {
                    tvMobile.setText(MainApplicationSingleton.
                            parseFormatNoCCPhoneNumberByISO(
                                    tvMobile.getText().toString(), userCountry.getIsoCode()));
                }
            }
        });
        tvMobile.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.d(TAG, keyCode + " " + event);
                String text = tvMobile.getText().toString();
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_ENTER) {
                    if (isContactsPermitted() && !TextUtils.isEmpty(text)) {
                        getLoaderManager().restartLoader(MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID,
                                null, LoginFragment.this);
                    }
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
                    if (isPhoneNumberValid()) {
                        performGetCodeTask(simpleCursorAdapter);
                        return true;
                    }
                }
                return false;
            }
        };
        tvMobile.setOnEditorActionListener(onEditorActionListener);
        tvMobile.setAdapter(simpleCursorAdapter);

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performGetCodeTask(simpleCursorAdapter);
            }
        });
        View llCountry = view.findViewById(R.id.ll_country);
        llCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(Country country) {
                        setCountrySelected(country);
//                        btnCountry.setText(name+":"+dialCode);
                        picker.dismiss();
                    }
                });

                picker.show(getFragmentManager(), "COUNTRY_CODE_PICKER");
            }
        });
        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(Country country) {
                        setCountrySelected(country);
//                        btnCountry.setText(name+":"+dialCode);
                        picker.dismiss();
                    }
                });

                picker.show(getFragmentManager(), "COUNTRY_CODE_PICKER");
            }
        });

/*
        tvUsername.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        try {
                            if (hasFocus) {
                                if (0 == tvUsername.getText().length() &&
                                        tvMobile.getText().length() != 0) {
                                    if (simpleCursorAdapter != null) {
                                        Cursor cursor = simpleCursorAdapter.getCursor();
                                        if (cursor != null && cursor.getCount() > 0 && !cursor.isClosed()) {
                                            int nameIndex = cursor.getColumnIndex(
                                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                                            if (nameIndex != -1) {
                                                String val = cursor.getString(nameIndex);
                                                tvUsername.setText(val);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                            Log.e(TAG, ignored.getMessage());
                        }
                    }
                }
        );
*/

        tvMobile.requestFocus();
        unPackUser();
    }

    private void unPackUser() {
        final String mobile = user.getMobile();
        if (!TextUtils.isEmpty(mobile))
            tvMobile.setText(mobile);
        Country country = user.getCountry();
        if (null == country) {
            country = new Country(getSimCountryIso());
            setCountrySelected(country);
        } else {
//            btnCountry.setText(country.toString());
            btnCountry.setText(country.getIsoCode());
            tvCountry.setText(country.getDialCode());
        }
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
    }

    public boolean isContactsPermitted() {
        return IntellibitzPermissionFragment.isReadContactsPermissionGranted(getActivity());
    }

    private void setPermissionsGranted() {
        getLoaderManager().initLoader(MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID,
                null, this);
    }

    @Override
    protected void onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted();
        setPermissionsGranted();
    }

    @Override
    protected void onContactsPermissionsDenied() {
        super.onContactsPermissionsGranted();
        Activity activity = getActivity();
        if (null == activity) return;
        snackbar = makeReadContactsSnack(snackbar, getSnackView(), activity);
    }

    private void setCountrySelected(Country country) {
        user.setCountry(country);
        tvCountry.setText(country.getDialCode());
//        btnCountry.setText(country.toString());
        btnCountry.setText(country.getIsoCode());
    }

    private void packUser(ContactItem u) {
        user.setName(u.getName());
        user.setDevice(u.getDevice());
        user.setMobile(u.getMobile());
        user.setCountryName(u.getCountryName());
        user.setCountryCode(u.getCountryCode());
        user.setProfilePic(u.getProfilePic());
    }

    public ContactItem packUser(SimpleCursorAdapter simpleCursorAdapter) {
        String mobile = MainApplicationSingleton.parseFormatNoCCPhoneNumberByISO(
                tvMobile.getText().toString(), user.getCountry().getIsoCode());
        user.setMobile(mobile);
        user.setCountryName(btnCountry.getText().toString());
        user.setCountryCode(tvCountry.getText().toString());
        if (simpleCursorAdapter != null) {
            try {
                Cursor cursor = simpleCursorAdapter.getCursor();
                if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
                    setTvUsername(cursor);
                    int photoIndex = -1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        photoIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);
                    } else {
                        photoIndex = cursor.getColumnIndex(
                                ContactsContract.Contacts.PHOTO_ID);
                    }
                    if (photoIndex != -1) {
                        String photo = cursor.getString(photoIndex);
//            Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
                        if (null == user.getProfilePic() && photo != null) {
                            try {
                                Bitmap bm = MediaStore.Images.Media.getBitmap(
                                        getActivity().getContentResolver(), Uri.parse(photo));
                                File f = MediaPickerFile.createImageFileInESPublicDir(user.getName(), ".jpg");
                                if (null == f) {

                                } else {
                                    MediaPickerUri.bitmapToFile(getActivity(), bm, f);
                                    user.setProfilePic(f.getAbsolutePath());
                                }
                                Log.d(TAG, "User Profile pic: " + user.getProfilePic());
                            } catch (Throwable e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                Log.e(TAG, ignored.getMessage());
            }
        }
/*
        String name = tvUsername.getText().toString();
        if (!TextUtils.isEmpty(name))
            user.setName(name);
*/
        return user;
    }

    private void setTvUsername(Cursor cursor) {
        try {
            int nameIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            if (nameIndex != -1) {
                try {
                    String name = cursor.getString(nameIndex);
                    String lastName = "";
                    String firstName = "";
                    if (name.split("\\w+").length > 1) {
                        lastName = name.substring(name.lastIndexOf(" ") + 1);
                        firstName = name.substring(0, name.lastIndexOf(' '));
                    } else {
                        firstName = name;
                    }
                    user.setName(name);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                } catch (Throwable ignored) {
                    Log.e(TAG, ignored.getMessage());
                }
            }
        } catch (Throwable ignore) {
//            ignore.printStackTrace();
            Log.e(TAG, "Name cannot be retrieved from Cursor: " + ignore.getMessage());
        }
    }

    private String getFormattedPhoneNumber() {
        String text;
        text = tvMobile != null ? tvMobile.getText().toString() : "";
        return MainApplicationSingleton.parseFormatPhoneNumberByISO(
                text, user.getCountry().getIsoCode());
    }

    private void invalidPhoneNumberAlert(View v) {
        if (null == v || null == v.getResources()) return;
        setError(v.getResources().getString(R.string.enter_phone_number));
    }

    protected boolean isPhoneNumberValid() {
        // Phone Number not entered
        if (tvMobile != null && tvMobile.getText().toString().equals("")) {
            invalidPhoneNumberAlert(tvMobile);
            return false;
        } else {
            String phoneNumber = getFormattedPhoneNumber();
            // Phone Number invalid
            if (phoneNumber == null) {
                invalidPhoneNumberAlert(tvMobile);
                return false;
            }
        }
        setError(null);

        return true;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (tvMobile != null)
            tvMobile.setError(text);
    }

    private String getSimCountryIso() {
        Activity activity = getActivity();
        if (null == activity) return null;
        return MainApplicationSingleton.getSimCountryIso(activity);
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Login", "Requesting for OTP", true);
    }

    public void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void performGetCodeTask(SimpleCursorAdapter simpleCursorAdapter) {
        Activity activity = getActivity();
        if (null == activity) return;
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            //            // TODO: 22-02-2016
//            Call get code api
            if (isPhoneNumberValid()) {
                packUser(simpleCursorAdapter);
                setError(null);
                showProgress();
                execGetOTP();
            }
//            setupMobileActivateFragment();
        } else {

        }
    }

    @Override
    public void onPostMobileGetCodeExecute(JSONObject response) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
                if (mobileGetCodeListener != null)
                    mobileGetCodeListener.onAccountExists(response, user);
//                startOTPActivity();
                finishActivity();
            } else if (2 == status) {
                if (mobileGetCodeListener != null)
                    mobileGetCodeListener.onNewAccount(response, user);
//                startOTPActivity();
                finishActivity();
            } else if (99 == status) {
                onPostMobileGetCodeExecuteFail(response);
//                static otp build - goes to next screen even if otp fails
//                finishActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostMobileGetCodeExecuteFail(response);
        }
        hideProgress();
    }

    private void finishActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @Override
    public void onPostMobileGetCodeExecuteFail(JSONObject response) {
//                    // TODO: 02-03-2016
//                    handles error
        Log.e(TAG, "onPostMobileGetCodeExecuteFail: " + response);
        if (null == response) {
            setError("Network fail - Please try again");
        } else {
//            setError(response.toString());
            setError("Failed to generate OTP - Please try again");
        }
        hideProgress();
    }

    @Override
    public void setMobileGetCodeTaskToNull() {
        mobileGetCodeTask = null;
    }

    private void execGetOTP() {
        mobileGetCodeTask = new MobileGetCodeTask(user.getDevice(), getUserMobileWithCC(),
                MainApplicationSingleton.MOBILE_GETCODE_URL);
        mobileGetCodeTask.setMobileGetCodeTaskListener(this);
        mobileGetCodeTask.execute();
    }

    private String normalizeUserMobile() {
        //            keeps only the digits in a phone number
        user.setMobile(PhoneNumberUtil.normalizeDigitsOnly(user.getMobile()));
        return user.getMobile();
    }

    @NonNull
    private String getUserMobileWithCC() {
        normalizeUserMobile();
        return user.getCountry().getDialCode() + user.getMobile();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == id) {
/*
                long mListId = 0;
                if (args != null) {
                    mListId = args.getLong(CONTACT_FILTER);
                }
*/
//            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String s = tvMobile.getText().toString();
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " like ? ";
            String[] selectionArgs = new String[]{"%%"};
            if (!TextUtils.isEmpty(s)) {
/*
                uri = Uri.withAppendedPath(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                        Uri.encode(s));
*/
//                selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%?%' ";
                selectionArgs = new String[]{"%" + s + "%"};
            }
            String[] CONTENT_PROJECTION = new String[]{
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                    ContactFetch.PHOTO_THUMBNAIL_URI,
                    ContactFetch.PHOTO_URI,
                    ContactFetch.PHOTO_FILE_ID
            };
            return new CursorLoader(getActivity(),
                    uri,
                    CONTENT_PROJECTION,
                    selection, selectionArgs, null);

//            return new MergeCursor()
        }
/*
        Uri uri;
        switch (id) {
            case 110:
*/
/*
                return new CursorLoader(getActivity(), uri, CONTENT_PROJECTION,
                        ContactsContract.CommonDataKinds.Phone.NUMBER+"=?", new String[]{id+""}, null);
*//*

        } //Apply filter here with new info comes from 'restartLoader'
        return null;
*/
/*
        if (null == constraint){
            uri = Uri.withAppendedPath(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                    Uri.encode("0"));
        } else {
            uri = Uri.withAppendedPath(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                    Uri.encode(constraint.toString()));
        }
*/
/*
        FilterQueryProvider filter = simpleCursorAdapter.getFilterQueryProvider();
        if (filter != null) {
            return filter.runQuery(constraint);
        }
*/
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == loader.getId()) {
                SimpleCursorAdapter simpleCursorAdapter = (SimpleCursorAdapter) tvMobile.getAdapter();
                if (simpleCursorAdapter != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        if (!cursor.isClosed())
                            simpleCursorAdapter.swapCursor(cursor);
                    } else {
                        if (!cursor.isClosed())
                            simpleCursorAdapter.changeCursor(cursor);
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID == loader.getId()) {
                SimpleCursorAdapter simpleCursorAdapter = (SimpleCursorAdapter) tvMobile.getAdapter();
                if (simpleCursorAdapter != null) {
                    Cursor data = simpleCursorAdapter.getCursor();
                    if (data != null && !data.isClosed()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            simpleCursorAdapter.swapCursor(null);
                        } else {
                            simpleCursorAdapter.changeCursor(null);
                        }
                    }
                }
            }
        }
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
    private void startOTPActivity() {
        Intent intent = new Intent(getActivity(), OTPActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    ContactItem item = data.getParcelableExtra(ContactItem.USER_CONTACT);
                    Activity activity = getActivity();
                    Intent intent = activity.getIntent();
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) item);
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Activity activity = getActivity();
                Intent intent = activity.getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                activity.setResult(Activity.RESULT_CANCELED, intent);
                activity.finish();
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        }
    }

    public interface MobileGetCodeListener {
        //        state is maintained local within the fragment
//        the fragment informs the activity after successfull account creation
        void onAccountExists(JSONObject response, ContactItem user);

        void onNewAccount(JSONObject response, ContactItem user);
    }

    class MyArrayAdapter extends ArrayAdapter<Country> {
        //        private Context context;
        private List<Country> originalList;
        private ArrayList<Object> suggestions = new ArrayList<>();
        private Context mContext;
        private List<Country> countries;
        private List<Country> countriesAll;
        private List<Country> countriesSuggestion;
        private int mLayoutResourceId;

        public MyArrayAdapter(Context context, int simple_dropdown_item_1line,
                              List<Country> allCountriesList) {
            super(context, simple_dropdown_item_1line, allCountriesList);
            this.originalList = allCountriesList;
            this.mContext = context;
            this.mLayoutResourceId = simple_dropdown_item_1line;
            this.countries = new ArrayList<>(allCountriesList);
            this.countriesAll = new ArrayList<>(allCountriesList);
            this.countriesSuggestion = new ArrayList<>();
        }

        public int getCount() {
            return countries.size();
        }

        public Country getItem(int position) {
            return countries.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    convertView = inflater.inflate(mLayoutResourceId, parent, false);
                }
                Country department = getItem(position);
                TextView name = (TextView) convertView.findViewById(android.R.id.text1);
                name.setText(department.getDialCode());
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
                    return ((Country) resultValue).getDialCode();
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (!TextUtils.isEmpty(constraint)) {
                        countriesSuggestion.clear();
                        for (Country department : countriesAll) {
                            if (department.getDialCode().toLowerCase().startsWith(
                                    constraint.toString().toLowerCase())) {
                                countriesSuggestion.add(department);
                            }
                        }
                        filterResults.values = countriesSuggestion;
                        filterResults.count = countriesSuggestion.size();
                    } else {
//                        return new FilterResults();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    countries.clear();
                    if (results != null && results.count > 0) {
                        // avoids unchecked cast warning when using countries.addAll((ArrayList<Department>) results.values);
                        List<?> result = (List<?>) results.values;
                        for (Object object : result) {
                            if (object instanceof Country) {
                                countries.add((Country) object);
                            }
                        }
                    } else if (TextUtils.isEmpty(constraint)) {
                        // no filter, add entire original list back in
                        countries.addAll(countriesAll);
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
