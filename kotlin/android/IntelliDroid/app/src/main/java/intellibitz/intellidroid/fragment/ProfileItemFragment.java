package intellibitz.intellidroid.fragment;

import android.annotation.TargetApi;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.task.SetPwdTask;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.MyAccountActivity;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.task.SetPwdTask;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.util.NetworkImageView;

import intellibitz.intellidroid.activity.MyAccountActivity;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 *
 */
public class ProfileItemFragment extends
        IntellibitzActivityFragment implements
        UpdateProfileTask.UpdateProfileTaskListener,
        UploadProfilePicTask.UploadProfilePicTaskListener,
        SetPwdTask.SetPwdTaskListener {

    public static final String TAG = "ProfileItemFragment";
    private final static int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1;
    private View snackView;
    private TextInputEditText etName;
    private TextInputEditText etStatus;
    private UpdateProfileTask updateProfileTask;
    private UploadProfilePicTask uploadProfilePicTask;
    private Button btnSend;
    private Button btnNewPwd;
    private TextInputEditText etNewPwd;
    private SetPwdTask setPwdTask;
    private ProfileTopicListener profileTopicListener;
    private ImageView imageView;

    public ProfileItemFragment() {
        // Required empty public constructor
        super();
    }

    public static ProfileItemFragment newInstance(ProfileListener listener, ContactItem user) {
        ProfileItemFragment fragment = new ProfileItemFragment();
        if (listener instanceof ProfileTopicListener)
            fragment.setProfileTopicListener((ProfileTopicListener) listener);
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
    protected void onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted();
        if (null == user.getProfilePic()) {
            initPic();
        }
    }

    @TargetApi(21)
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
    public void onStart() {
        super.onStart();
        displayImage(user.getProfilePic());
        if (!IntellibitzPermissionFragment.isReadContactsPermissionGranted(getContext())) {
            mayRequestReadContacts(snackView);
        }
//        setupMediaChooserOnPermissions(imageView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*
        if (context instanceof OnProfilePicChangeListener) {
            profileTopicListener = (OnProfilePicChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProfilePicChangeListener");
        }
*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        profileTopicListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.profile_item_content, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snackView = view.findViewById(R.id.cl);
        etName = (TextInputEditText) view.findViewById(R.id.username);
        etStatus = (TextInputEditText) view.findViewById(R.id.et_status);
        imageView = (ImageView) view.findViewById(R.id.profile_imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProfileInfoActivity();
            }
        });
        btnSend = (Button) view.findViewById(R.id.btn_send);
        etNewPwd = (TextInputEditText) view.findViewById(R.id.tiet_newpwd);
        btnNewPwd = (Button) view.findViewById(R.id.btn_newpwd);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("UPDATE".equalsIgnoreCase(btnSend.getText().toString())) performSend();
                    btnSend.setText("CHANGE".equalsIgnoreCase(
                            btnSend.getText().toString()) ? "UPDATE" : "CHANGE");
                    etName.setEnabled(!etName.isEnabled());
                    etStatus.setEnabled(!etStatus.isEnabled());
                }
            });
            btnNewPwd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ("Update".equalsIgnoreCase(btnNewPwd.getText().toString())) execSetPwdTask();
                    btnNewPwd.setText("Change Web Password".equalsIgnoreCase(
                            btnNewPwd.getText().toString()) ? "Update" : "Change Web Password");
                    etNewPwd.setEnabled(!etNewPwd.isEnabled());
                }
            });
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        String userName = user.getName();
        etName.setText(userName);
        if (null == userName || userName.isEmpty()) {
            btnSend.setText("UPDATE");
            etName.setEnabled(true);
            etStatus.setEnabled(true);
        }
        etStatus.setText(user.getStatus());
        View myac = view.findViewById(R.id.ll_myaccount);
        myac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyAccountActivity();
            }
        });
    }

    private void execSetPwdTask() {
        final String newpwd = etNewPwd.getText().toString();
        if (TextUtils.isEmpty(newpwd)) return;
        setPwdTask = new SetPwdTask(user.getDataId(), user.getToken(), user.getDevice(),
                user.getDeviceRef(), newpwd, MainApplicationSingleton.AUTH_SET_PWD);
        setPwdTask.setSetPwdTaskListener(this);
        setPwdTask.execute();
    }

    @Override
    public void onPostSetPwdExecute(JSONObject response, String newpwd) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
            onPostSetPwdExecuteFail(response, newpwd);
//            retries again..
        } else {
//            createNewChat(contactItem, user, mainActivity.getApplicationContext());
            Log.e(TAG, "onPostSetPwdExecute: - SUCCESS - " + newpwd);
        }

    }

    @Override
    public void onPostSetPwdExecuteFail(JSONObject response, String newpwd) {
        Log.e(TAG, "onPostSetPwdExecuteFail: - FAIL - " + response);
    }

    @Override
    public void setSetPwdTaskToNull() {
        setPwdTask = null;
    }

    public void onNewMenuClicked() {
        btnSend.callOnClick();
    }

    private void performSend() {
        String name = etName.getText().toString();
        if (name.isEmpty()) {
            etName.setError("Name is required for Intellibitz");
        } else {
            etName.setError(null);
            user.setName(name);
            user.setStatus(etStatus.getText().toString());
            Log.d(TAG, "perform send");
            updateProfileTask = new UpdateProfileTask(user.getDataId(), user.getToken(),
                    user.getDevice(), user.getDeviceRef(), user.getName(), user.getStatus(),
                    MainApplicationSingleton.AUTH_UPDATE_PROFILE);
            updateProfileTask.setUpdateProfileTaskListener(this);
            updateProfileTask.execute();
        }
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
                UserContentProvider.updatesProfileInDB(user, getMainActivity());
                Log.e(TAG, "Profile Update SUCCESS - " + response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        }

    }

    @Override
    public void onPostUpdateProfileExecuteFail(JSONObject response) {
//                ERROR
        Log.e(TAG, "Profile Update ERROR - " + response);
    }

    @Override
    public void setUpdateProfileTaskToNull() {
        updateProfileTask = null;
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
        }
    }

    private void setupMediaChooser(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPicker.openMediaChooser(ProfileItemFragment.this,
                        "Choose now", new MediaPicker.OnError() {
                            @Override
                            public void onError(IOException e) {

                                Log.e("MediaPicker", "Open chooser error.", e);

                            }
                        });
            }
        });
    }

    private void displayImage(String file) {
        if (file != null && !file.isEmpty()) {
            Log.d(TAG, "Profile image change: " + file);
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
                UserContentProvider.updatesProfileInDB(user, getMainActivity());
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
                setupMediaChooser(imageView);
            }
        }
    }

    @Override
    protected void onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted();
        setupMediaChooser(imageView);
    }

    @Override
    protected void onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted();
        setupMediaChooser(imageView);
    }

    @Override
    protected void onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted();
        setupMediaChooser(imageView);
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
    private void startProfileInfoActivity() {
        Intent intent = new Intent(getActivity(), ProfileInfoActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE);

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
     */
    private void startMyAccountActivity() {
        Intent intent = new Intent(getActivity(), MyAccountActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MYACCOUNT_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                            MediaPicker.startForImageCrop(ProfileItemFragment.this, mediaFile, paramWidth,
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
