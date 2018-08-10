package com.fisincorporated.aviationweather.airport.codelist;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fisincorporated.aviationweather.R;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class AirportListActivity extends DaggerAppCompatActivity implements  AirportCodeListViewModel.EntryCompleteListener{

    @Inject
    public AirportCodeListViewModel airportCodeListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        airportCodeListViewModel.setView(findViewById(android.R.id.content))
                .setEntryCompleteListener(this);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onEntryComplete() {
        finish();
    }
}
