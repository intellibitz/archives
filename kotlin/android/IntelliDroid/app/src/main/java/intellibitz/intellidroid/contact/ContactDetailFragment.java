package intellibitz.intellidroid.contact;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 *
 */
public class ContactDetailFragment extends
        IntellibitzActivityFragment {
    public static final String TAG = "ContactDetailFRAG";

    private ContactItem contactItem;
    private ContactItem user;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactDetailFragment() {
        super();
    }

    public static ContactDetailFragment newInstance(ContactItem contactItem, ContactItem user) {
        ContactDetailFragment fragment = new ContactDetailFragment();
        fragment.setUser(user);
        fragment.setContactItem(contactItem);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(ContactItem.TAG, contactItem);
        fragment.setArguments(args);
        return fragment;
    }

    public void setContactItem(ContactItem contactItem) {
        this.contactItem = contactItem;
    }

    public void setUser(ContactItem user) {
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable(ContactItem.USER_CONTACT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contactdetail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            contactItem = getArguments().getParcelable(ContactItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG);
        }
        setupAppBar();
        setupContactPic();
        setupContactInfo();
    }

    private void setupContactInfo() {
        View rootView = getView();
        if (rootView != null) {
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_contactinfo);
//            clears old views
            linearLayout.removeAllViews();
            JSONArray emails = contactItem.getEmails();
            for (int i = 0; i < emails.length(); i++) {
                try {
                    String email = emails.getString(i);
                    View view = LayoutInflater.from(getContext()).inflate(
                            R.layout.list_item_contactinfo,
                            linearLayout, false);
                    TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                    tv.setText(email);
                    Drawable draw = getDrawable(R.drawable.ic_email_black_24dp,
                            getActivity().getTheme());
                    draw.setBounds(new Rect(0, 0, 40, 40));
                    tv.setCompoundDrawables(null, null, draw, null);
                    linearLayout.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            JSONArray phones = contactItem.getMobiles();
            for (int i = 0; i < phones.length(); i++) {
                try {
                    String phone = phones.getString(i);
                    View view = LayoutInflater.from(getContext()).inflate(
                            R.layout.list_item_contactinfo,
                            linearLayout, false);
                    TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                    tv.setText(phone);
                    Drawable draw = getDrawable(R.drawable.phone_icon,
                            getActivity().getTheme());
                    draw.setBounds(new Rect(0, 0, 40, 40));
                    tv.setCompoundDrawables(null, null, draw, null);
                    linearLayout.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupContactPic() {
        try {
            String pic = contactItem.getProfilePic();
            View view = getView();
            if (view != null) {
                ImageView imageView = (ImageView) view.findViewById(R.id.iv_contact);
                Bitmap bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, getContext());
                if (bitmap != null) {
                    Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                    setImageDrawable(imageView, croppedBitmap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

    private void setupAppBar() {
        View view = getView();
        if (view != null) {
            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvTitle.setText(contactItem.getDisplayName());
            TextView tvClose = (TextView) view.findViewById(R.id.tv_close);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancel();
                }
            });
            getAppCompatActivity().setSupportActionBar(toolbar);
        }
    }

    private void onCancel() {
        final Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }


}
