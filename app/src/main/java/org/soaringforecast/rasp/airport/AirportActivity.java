package org.soaringforecast.rasp.airport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.soaringforecast.rasp.airport.airportweather.AirportMetarTafFragment;
import org.soaringforecast.rasp.airport.list.AirportListFragment;
import org.soaringforecast.rasp.airport.search.AirportSearchFragment;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.airport.messages.AddAirportEvent;
import org.soaringforecast.rasp.airport.messages.DisplayAirportList;
import org.soaringforecast.rasp.settings.messages.DisplaySettings;
import org.soaringforecast.rasp.settings.SettingsActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AirportActivity extends MasterActivity {

    private static final String AIRPORT_OPTION = "AIRPORT_OPTION";
    private static final String AIRPORT_METAR_TAF = "AIRPORT_METAR_TAF";
    private static final String AIRPORT_SEARCH = "AIRPORT_SEARCH";
    private static final String AIRPORT_LIST_MAINTENANCE = "AIRPORT_LIST_MAINTENANCE";

    public String airportFragmentOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        airportFragmentOption = getIntent().getExtras().getString(AIRPORT_OPTION);
        return getAirportFragment();
    }

    @Nullable
    private Fragment getAirportFragment() {
        if (airportFragmentOption != null) {
            switch (airportFragmentOption) {
                case AIRPORT_METAR_TAF:
                    return getAirportMetarTafFragment();
                case AIRPORT_SEARCH:
                    return getAirportSearchFragment();
                case AIRPORT_LIST_MAINTENANCE:
                    return getAirportListFragment();
            }
        }
        return getAirportMetarTafFragment();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddAirportEvent event) {
        displayFragment(getAirportSearchFragment(), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplayAirportList event) {
        displayFragment(getAirportListFragment(), true,true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplaySettings event) {
        displaySettingsActivity();
    }

    private Fragment getAirportListFragment() {
        return new AirportListFragment();
    }

    private Fragment getAirportSearchFragment() {
        return new AirportSearchFragment();
    }

    private Fragment getAirportMetarTafFragment() {
        return new AirportMetarTafFragment();
    }

    private void displaySettingsActivity() {
        SettingsActivity.Builder builder = SettingsActivity.Builder.getBuilder();
        builder.displaySettings();
        startActivity(builder.build(this));
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static AirportActivity.Builder getBuilder() {
            AirportActivity.Builder builder = new AirportActivity.Builder();
            return builder;
        }

        public AirportActivity.Builder displayMetarTaf() {
            bundle.putString(AIRPORT_OPTION, AIRPORT_METAR_TAF);
            return this;
        }

        public AirportActivity.Builder displayAirportSearch() {
            bundle.putString(AIRPORT_OPTION, AIRPORT_SEARCH);
            return this;
        }

        public AirportActivity.Builder displayAirportListMaintenace() {
            bundle.putString(AIRPORT_OPTION, AIRPORT_LIST_MAINTENANCE);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, AirportActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}
