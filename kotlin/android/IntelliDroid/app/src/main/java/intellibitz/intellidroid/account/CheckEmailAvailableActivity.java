package intellibitz.intellidroid.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;

public class CheckEmailAvailableActivity extends
        IntellibitzTwoPaneUserActivity {

    private static final String TAG = "ChkEmailAvblAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkemailavailable);
//        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            setupAlert(getIntent().getBooleanExtra("isRegisterAgain", false), findViewById(R.id.cl));
            setupTwopane();
//        setupAppBar();
            setupFragments();
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
    }

    private void setupFragments() {
        final CheckEmailAvailableFragment checkEmailAvailableFragment =
                CheckEmailAvailableFragment.newInstance(user);
        replaceContentFragment(checkEmailAvailableFragment);
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
                    Intent intent = new Intent(CheckEmailAvailableActivity.this,
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
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
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


}
