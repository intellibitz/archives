package intellibitz.intellidroid.domain

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import intellibitz.intellidroid.R

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupSimplePreferencesScreen()
    }

    private fun setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return
        }

        addPreferencesFromResource(R.xml.pref_general)

        val fakeHeader = PreferenceCategory(this).apply {
            setTitle(R.string.pref_header_notifications)
        }
        preferenceScreen.addPreference(fakeHeader)
        addPreferencesFromResource(R.xml.pref_notification)

        val fakeHeaderData = PreferenceCategory(this).apply {
            setTitle(R.string.pref_header_data_sync)
        }
        preferenceScreen.addPreference(fakeHeaderData)
        addPreferencesFromResource(R.xml.pref_data_sync)

        bindPreferenceSummaryToValue(findPreference("example_text"))
        bindPreferenceSummaryToValue(findPreference("example_list"))
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"))
        bindPreferenceSummaryToValue(findPreference("sync_frequency"))
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this) && !isSimplePreferences(this)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target)
        }
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || NotificationPreferenceFragment::class.java.name == fragmentName
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)
            bindPreferenceSummaryToValue(findPreference("example_text"))
            bindPreferenceSummaryToValue(findPreference("example_list"))
        }

        @Deprecated("Deprecated in Java")
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class NotificationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"))
        }

        @Deprecated("Deprecated in Java")
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)
            bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        @Deprecated("Deprecated in Java")
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val ALWAYS_SIMPLE_PREFS = false

        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()

                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    preference.summary = if (index >= 0) preference.entries[index] else null
                } else if (preference is RingtonePreference) {
                    if (TextUtils.isEmpty(stringValue)) {
                        preference.setSummary(R.string.pref_ringtone_silent)
                    } else {
                        val ringtone = RingtoneManager.getRingtone(
                            preference.context, Uri.parse(stringValue)
                        )
                        if (ringtone == null) {
                            preference.summary = null
                        } else {
                            val name = ringtone.getTitle(preference.context)
                            preference.summary = name
                        }
                    }
                } else {
                    preference.summary = stringValue
                }
                true
            }

        private fun isXLargeTablet(context: Context): Boolean {
            return (context.resources.configuration.screenLayout
                    and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        private fun isSimplePreferences(context: Context): Boolean {
            return (ALWAYS_SIMPLE_PREFS
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                    || !isXLargeTablet(context))
        }

        private fun bindPreferenceSummaryToValue(preference: Preference?) {
            if (preference == null) return
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }
    }
}
