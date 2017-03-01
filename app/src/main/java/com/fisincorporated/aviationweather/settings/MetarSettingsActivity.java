package com.fisincorporated.aviationweather.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.fisincorporated.aviationweather.R;


// Cribbed various code
// http://alvinalexander.com/android/android-tutorial-preferencescreen-preferenceactivity
// -preferencefragment
// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android
// -preference-in-the-preference-su/4325239#4325239
public class MetarSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new
                SettingsPreferenceFragment()).commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.display_preferences);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener
                    (this);
            initSummary(getPreferenceScreen());
        }

        private void initSummary(Preference preference) {
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) preference;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    initSummary(pGrp.getPreference(i));
                }
            } else {
                updatePrefSummary(preference);
            }
        }

        private void updatePrefSummary(Preference preference) {
            if (preference instanceof ListPreference) {
                ListPreference listPref = (ListPreference) preference;
                preference.setSummary(listPref.getEntry());
            }
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                if (preference.getTitle().toString().toLowerCase().contains("password")) {
                    preference.setSummary("******");
                } else {
                    preference.setSummary(editTextPref.getText());
                }
            }
            if (preference instanceof MultiSelectListPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                preference.setSummary(editTextPref.getText());
            }
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            }
        }

    }
}
