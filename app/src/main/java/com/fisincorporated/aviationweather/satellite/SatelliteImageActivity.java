package com.fisincorporated.aviationweather.satellite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.DataLoading;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Inject;

public class SatelliteImageActivity extends AppCompatActivity implements DataLoading {

    private ProgressBar loadingProgressBar;

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

        loadingProgressBar = (ProgressBar) findViewById(R.id.activity_load_progress_bar);

        ((WeatherApplication) getApplication()).getComponent().inject(this);
        satelliteViewModel.setView((ViewGroup) findViewById(android.R.id.content)).setDataLoading(this);

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


    @Override
    public void loadRunning(final boolean dataLoading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingProgressBar.setVisibility(dataLoading ? View.VISIBLE : View.GONE);
            }
        });
    }

}
