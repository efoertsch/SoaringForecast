package org.soaringforecast.rasp.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.settings.forecastorder.ForecastOrderFragment;
import org.soaringforecast.rasp.settings.preferences.SettingsPreferenceFragment;
import org.soaringforecast.rasp.settings.regions.RegionSelectionFragment;


// Cribbed various code
// http://alvinalexander.com/android/android-tutorial-preferencescreen-preferenceactivity
// -preferencefragment
// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android
// -preference-in-the-preference-su/4325239#4325239
public class SettingsActivity extends MasterActivity {

    private static final String DISPLAY_REQUEST = "DISPLAY_REQUEST";
    private static final String DISPLAY_SETTINGS = "DISPLAY_SETTINGS";
    private static final String SELECT_REGION = "SELECT_REGION";
    private static final String DISPLAY_ORDER_FORECASTS = "DISPLAY_ORDER_FORECASTS";

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected Fragment createFragment() {
        String request = getIntent().getExtras().getString(DISPLAY_REQUEST);
        if (request != null) {
            switch (request) {
                case DISPLAY_SETTINGS:
                    return getSettingsFragment();
                case SELECT_REGION:
                    return getRegionSelectionFragment();
                case DISPLAY_ORDER_FORECASTS:
                    return getOrderForecastsFragment();
            }
        }
        return getSettingsFragment();
    }

    private Fragment getOrderForecastsFragment() {
        return new ForecastOrderFragment();
    }

    private Fragment getRegionSelectionFragment() {
        return new RegionSelectionFragment();
    }

    private Fragment getSettingsFragment() {
        return new SettingsPreferenceFragment();
    }


    public static class Builder {
        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static SettingsActivity.Builder getBuilder() {
            SettingsActivity.Builder builder = new SettingsActivity.Builder();
            return builder;
        }

        public SettingsActivity.Builder displaySettings() {
            bundle.putString(DISPLAY_REQUEST, DISPLAY_SETTINGS);
            return this;
        }

        public SettingsActivity.Builder displaySelectRegion() {
            bundle.putString(DISPLAY_REQUEST, SELECT_REGION);
            return this;
        }

        public SettingsActivity.Builder displayOrderedForecasts() {
            bundle.putString(DISPLAY_REQUEST, DISPLAY_ORDER_FORECASTS);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
