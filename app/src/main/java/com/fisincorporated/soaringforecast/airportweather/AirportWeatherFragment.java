package com.fisincorporated.soaringforecast.airportweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AirportWeatherFragment extends DaggerFragment {

    public static final String METAR_LIST = "METAR_LIST";

    @Inject
    public AirportWeatherViewModel airportWeatherViewModel;

    public AirportWeatherFragment() {}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.airport_weather_fragment, container, false);
        airportWeatherViewModel.setView(view);
        //set title
        getActivity().setTitle(R.string.action_airport_weather);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.nav_drawer_metar_taf_label);
        airportWeatherViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        airportWeatherViewModel.onPause();

    }
}
