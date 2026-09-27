package intellibitz.intellidroid;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.google.android.material.tabs.TabLayout;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntellibitzUserFragment extends
        IntellibitzPermissionFragment {
    protected ContactItem user;
    protected ProgressDialog progressDialog;

    public IntellibitzUserFragment() {
        // Required empty public constructor
        super();
    }

    public static boolean selectTabAtByTag(TabLayout tabLayout, int tab) {
        TabLayout.Tab tabAt = findTabAtByTag(tabLayout, tab);
        if (tabAt != null && tab == (int) tabAt.getTag()) {
            tabAt.select();
            return true;
        }
        return false;
    }

    public static TabLayout.Tab findTabAtByTag(TabLayout tabLayout, int tab) {
        if (null == tabLayout) return null;
        int count = tabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            TabLayout.Tab tabAt = tabLayout.getTabAt(i);
            if (tabAt != null && tab == (int) tabAt.getTag()) {
                return tabAt;
            }
        }
        return null;
    }

    public static TabLayout.Tab getSelectedTab(TabLayout tabLayout) {
        int position = getSelectedTabPosition(tabLayout);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
        if (-1 == position) {
            return null;
        }
        return tabLayout.getTabAt(position);
    }

    public static int getSelectedTabPosition(TabLayout tabLayout) {
        if (null == tabLayout) return -1;
        return tabLayout.getSelectedTabPosition();
    }

    public void setUser(ContactItem user) {
        this.user = user;
    }

    @Override
    public void onDestroy() {
//        user = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ContactItem.USER_CONTACT, user);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ContactItem userItem = getActivity().getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        if (null == user) {
            user = userItem;
        }
    }

    public void showProgress(Context context, String title, String msg, boolean indeterminate) {
        progressDialog = ProgressDialog.show(context, title, msg, indeterminate);
    }

    public void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }


    protected void okActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    protected void cancelActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }

}
