package com.fisincorporated.aviationweather.soaring.forecast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fisincorporated.aviationweather.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragment extends DaggerFragment {

    public static final String MASTER_FORECAST_FRAGMENT = "MASTER_FORECAST_FRAGMENT";

    @Inject
    SoaringForecastViewModel soaringForecastViewModel;

    public SoaringForecastFragment() {}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.soaring_forecast_rasp, container, false);
        soaringForecastViewModel.setView(this, view);
        checkForGooglePlayServices();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.rasp);
        soaringForecastViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        soaringForecastViewModel.onPause();

    }
    private void checkForGooglePlayServices() {
        int GooglePlayAvailableCode;
        GooglePlayAvailableCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (ConnectionResult.SUCCESS != GooglePlayAvailableCode) {
            Toast.makeText(getContext(), "GooglePlayServices not available",
                    Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }
}
