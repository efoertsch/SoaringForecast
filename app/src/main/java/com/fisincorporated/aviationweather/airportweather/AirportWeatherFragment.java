package com.fisincorporated.aviationweather.airportweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppFragment;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Inject;

public class AirportWeatherFragment extends AppFragment {

    public static final String METAR_LIST = "METAR_LIST";

    @Inject
    public AirportWeatherViewModel airportWeatherViewModel;

    public AirportWeatherFragment() {}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.airport_weather_fragment, container, false);
        ((WeatherApplication) getActivity().getApplication()).getComponent().inject(this);
        airportWeatherViewModel.setView(view);
        airportWeatherViewModel.setDataLoading(dataLoading);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        airportWeatherViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        airportWeatherViewModel.onPause();

    }
}
