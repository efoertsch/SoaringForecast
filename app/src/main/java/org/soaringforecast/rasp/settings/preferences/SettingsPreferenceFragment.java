package org.soaringforecast.rasp.settings.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.SettingsFragment;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
// and modified somewhat
// Using PreferenceFragmentCompat so that dagger injection can be used in onAttach
// This required calling onCreatePreferences() but setting shared preferences there caused updated to be ignored
// So call is made just for form, no program logic done there.
public class SettingsPreferenceFragment extends SettingsFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    @Named("AIRPORT_PREFS")
    public String AIRPORT_PREFS;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AIRPORT_PREFS);
        PreferenceManager.setDefaultValues(getContext(), R.xml.display_preferences, false);
        addPreferencesFromResource(R.xml.display_preferences);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // See comments at top
        //  setPreferencesFromResource(R.xml.display_preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.settings));
        // Set up a listener whenever a key changes
        initSummary(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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
            preference.setSummary(editTextPref.getText());
        }
//        if (preference instanceof MultiSelectListPreference) {
//            EditTextPreference editTextPref = (EditTextPreference) preference;
//            preference.setSummary(editTextPref.getText());
//        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

}
