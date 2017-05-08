package com.fisincorporated.aviationweather.satellite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;


import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Inject;

public class SatelliteImageActivity extends AppCompatActivity {

    @Inject
    SatelliteViewModel satelliteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ((WeatherApplication) getApplication()).getComponent().inject(this);
        satelliteViewModel.setView((ViewGroup) findViewById(android.R.id.content));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume() {
        super.onResume();
        satelliteViewModel.onResume();

    }

    public void onPause() {
        super.onPause();
        satelliteViewModel.onPause();

    }
}
