package com.fisincorporated.soaringforecast.satellite.noaa;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class NoaaSatelliteImageFragment extends DaggerFragment {

    @Inject
    NoaaSatelliteViewModel satelliteViewModel;

    public static NoaaSatelliteImageFragment newInstance() {
        NoaaSatelliteImageFragment noaaSatelliteImageFragment = new NoaaSatelliteImageFragment();
        return noaaSatelliteImageFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.satellite_image_fragment_noaa, container, false);
        satelliteViewModel.setView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.noaa_satellite);
        satelliteViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        satelliteViewModel.onPause();

    }
}
