package intellibitz.intellidroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ContactListener;

import androidx.fragment.app.Fragment;

/**
 *
 */
public class IntellibitzContactDetailFragment extends
        Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The email content this fragment is presenting.
     */
    private ContactItem contactItem;
    private ContactItem user;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IntellibitzContactDetailFragment() {
        super();
    }

    public static IntellibitzContactDetailFragment newInstance(ContactListener emailListener,
                                                               IntellibitzActivity c, boolean twoPane,
                                                               ContactItem contactItem, ContactItem user) {
        IntellibitzContactDetailFragment fragment = new IntellibitzContactDetailFragment();
//        fragment.setFragmentContext(c);
//        fragment.setContactMessageHeaderListener((ContactMessageHeaderListener) emailListener);
//        fragment.setContactMessageListener((ContactMessageListener) emailListener);
        fragment.setUser(user);
        fragment.setContactItem(contactItem);
        Bundle args = new Bundle();
        args.putBoolean("twoPane", twoPane);
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(ContactItem.DEVICE_CONTACT, contactItem);
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
        View rootView = inflater.inflate(R.layout.contact_detail, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.contact_detail);
        // Show the email content as text in a TextView.
        if (contactItem != null) {
            textView.setText(contactItem.toString());
        }
        return rootView;
    }
}
