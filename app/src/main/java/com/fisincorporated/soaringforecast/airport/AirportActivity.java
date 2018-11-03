package com.fisincorporated.soaringforecast.airport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.fisincorporated.soaringforecast.airport.list.AirportListFragment;
import com.fisincorporated.soaringforecast.airport.search.AirportSearchFragment;
import com.fisincorporated.soaringforecast.airport.airportweather.AirportMetarTafFragment;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.MasterActivity;
import com.fisincorporated.soaringforecast.messages.AddAirportEvent;
import com.fisincorporated.soaringforecast.messages.DisplayAirportList;
import com.fisincorporated.soaringforecast.messages.DisplaySettings;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.settings.SettingsActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

public class AirportActivity extends MasterActivity {

    private static final String AIRPORT_OPTION = "AIRPORT_OPTION";
    private static final String AIRPORT_METAR_TAF = "AIRPORT_METAR_TAF";
    private static final String AIRPORT_SEARCH = "AIRPORT_SEARCH";
    private static final String AIRPORT_LIST_MAINTENANCE = "AIRPORT_LIST_MAINTENANCE";

    public String airportFragmentOption;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

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
        displayFragment(getAirportSearchFragment(),true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplayAirportList event) {
        displayFragment(getAirportListFragment(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DisplaySettings event) {
        displaySettingsActivity();
    }

    private Fragment getAirportListFragment() {
        return  AirportListFragment.newInstance(appRepository, appPreferences);
    }

    private Fragment getAirportSearchFragment() {
        return AirportSearchFragment.newInstance(appRepository, appPreferences);
    }

    private Fragment getAirportMetarTafFragment() {
        return AirportMetarTafFragment.newInstance(appRepository, appPreferences);
    }

    private void displaySettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
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
