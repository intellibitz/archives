package intellibitz.intellidroid.account;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;

import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;


/**
 *
 */
public class AccountLandingFragment extends
        IntellibitzUserFragment {

    private static final String TAG = "AcctLandingFrag";

    public AccountLandingFragment() {
        super();
    }

    public static AccountLandingFragment newInstance(ContactItem user) {
        AccountLandingFragment fragment = new AccountLandingFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accountlanding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }

        View btnGotInvite = view.findViewById(R.id.btn_gotInvite);
        btnGotInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        View txtSignIn = view.findViewById(R.id.txt_signIn);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        View btnCreateTeam = view.findViewById(R.id.btn_newTeam);
        btnCreateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

}
