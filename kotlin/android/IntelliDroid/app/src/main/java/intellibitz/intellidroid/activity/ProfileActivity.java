package intellibitz.intellidroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.fragment.ProfileItemFragment;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.domain.MainSettingsActivity;
import intellibitz.intellidroid.fragment.ProfileItemFragment;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;

import java.io.File;

public class ProfileActivity extends
        IntellibitzTwoPaneUserActivity implements
        ProfileListener,
        ProfileTopicListener {

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        setupTwopane();
        setupAppBar();
        setupFragments();
    }


    private void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        toolbar.setTitle(R.string.profile);
        toolbar.setSubtitle(R.string.app_title);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
            NavUtils.navigateUpTo(this, new Intent(this, MainSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFragments() {
        final ProfileItemFragment profileItemFragment = ProfileItemFragment.newInstance(this, user);
        replaceContentFragment(profileItemFragment);
/*
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String name = null;
        String fileName = null;
        if (null == user) {
//            name = savedInstanceState.getString(MainApplicationSingleton.NAME_PARAM);
//            fileName = savedInstanceState.getString(MainApplicationSingleton.PROFILE_PIC_PARAM);
        } else {
            name = user.getName();
            fileName = user.getProfilePic();
        }
*/
/*
        fragmentTransaction.replace(
                R.id.fragment_profile_layout, profileItemFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
*/
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
    public void onBackPressed() {
//        if no more fragments is left.. then its the workflow activity which is blank
//        finish the blank activity.. to go back
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        the activity will be destroyed, a chance to save the state
//        super.onSaveInstanceState(outState);
        ProfileItemFragment fragment = (ProfileItemFragment)
                getSupportFragmentManager().getFragments().get(0);
        outState.putParcelable(ContactItem.USER_CONTACT, user);
    }

    @Override
    public void onProfilePicChanged(File file) {

    }

    @Override
    public void onProfileTopicClicked(ContactItem item) {
//        setProfileHeaders(item);
/*
        NestDetailFragment detailFragment = NestDetailFragment.newInstance(
                this, mainActivity, twoPane, item, user);
*/
//        mainActivity.showDetailToolbar();
//        mainActivity.replaceDetailFragment(detailFragment);
    }

    @Override
    public void onProfileTopicsLoaded(int count) {

    }

    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);

    }
/*
    public class ProfilePicUploadTask extends AsyncTask<File, Void, Boolean> {

        private String uid;
        private String token;
        private String device = "android";
        private String deviceRef;
        private String url;
        private JSONObject response;
        private String fileName;

        ProfilePicUploadTask(String uid, String token, String device, String deviceRef, String url) {
            this.uid = uid;
            this.token = token;
            this.device = device;
            this.url = url;
            this.deviceRef = deviceRef;
        }

        @Override
        protected Boolean doInBackground(File... params) {
            File file = params[0];
            fileName = file.getAbsolutePath();
            String charset = "UTF-8";
            try {
                HttpUrlConnectionParser.MultipartUtility multipart =
                        new HttpUrlConnectionParser.MultipartUtility(url, charset);
                multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
                multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
                multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
                multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
                multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName);
                // params comes from the execute() call: params[0] is the url.
                response = multipart.finishAsJSON(); // response from server.
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null != response;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            uploadProfilePicTask = null;
            try {
                int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
                if (99 == status) {
                    Bundle bundle = new Bundle();
                    bundle.putString("error", response.toString());
                    System.out.println("PROFILE UPLOAD ERROR - " + response);
                } else if (1 == status) {
//                    SUCCESS
                    //// TODO: 05-02-2016
//                    token ok, password to be set for user
//                    String url = "https://www.google.com";
                    MainApplicationSingleton.getInstance(getContext()).putStringValueSP(
                            MainApplicationSingleton.PROFILE_PIC_PARAM, fileName);
                    System.out.println("PROFILE UPLOAD SUCCESS - " + response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (success) {
//                // TODO: 25-02-2016
//                do the ui flow right..move this to a service or something
//                stay on the activity..
//                finish();
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            uploadProfilePicTask = null;
        }

    }
*/

}
