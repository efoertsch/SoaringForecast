package com.fisincorporated.soaringforecast.airport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.airport.list.AirportListFragment;
import com.fisincorporated.soaringforecast.airport.search.AirportSearchFragment;
import com.fisincorporated.soaringforecast.airportweather.AirportMetarTafFragment;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.MasterActivity;
import com.fisincorporated.soaringforecast.messages.AddAirportEvent;
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

    private MenuItem airportListItem;
    private MenuItem airportAddItem;

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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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
                    return airportListFragment();
            }
        }
        return getAirportMetarTafFragment();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.airport_activity_menu, menu);
        airportListItem = menu.findItem(R.id.airport_activity_menu_list);
        airportAddItem = menu.findItem(R.id.airport_activity_menu_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.airport_activity_menu_add:
                airportFragmentOption =  AIRPORT_SEARCH;
                toggleAirportMenuOptions();
                displayFragment(getAirportSearchFragment(),true);
                return true;
            case R.id.airport_activity_menu_list:
                airportFragmentOption =  AIRPORT_LIST_MAINTENANCE;
                toggleAirportMenuOptions();
                displayFragment(airportListFragment(),true);
                return true;
            case R.id.airport_activity_metar_options:
                displaySettingsActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleAirportMenuOptions() {
        boolean displayAdd = (AIRPORT_SEARCH.equals(airportFragmentOption));
        // toggle satellite menu options for display of other satellite
        if (airportAddItem != null) {
            airportListItem.setVisible(!displayAdd);
        }
        if (airportListItem!= null) {
            airportListItem.setVisible(displayAdd);
        }
    }

    private Fragment airportListFragment() {
        return  AirportListFragment.newInstance(appRepository, appPreferences);
    }

    private Fragment getAirportSearchFragment() {
        return AirportSearchFragment.newInstance(appRepository, appPreferences);
    }

    private Fragment getAirportMetarTafFragment() {
        return new AirportMetarTafFragment();
    }

    private void displaySettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddAirportEvent event) {
        displayNewFragment(getAirportSearchFragment());
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
