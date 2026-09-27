package intellibitz.intellidroid.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.util.NetworkImageView;

import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


/**
 *
 */
public class ProfileInfoFragment extends
        IntellibitzUserFragment implements
        UpdateProfileTask.UpdateProfileTaskListener,
        UploadProfilePicTask.UploadProfilePicTaskListener {

    private static final String TAG = "ProfileInfoFragment";
    private final static int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1;
    private Button btnContinue;
    private UpdateProfileTask updateProfileTask;
    private UploadProfilePicTask uploadProfilePicTask;
    private ImageView imageView;
    private ProfileTopicListener profileTopicListener;
    private EditText etFirstname;
    private EditText etLastname;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private Button btnPermission;
    private View viewPermission;
    private ProgressDialog progressDialog;

    public ProfileInfoFragment() {
        super();
    }

    public static ProfileInfoFragment newInstance(ContactItem user) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setProfileTopicListener(ProfileTopicListener profileTopicListener) {
        this.profileTopicListener = profileTopicListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileTopicListener)
            profileTopicListener = (ProfileTopicListener) context;
    }

    @Override
    protected void onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted();
        if (null == user.getProfilePic()) {
            initPic();
        }
    }

    public void initPic() {
        if (MainApplicationSingleton.isAPI21()) {
            Uri contactUri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI,
                    Uri.encode(user.getMobile()));
            Cursor cursor = getContext().getContentResolver().query(
                    contactUri, null, null, null, null);
            if (null == cursor) return;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
//                Log.e(TAG, cursor.toString());
                    // Display photo URI.
                    int columnIndex = cursor.getColumnIndex(
                            ContactsContract.PhoneLookup.PHOTO_URI);
                    if ((columnIndex != -1) && (cursor.getString(columnIndex) != null)) {
                        Uri uri = Uri.parse(cursor.getString(columnIndex));
                        if (uri != null) {
                            try {
                                Bitmap bm = MediaStore.Images.Media.getBitmap(
                                        getContext().getContentResolver(), uri);
                                String path = MediaStore.Images.Media.insertImage(
                                        getContext().getContentResolver(), bm, user.getDataId(), user.getName());
                                File f = MediaPickerUri.resolveToFile(getContext(), Uri.parse(path));
                                profileImageChanged(f);
                                user.setProfilePic(f.getAbsolutePath());
                                break;
/*
                            ((NetworkImageView) imageView).setImageUrl(f.getAbsolutePath(),
                                    MySingleton.getInstance(getActivity()).getImageLoader());
*/
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                        try {
//                            File f = MediaPickerUri.resolveToFile(getContext(), uri);
//                            File f = uri.getPath();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
/*
                ((NetworkImageView)imageView).setLocalImageBitmap(BitmapFactory.decodeStream(
//                                openPhoto(ContentUris.parseId(uri))));
                        is));
*/
                            break;
                        }
//                    Log.e(TAG, uri.toString());
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        unPackUser(getArguments());
        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) ;
    }

    public boolean alertReadContacts() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeReadContactsSnack(getSnackView(), activity);
        }
        if (isReadContactsPermissionGranted(getActivity())) {
            if (snackbar != null)
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
            if (snackbar != null)
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
            if (snackbar != null)
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
        view = inflater.inflate(R.layout.fragment_profileinfo, container, false);
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
        imageView = (ImageView) view.findViewById(R.id.profile_imageView);

        btnContinue = (Button) view.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSend();
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
        etFirstname = (EditText) view.findViewById(R.id.et_firstname);
        etLastname = (EditText) view.findViewById(R.id.et_lastname);

        etLastname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    if (isPhoneNumberValid()) {
                        performSend();
                        return true;
                    }
                }
                return false;
            }
        });
        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSend();
            }
        });

/*
        if (isContactsPermitted()) {
            setPermissionsGranted();
        }
*/
        etFirstname.requestFocus();
        unPackUser();
        displayImage(user.getProfilePic());
        if (!IntellibitzPermissionFragment.isReadContactsPermissionGranted(getContext())) {
            mayRequestReadContacts(snackView);
        }
        setupMediaChooserOnPermissions(view);
    }

    private void performSend() {
        String firstname = etFirstname.getText().toString();
        if (TextUtils.isEmpty(firstname)) {
            etFirstname.setError("First Name is required for Intellibitz");
        }
        String lastname = etLastname.getText().toString();
        if (TextUtils.isEmpty(lastname)) {
            etLastname.setError("Last Name is required for Intellibitz");
        }
        etFirstname.setError(null);
        etLastname.setError(null);
        user.setName(firstname);
        user.setFirstName(firstname);
        user.setLastName(firstname);
        user.setDisplayName(firstname + " " + lastname);
        showProgress();
        execUpdateProfileTask();
    }

    private void execUpdateProfileTask() {
        updateProfileTask = new UpdateProfileTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), user.getName(), user.getStatus(),
                MainApplicationSingleton.AUTH_UPDATE_PROFILE);
        updateProfileTask.setUpdateProfileTaskListener(this);
        updateProfileTask.execute();
    }

    @Override
    public void onPostUpdateProfileExecute(JSONObject response) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status) {
                onPostUpdateProfileExecuteFail(response);
            } else if (-1 == status) {
                onPostUpdateProfileExecuteFail(response);
            } else if (1 == status) {
//                    SUCCESS
                UserContentProvider.updatesProfileInDB(user, getActivity());
                hideProgress();
//                startAddEmailActivity();
                Activity activity = getActivity();
                Intent intent = activity.getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
                Log.e(TAG, "Profile Update SUCCESS - " + response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        }

    }

    @Override
    public void onPostUpdateProfileExecuteFail(JSONObject response) {
        hideProgress();
//                ERROR
        Log.e(TAG, "Profile Update ERROR - " + response);
    }

    @Override
    public void setUpdateProfileTaskToNull() {
        updateProfileTask = null;
        hideProgress();
    }

    private void setupMediaChooserOnPermissions(View view) {
        if (IntellibitzPermissionFragment.isCameraPermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(getContext())) {
            setupMediaChooser(view);
        } else {
            mayRequestCamera(snackView);
            mayRequestReadExternalStorage(snackView);
            mayRequestWriteExternalStorage(snackView);
            setupMediaChooser(view);
        }
    }

    private void setupMediaChooser(View view) {
        if (null == view) {
            Log.e(TAG, "FAILED to setupMediaChooser: view is NULL");
            return;
        }
        if (null == imageView)
            imageView = (ImageView) view.findViewById(R.id.profile_imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMediaChooser();
            }
        });
        View v = view.findViewById(R.id.ll_avatar);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMediaChooser();
            }
        });
        View v2 = view.findViewById(R.id.ll_profileimage);
        v2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMediaChooser();
            }
        });
        View v3 = view.findViewById(R.id.tv_profileinfo);
        v3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMediaChooser();
            }
        });
    }

    private void openMediaChooser() {
        MediaPicker.openMediaChooser(ProfileInfoFragment.this,
                "Choose now", new MediaPicker.OnError() {
                    @Override
                    public void onError(IOException e) {

                        Log.e("MediaPicker", "Open chooser error.", e);

                    }
                });
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

/*
    private void setPermissionsGranted() {
        getLoaderManager().restartLoader(
                MainApplicationSingleton.LOGIN_FRAGMENT_LOADERID,
                null, this);
    }
*/

/*
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
*/

    private void unPackUser() {
        String firstName = user.getFirstName();
        if (!TextUtils.isEmpty(firstName))
            etFirstname.setText(firstName);
        String lastName = user.getLastName();
        if (!TextUtils.isEmpty(lastName))
            etLastname.setText(lastName);
    }

    /**
     * unpacks the user from the previous state, or arguments onto the ui view
     *
     * @param savedInstanceState the state from which to restore the user from
     */
    private void unPackUser(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (null == user) {
//                first time.. gets from the arguments
                user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            } else {
//                restores user.. from previous states
                ContactItem u = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
                if (u != null) {
                    packUser(u);
                }
            }
            if (user != null) {
//            unpacks the user onto the view
                unPackUser();
            } else {
                etFirstname.setText(user.getFirstName());
                etLastname.setText(user.getLastName());
            }
        }
    }

    private void packUser(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable(ContactItem.USER_CONTACT, packUser());
        }
    }

    private void packUser(ContactItem u) {
        user.setName(u.getName());
        user.setDevice(u.getDevice());
        user.setMobile(u.getMobile());
        user.setCountryName(u.getCountryName());
        user.setCountryCode(u.getCountryCode());
        user.setProfilePic(u.getProfilePic());
    }

    public ContactItem packUser() {
        String firstname = etFirstname.getText().toString();
        String lastname = etLastname.getText().toString();
        if (!TextUtils.isEmpty(firstname))
            user.setFirstName(firstname);
        if (!TextUtils.isEmpty(lastname))
            user.setLastName(lastname);
        return user;
    }

    private void setTvUsername(Cursor cursor) {
        try {
            int nameIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            if (nameIndex != -1) {
                try {
                    String val = cursor.getString(nameIndex);
                    user.setName(val);
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
        String text = null;
        text = etFirstname != null ? etFirstname.getText().toString() : "";
        return MainApplicationSingleton.parseFormatPhoneNumberByISO(
                text, user.getCountry().getIsoCode());
    }

    private void invalidPhoneNumberAlert(View v) {
        if (null == v || null == v.getResources()) return;
        setError(v.getResources().getString(R.string.enter_phone_number));
    }

    protected boolean isPhoneNumberValid() {
        // Phone Number not entered
        if (etFirstname != null && etFirstname.getText().toString().equals("")) {
            invalidPhoneNumberAlert(etFirstname);
            return false;
        } else {
            String phoneNumber = getFormattedPhoneNumber();
            // Phone Number invalid
            if (phoneNumber == null) {
                invalidPhoneNumberAlert(etFirstname);
                return false;
            }
        }
        setError(null);

        return true;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (etFirstname != null)
            etFirstname.setError(text);
    }

    private String getSimCountryIso() {
        Activity activity = getActivity();
        if (null == activity) return null;
        return MainApplicationSingleton.getSimCountryIso(activity);
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Profile", "Saving Profile info", true);
    }

    public void hideProgress() {
        if (progressDialog != null) {
            FragmentActivity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void displayImage(String file) {
        if (file != null && !file.isEmpty()) {
            Log.d(TAG, "Profile image change: " + file);
            if (MainApplicationSingleton.isAPI16()) {
                imageView.setBackground(null);
            } else {
                imageView.setBackgroundDrawable(null);
            }
            ((NetworkImageView) imageView).setImageUrl(file,
                    MainApplicationSingleton.getInstance(getActivity()).getImageLoader());
/*
            if (file.contains("http")){

            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(file));
            }
*/
        }
    }

    public void profileImageChanged(File file) {
        if (profileTopicListener != null) {
            profileTopicListener.onProfilePicChanged(file);
        }
        onProfilePicChanged(file);
    }

    public void onProfilePicChanged(File file) {
//        // TODO: 27-02-2016
//        interact wiht the fragment
        uploadProfilePicTask = new UploadProfilePicTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_UPLOAD_PROFILE_PIC);
        uploadProfilePicTask.setUploadProfilePicTaskListener(this);
        uploadProfilePicTask.execute(file);

//        // TODO: 11-03-2016
//        save profile pic in local device storage.. for faster cache access
    }


    @Override
    public void onPostUploadProfilePicExecute(JSONObject response, String filename) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status || -1 == status) {
                onPostUploadProfilePicExecuteFail(response, filename);
            } else if (1 == status) {
//                    SUCCESS
                user.setProfilePic(filename);
                UserContentProvider.updatesProfileInDB(user, getActivity());
                Log.e(TAG, "Profile Upload SUCCESS - " + response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    @Override
    public void onPostUploadProfilePicExecuteFail(JSONObject response, String filename) {
//                ERROR
        Log.e(TAG, "onPostUploadProfilePicExecuteFail: ERROR - " + response + " :file: " + filename);
    }

    @Override
    public void setUploadProfilePicTaskToNull() {
        uploadProfilePicTask = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMediaChooser(getView());
            }
        }
    }

    @Override
    protected void onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted();
        setupMediaChooser(getView());
    }

    @Override
    protected void onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted();
        setupMediaChooser(getView());
    }

    @Override
    protected void onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted();
        setupMediaChooser(getView());
    }

    @Override
    protected void onReadExternalStoragePermissionsDenied() {
        super.onReadExternalStoragePermissionsDenied();
    }

    @Override
    protected void onWriteExternalStoragePermissionsDenied() {
        super.onWriteExternalStoragePermissionsDenied();
    }

    @Override
    protected void onContactsPermissionsDenied() {
        super.onContactsPermissionsDenied();
    }

    @Override
    protected void onCameraPermissionsDenied() {
        super.onCameraPermissionsDenied();
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
    private void startAddEmailActivity() {
        Intent intent = new Intent(getActivity(), AddEmailActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE);

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
        if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
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

        MediaPicker.handleActivityResult(getActivity(), requestCode, resultCode, data,
                new MediaPicker.OnResult() {
                    @Override
                    public void onError(IOException e) {
                        Log.e("MediaPicker", "Got file error.", e);
                    }

                    @Override
                    public void onSuccess(File mediaFile, MediaPickerRequest request) {
                        Log.e("MediaPicker", "Got file result: " + mediaFile + " for code: " + request);
                        if (request != MediaPickerRequest.REQUEST_CROP) {
                            final int paramColor = ContextCompat.getColor(getActivity(), android.R.color.black);
                            final int paramWidth = 128;
                            final int paramHeight = 128;
                            MediaPicker.startForImageCrop(ProfileInfoFragment.this, mediaFile, paramWidth,
                                    paramHeight, paramColor, new MediaPicker.OnError() {
                                        @Override
                                        public void onError(IOException e) {
                                            Log.e("MediaPicker", "Open cropper error.", e);
                                        }
                                    });
                        } else {
                            //                                final String dataUrl = MediaPickerEncoder.toDataUrl(mediaFile);
//                                Log.d(TAG, dataUrl);
                            user.setProfilePic(mediaFile.getAbsolutePath());
                            // When we are done cropping, display it in the ImageView.
                            displayImage(user.getProfilePic());
//                                notify the listener, the parent activity in this case
//                                syncs to the cloud
                            profileImageChanged(mediaFile);
                        }
                    }

                    @Override
                    public void onCancelled() {
                        Log.e("MediaPicker", "Got cancelled event.");
                    }
                });

    }


}
