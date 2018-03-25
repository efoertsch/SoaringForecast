package com.fisincorporated.aviationweather.satellite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppFragment;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Inject;


public class SatelliteImageFragment extends AppFragment {

    @Inject
    SatelliteViewModel satelliteViewModel;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.satellite_image_fragment, container, false);
        ((WeatherApplication) getActivity().getApplication()).getComponent().inject(this);
        satelliteViewModel.setView(view);
        satelliteViewModel.setDataLoading(dataLoading);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        satelliteViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        satelliteViewModel.onPause();

    }
}
