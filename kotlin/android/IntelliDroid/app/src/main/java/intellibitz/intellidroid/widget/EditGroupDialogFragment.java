package intellibitz.intellidroid.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;

//import intellibitz.intellidroid.util.MediaPicker;
//import intellibitz.intellidroid.util.MediaPickerRequest;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnEditGroupDialogFragmentListener} interface
 * to handle interaction events.
 */
public class EditGroupDialogFragment extends
        BottomSheetDialogFragment {

    private static final String TAG = "NewGroupDialog";
    private EditText sub;
    private MultiAutoCompleteTextView to;
    private MultiAutoCompleteTextView cc;
    private MultiAutoCompleteTextView bcc;
    private int item;
    private ContactItem user;
    private boolean chatMode = false;
    private TextView tvIcon;
    private View snackView;
    private Snackbar snackbar;
    private Button btnPermission;
    private View viewPermission;
    private View view;
    private File profilePic;
    private MessageItem messageItem;
    private OnEditGroupDialogFragmentListener mListener;

    public EditGroupDialogFragment() {
        // Required empty public constructor
        super();
    }

    public static EditGroupDialogFragment newMessageDialog(
            OnEditGroupDialogFragmentListener listener, int item,
            ContactItem user, MessageItem messageItem) {
        // Create an instance of the dialog fragment and show it
        EditGroupDialogFragment dialog = new EditGroupDialogFragment();
        dialog.addEditGroupDialogListener(listener);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ContactItem.USER_CONTACT, user);
        bundle.putParcelable(MessageItem.TAG, messageItem);
        bundle.putInt("item", item);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static EditGroupDialogFragment newMessageDialog(OnEditGroupDialogFragmentListener listener,
                                                           int item, ContactItem user) {
        // Create an instance of the dialog fragment and show it
        return newMessageDialog(listener, item, user, null);
    }

    public MessageItem getMessageItem() {
        return messageItem;
    }

    public String getSubject() {
        return String.valueOf(sub.getText());
    }

    public Rfc822Token[] getTo() {
        return Rfc822Tokenizer.tokenize(String.valueOf(to.getText()));
    }

/*
    public String getTo (){
        return String.valueOf(to.getText());
    }

    public String getCc (){
        return String.valueOf(cc.getText());
    }

    public String getBcc (){
        return String.valueOf(bcc.getText());
    }
*/

    public Rfc822Token[] getCc() {
        return Rfc822Tokenizer.tokenize(String.valueOf(cc.getText()));
    }

    public Rfc822Token[] getBcc() {
        return Rfc822Tokenizer.tokenize(String.valueOf(bcc.getText()));
    }

    public boolean isChatMode() {
        return chatMode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        user = arguments.getParcelable(ContactItem.USER_CONTACT);
        item = arguments.getInt("item");
        messageItem = arguments.getParcelable(
                MessageItem.TAG);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_group);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        view = inflater.inflate(R.layout.dialog_group_edit, (ViewGroup) getView());
        tvIcon = (TextView) view.findViewById(R.id.tv_icon);
        viewPermission = view.findViewById(R.id.ll_perm);
        btnPermission = (Button) view.findViewById(R.id.btn_perm);
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMediaPermissions(getSnackView(), getActivity());
            }
        });
        getSnackView();

        Switch sw = (Switch) view.findViewById(R.id.sw_message);
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch) v).isChecked();
                if (checked) {
                    ((Switch) v).setText(R.string.menu_title_chat);
                    setChatMode();
                } else {
                    ((Switch) v).setText(R.string.broadcast);
                    setEmailMode();
                }
            }
        });

        sub = (EditText) view.findViewById(R.id.new_email_sub);
        to = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_to);
        cc = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_cc);
        bcc = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_bcc);


        if (messageItem != null) {
            sub.setText(messageItem.getSubject());
            to.setText(messageItem.getTo());
            cc.setText(messageItem.getCc());
            bcc.setText(messageItem.getBcc());
        }

        if (2 == item) {
//            sw.setVisibility(View.GONE);
            setChatMode();
        } else if (1 == item) {
            sw.setVisibility(View.GONE);
            setEmailMode();
        } else if (0 == item) {
            sw.setVisibility(View.VISIBLE);
            sw.setChecked(false);
            setEmailMode();
        }

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.menu_title_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null)
                            mListener.onDialogPositiveClick(EditGroupDialogFragment.this);
                        // sign in the user ...
                    }
                })
                .setNegativeButton(R.string.menu_title_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        NewEmailDialogFragment.this.getDialog().cancel();
                        if (mListener != null)
                            mListener.onDialogNegativeClick(EditGroupDialogFragment.this);
                    }
                });

//        sets the default to chat mode
        sw.setText("CHAT");
        sw.setChecked(true);
        setChatMode();

//        setupMediaChooser(tvIcon);
        setupMediaChooserOnPermissions(tvIcon);

        return builder.create();
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
//            snackView = view.findViewById(R.id.username);
            snackView = view.findViewById(R.id.mr_art);
        return snackView;
    }

    private void setEmailMode() {
        tvIcon.setVisibility(View.GONE);
        cc.setVisibility(View.VISIBLE);
        bcc.setVisibility(View.VISIBLE);
        chatMode = false;
        to.setTokenizer(new Rfc822Tokenizer());
        cc.setTokenizer(new Rfc822Tokenizer());
        bcc.setTokenizer(new Rfc822Tokenizer());
        new SetupEmailAutoCompleteTask().execute(null, null);
    }

    private void setChatMode() {
        tvIcon.setVisibility(View.VISIBLE);
        cc.setVisibility(View.GONE);
        bcc.setVisibility(View.GONE);
        chatMode = true;
//        to.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//        cc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//        bcc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        to.setTokenizer(new Rfc822Tokenizer());
        cc.setTokenizer(new Rfc822Tokenizer());
        bcc.setTokenizer(new Rfc822Tokenizer());
        new SetupPhoneAutoCompleteTask().execute(null, null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEditGroupDialogFragmentListener) {
            addEditGroupDialogListener((OnEditGroupDialogFragmentListener) context);
        }
    }

    private void setupMediaChooserOnPermissions(View view) {
        setupMediaChooser(view);
        if (isCameraStoragePermissionsGranted()) {
            viewPermission.setVisibility(View.GONE);
        } else {
            requestMediaPermissions(snackView, getActivity());
        }
    }

    private boolean isCameraStoragePermissionsGranted() {
        return IntellibitzPermissionFragment.isCameraPermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(getContext());
    }

    private void requestMediaPermissions(View view, Activity activity) {
        IntellibitzPermissionFragment.mayRequestCamera(view, activity);
        IntellibitzPermissionFragment.mayRequestReadExternalStorage(view, activity);
        IntellibitzPermissionFragment.mayRequestWriteExternalStorage(view, activity);
    }

    private void setupMediaChooser(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                MediaPicker.openMediaChooser(EditGroupDialogFragment.this,
                        "Choose now", new MediaPicker.OnError() {
                            @Override
                            public void onError(IOException e) {

                                Log.e("MediaPicker", "Open chooser error.", e);

                            }
                        });
*/
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

/*
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
                            MediaPicker.startForImageCrop(EditGroupDialogFragment.this, mediaFile, paramWidth,
                                    paramHeight, paramColor, new MediaPicker.OnError() {
                                        @Override
                                        public void onError(IOException e) {
                                            Log.e("MediaPicker", "Open cropper error.", e);
                                        }
                                    });
                        } else {
                            profilePic = mediaFile;
                            displayImage(mediaFile.getAbsolutePath());
*/
/*
                            try {
//                                final String dataUrl = MediaPickerEncoder.toDataUrl(mediaFile);
//                                Log.d(TAG, dataUrl);
//                                user.setProfilePic(mediaFile.getAbsolutePath());
                                // When we are done cropping, display it in the ImageView.
//                                notify the listener, the parent activity in this case
//                                syncs to the cloud
//                                profileImageChanged(mediaFile);
                            } catch (IOException e) {
                                Log.e("MediaPicker", "Unable to get data url.", e);
                            }
*//*

                        }
                    }

                    @Override
                    public void onCancelled() {
                        Log.e("MediaPicker", "Got cancelled event.");
                    }
                });
*/
    }

    private void displayImage(String file) {
        if (file != null && !file.isEmpty()) {
            if (messageItem != null)
                messageItem.setProfilePic(file);
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(new Rect(0, 0, 60, 60));
            tvIcon.setCompoundDrawables(null, drawable, null, null);
            Log.d(TAG, "Profile image change: " + file);
        }
    }

    public File getProfilePic() {
        return profilePic;
    }

    public void addEditGroupDialogListener(OnEditGroupDialogFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addEmailsToAutoComplete(List<Map<String, String>> contacts) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        Context context = getContext();
//        the activity might disappear faster on cancel, before the loader arrives
        if (context != null) {
            SimpleAdapter adapter = new SimpleAdapter(context, contacts,
                    android.R.layout.simple_dropdown_item_1line, new String[]{"text1", "text2"},
                    new int[]{android.R.id.text1, android.R.id.text1}) {
            };
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (null == data || "".equals(data)) return true;
                    TextView textView = (TextView) view;
                    textView.setCompoundDrawables(null, null, null, null);
                    String photo = (String) data;
                    if (photo.startsWith("content")) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContext().getContentResolver(), Uri.parse(photo));
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            drawable.setBounds(new Rect(0, 0, 60, 60));
                            textView.setCompoundDrawables(null, null, drawable, null);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }

            });

            to.setAdapter(adapter);
            cc.setAdapter(adapter);
            bcc.setAdapter(adapter);
        }
    }

    private void addPhonesToAutoComplete(List<Map<String, String>> contacts) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        Context context = getContext();
//        the activity might disappear faster on cancel, before the loader arrives
        if (context != null) {
            SimpleAdapter adapter = new SimpleAdapter(context, contacts,
                    android.R.layout.simple_dropdown_item_1line, new String[]{"text1", "text2"},
                    new int[]{android.R.id.text1, android.R.id.text1}) {
            };
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (null == data || "".equals(data)) return true;
                    TextView textView = (TextView) view;
                    textView.setCompoundDrawables(null, null, null, null);
                    String photo = (String) data;
                    if (photo.startsWith("content")) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContext().getContentResolver(), Uri.parse(photo));
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            drawable.setBounds(new Rect(0, 0, 60, 60));
                            textView.setCompoundDrawables(null, null, drawable, null);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }

            });

            to.setAdapter(adapter);
            cc.setAdapter(adapter);
            bcc.setAdapter(adapter);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnEditGroupDialogFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(Void... voids) {
            // TODO: register the new account here.
//            investigate response and take action
            ArrayList<Map<String, String>> contacts = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            Context context = getContext();
            if (null == context) return contacts;
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String val = cursor.getString(cursor.getColumnIndex(ContactsContract
                            .CommonDataKinds.Email.DATA));
                    String photo = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    HashMap<String, String> vals = new HashMap<String, String>() {
                        @Override
                        public String toString() {
//                            return super.toString();
                            return get("text1");
                        }
                    };
                    vals.put("text1", val);
                    if (null == photo) photo = "";
                    vals.put("text2", photo);
                    contacts.add(vals);
                }
                cursor.close();
            }

            return contacts;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    class SetupPhoneAutoCompleteTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(Void... voids) {
            // TODO: register the new account here.
//            investigate response and take action
            ArrayList<Map<String, String>> contacts = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            Context context = getContext();
            if (null == context) return contacts;
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String val = cursor.getString(cursor.getColumnIndex(ContactsContract
                            .CommonDataKinds.Phone.DATA));
                    String photo = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
//                    HashMap<String, String> vals = new HashMap<String, String>();
                    HashMap<String, String> vals = new HashMap<String, String>() {
                        @Override
                        public String toString() {
//                            return super.toString();
                            return get("text1");
                        }
                    };
                    vals.put("text1", val);
                    if (null == photo) photo = "";
                    vals.put("text2", photo);
                    contacts.add(vals);
                }
                cursor.close();
            }

            return contacts;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> contacts) {
            addPhonesToAutoComplete(contacts);
        }
    }

}
