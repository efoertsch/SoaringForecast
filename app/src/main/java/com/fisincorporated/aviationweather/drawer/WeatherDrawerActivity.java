package com.fisincorporated.aviationweather.drawer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.airports.AirportListActivity;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherViewModel;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.settings.SettingsActivity;

import javax.inject.Inject;

// Nav bar http://guides.codepath.com/android/fragment-navigation-drawer#setup-toolbar

public class WeatherDrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private  NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Inject
    public AirportWeatherViewModel airportWeatherViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ((WeatherApplication) getApplication()).getComponent().inject(this);

        setContentView(R.layout.app_nav_drawer);

        drawerLayout = (DrawerLayout) findViewById(R.id.app_drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle  = setupDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                airportWeatherViewModel.refresh();
            }
        });

        navigationView = (NavigationView) findViewById(R.id.app_weather_drawer);
        setupDrawerContent(navigationView);

        airportWeatherViewModel.setView((ViewGroup) findViewById(android.R.id.content));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);

    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.add_airport_code:
                displayAirportList();
                break;
            case R.id.metar_menu_settings:
                displaySettingsActivity();
                break;
        }

        drawerLayout.closeDrawers();

    }


    private void displayAirportList() {
        Intent i = new Intent(this, AirportListActivity.class);
        startActivity(i);
    }

    private void displaySettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }


}
