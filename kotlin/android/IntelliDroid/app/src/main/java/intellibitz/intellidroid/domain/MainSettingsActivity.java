package intellibitz.intellidroid.domain;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;

import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.data.ContactItem;

/**
 *
 */
public class MainSettingsActivity extends
        AppCompatPreferenceActivity {

    private ContactItem user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        setupAppBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
    }

    private void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle(R.string.settings);
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
            Intent intent = new Intent(this, IntellibitzActivity.class);
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
