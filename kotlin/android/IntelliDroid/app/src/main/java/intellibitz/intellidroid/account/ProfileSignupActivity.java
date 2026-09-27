package intellibitz.intellidroid.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.data.ContactItem;

public class ProfileSignupActivity extends
        IntellibitzTwoPaneUserActivity {

    private static final String TAG = "ProfileSignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileinfo);
//        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            setupTwopane();
//        setupAppBar();
            setupFragments();
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
//            newLoginFragment(savedInstanceState);
        }
    }

    private void setupFragments() {
        final ProfileSignupFragment profileSignupFragment =
                ProfileSignupFragment.newInstance(user);
        replaceContentFragment(profileSignupFragment);
    }


    @Override
    public void onBackPressed() {
//        if no more fragments is left.. then its the workflow activity which is blank
//        finish the blank activity.. to go back
/*
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        }
*/
        super.onBackPressed();
    }

    public void setupAlert(boolean upAlert, View view) {
        if (upAlert) {
            String text = user.getName();
            if (null == text || text.isEmpty()) {
                text = "User Found";
            }
            Snackbar snack = Snackbar.make(
                    view, text, Snackbar.LENGTH_INDEFINITE);
            snack.setAction("SWITCH", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileSignupActivity.this,
                            MainApplicationSingleton.MAIN_ACTIVITY_CLASS);
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                    startActivity(intent);
                }
            });
            snack.show();
        }
    }

    /**
     * @param fragment the fragment to be replaced in the two pane container
     * @return fragment the replaced fragment
     */
    public Fragment replaceContentFragment(Fragment fragment) {
        if (null == fragment) {
            return null;
        }
        itemView.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.two_pane_container, fragment,
                fragment.getClass().getSimpleName());
//        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

    /**
     * @param fragment the detail fragment to be replaced in the two pane container
     * @return fragment the replaced detail fragment
     */
    public Fragment replaceDetailFragment(Fragment fragment) {
        if (null == fragment) return null;
        itemView.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (twoPane) {
            detailView.setVisibility(View.GONE);
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment,
                    fragment.getClass().getSimpleName());
            detailView.setVisibility(View.VISIBLE);
        } else {
            fragmentTransaction.replace(R.id.two_pane_container, fragment,
                    fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
//            NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        NOTE: THE ABOVE CALL DELIVERS THE ACTIVITY RESULT TO THE FRAGMENT
    }


/*
    @Override
    public void onNewAccount(JSONObject response, ContactItem user) {
        try {
            String msg = response.getString("msg");
            Log.e(TAG, "Message: " + msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setupOTPFragment(user);
    }

    @Override
    public void onAccountExists(JSONObject response, ContactItem user) {
        try {
            String msg = response.getString("msg");
            Log.e(TAG, "Message: " + msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setupOTPFragment(user);
    }
*/

/*
    private void performNextAction() {
        //            // TODO: 23-02-2016
//            view will be null - rework this logic
//            fragment.
//            View view = fragment.getView();
//            nameView = (EditText) view.findViewById(R.id.username);
//            if in second fragment
        if (isActivityShowingActivateFragment()) {
            otpFragment.performActivateTask();
        } else {
            loginFragment.performGetCodeTask();
        }
    }
*/

/*
    private boolean isActivityShowingActivateFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(
                R.id.fragment_mobile_get_code_layout);
        return fragment instanceof OTPFragment;
    }
*/

/*
    private void replaceLoginFragment() {
//        removes previous fragments before adding
//        // TODO: 26-02-2016
//        fragment already exists, exception thrown if blind add
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(loginFragment);
        fragmentTransaction.add(
                R.id.fragment_mobile_get_code_layout, loginFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void newLoginFragment(Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            savedInstanceState = getIntent().getExtras();
        }
        savedInstanceState.putParcelable(ContactItem.TAG, user);
        loginFragment = LoginFragment.newInstance(user);
        loginFragment.setArguments(savedInstanceState);
    }
*/

/*
    private void setupOTPFragment(ContactItem user) {
*/
/*
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                the back stack count is 0.. the 2nd fragment is getting recreated
//        this can happen during screen orientation change
//            only when the activity is destroyed and recreated, right in the 2nd fragment
//        sets up the fragment 1, so the back stack can play nicely
            replaceLoginFragment();
        }
*//*

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        otpFragment = OTPFragment.newInstance(user);
        Bundle arguments = new Bundle();
        arguments.putParcelable(ContactItem.TAG, user);
        otpFragment.setArguments(arguments);
//        removes previous fragments before adding
//        // TODO: 26-02-2016
//        fragment already exists, exception thrown if blind add
        fragmentTransaction.remove(otpFragment);
        fragmentTransaction.add(
                R.id.fragment_mobile_get_code_layout, otpFragment);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
*/

/*
    @Override
    public void onActivateSuccess(ContactItem user) {
        //            do in background returns success.. handle them
//        this activity, is called for result with this intent.. so the parent is aware
        Intent intent = getIntent();
        intent.putExtra(ContactItem.TAG, (Parcelable) user);
        setResult(Activity.RESULT_OK, intent);
//        startActivity(intent);
        finish();
    }

*/

}
