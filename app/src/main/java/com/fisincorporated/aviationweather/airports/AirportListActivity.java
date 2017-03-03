package com.fisincorporated.aviationweather.airports;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Inject;

public class AirportListActivity extends AppCompatActivity implements  AirportListViewModel.EntryCompleteListener{

    @Inject
    public AirportListViewModel airportListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((WeatherApplication) getApplication()).getComponent().inject(this);

        airportListViewModel.setView((ViewGroup) findViewById(android.R.id.content))
                .setEntryCompleteListener(this);

    }

    @Override
    public void onEntryComplete() {
        finish();
    }
}
