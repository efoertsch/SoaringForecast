package com.fisincorporated.soaringforecast.airport.airportweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.messages.AddAirportEvent;
import com.fisincorporated.soaringforecast.messages.DisplayAirportList;
import com.fisincorporated.soaringforecast.messages.DisplaySettings;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportMetarTafFragment extends DaggerFragment {

    @Inject
    public AirportMetarTafViewModel airportMetarTafViewModel;

    public AirportMetarTafFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.airport_weather_fragment, container, false);
        airportMetarTafViewModel.setView(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.airport_activity_menu, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.nav_drawer_metar_taf_label);
        airportMetarTafViewModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        airportMetarTafViewModel.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.airport_activity_menu_add:
                displayAirportSearchFragment();
                return true;
            case R.id.airport_activity_menu_list:
               airportListFragment();
                return true;
            case R.id.airport_activity_metar_options:
                displaySettingsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayAirportSearchFragment() {
        EventBus.getDefault().post(new AddAirportEvent());
    }

    private void airportListFragment() {
        EventBus.getDefault().post(new DisplayAirportList());
    }

    private void displaySettingsMenu() {
        EventBus.getDefault().post(new DisplaySettings());
    }
}
