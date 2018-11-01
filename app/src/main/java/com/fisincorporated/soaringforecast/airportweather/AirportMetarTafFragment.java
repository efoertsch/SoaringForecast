package com.fisincorporated.soaringforecast.airportweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportMetarTafFragment extends DaggerFragment {

    @Inject
    public AirportMetarTafViewModel airportMetarTafViewModel;

    public AirportMetarTafFragment() {}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.airport_weather_fragment, container, false);
        airportMetarTafViewModel.setView(view);
        return view;
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
}
