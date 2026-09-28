package intellibitz.intellidroid.domain

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import intellibitz.intellidroid.IntellibitzActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

@Suppress("DEPRECATION")
class MainSettingsActivity : AppCompatPreferenceActivity() {

    private var user: ContactItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        setupAppBar()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
    }

    private fun setupAppBar() {
        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        toolbar.setTitle(R.string.settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            val intent = Intent(this, IntellibitzActivity::class.java).apply {
                putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
            }
            NavUtils.navigateUpTo(this, intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
