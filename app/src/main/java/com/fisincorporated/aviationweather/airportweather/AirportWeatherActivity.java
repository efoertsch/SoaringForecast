package com.fisincorporated.aviationweather.airportweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.airports.AirportListActivity;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.settings.SettingsActivity;

import javax.inject.Inject;

public class AirportWeatherActivity extends AppCompatActivity {

    public static final String METAR_LIST = "METAR_LIST";

    @Inject
    public AirportWeatherViewModel airportWeatherViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_airport_weather);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                airportWeatherViewModel.refresh();
            }
        });

        ((WeatherApplication) getApplication()).getComponent().inject(this);

        airportWeatherViewModel.setView((ViewGroup) findViewById(android.R.id.content));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.weather_drawer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_menu_display_options:
                displaySettingsActivity();
                return true;
            case R.id.nav_menu_add_airport_codes:
                displayAirportList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void displayAirportList() {
        Intent i = new Intent(this, AirportListActivity.class);
        startActivity(i);
    }

    private void displaySettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onResume() {
        super.onResume();
        airportWeatherViewModel.onResume();

    }

    public void onPause() {
        super.onPause();
        airportWeatherViewModel.onPause();

    }
}


