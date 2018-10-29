package com.fisincorporated.soaringforecast.drawer;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.airport.list.AirportListFragment;
import com.fisincorporated.soaringforecast.airport.search.AirportSearchFragment;
import com.fisincorporated.soaringforecast.airportweather.AirportWeatherFragment;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.messages.AddAirportEvent;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.satellite.SatelliteActivity;
import com.fisincorporated.soaringforecast.settings.SettingsActivity;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastFragment;
import com.fisincorporated.soaringforecast.task.TaskActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

// Nav bar http://guides.codepath.com/android/fragment-navigation-drawer#setup-toolbar

public class WeatherDrawerActivity extends DaggerAppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    //private ProgressBar loadingProgressBar;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_nav_drawer);

        drawerLayout = findViewById(R.id.app_drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = setupDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        //loadingProgressBar = findViewById(R.id.activity_load_progress_bar);

        NavigationView navigationView = findViewById(R.id.app_weather_drawer);
        setupDrawerContent(navigationView);
        displaySoaringForecasts();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        checkForecastsToBeDisplayed();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    public void  checkForecastsToBeDisplayed() {
        NavigationView navigationView = findViewById(R.id.app_weather_drawer);
        Menu menu= navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_menu_skysight);
        if (menuItem != null) {
            menuItem.setVisible(appPreferences.isSkySightDisplayed());
        }
        menuItem = menu.findItem(R.id.nav_menu_dr_jacks);
        if (menuItem != null) {
            menuItem.setVisible(appPreferences.isDrJacksDisplayed());
        }
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
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_menu_airport_list:
                displayAirportListFragment();
                break;
            case R.id.nav_menu_airport_weather:
                displayAirportWeather();
                break;
            case R.id.nav_menu_display_options:
                displaySettingsActivity();
                break;
            case R.id.nav_menu_noaa_satellite:
                displayNoaaSatelliteFragment();
                break;
            case R.id.nav_menu_geos_satellite:
                displayGeossSatelliteFragment();
                break;
            case R.id.nav_menu_rasp:
                displaySoaringForecasts();
                break;
            case R.id.nav_menu_skysight:
                startSkySightBrowser();
                break;
            case R.id.nav_menu_dr_jacks:
                startDrJacksBrowser();
                break;
            case R.id.nav_menu_task_list:
                displayTaskList();
                break;
            case R.id.nav_menu_import_turnpoints:
                displayTurnpointsImport();
                break;
        }
        drawerLayout.closeDrawers();
    }


    private void displayFragment(Fragment fragment, boolean addToBackstack) {
        // Replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.app_frame_layout, fragment);
        if(addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

    }


    private void displayAirportListFragment() {
        AirportListFragment fragment = new AirportListFragment();
        displayFragment(fragment, true);
    }

    private void displayAirportWeather() {
        AirportWeatherFragment fragment = new AirportWeatherFragment();
        displayFragment(fragment, true);
    }

    private void displaySettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void displayNoaaSatelliteFragment() {
        SatelliteActivity.Builder builder = SatelliteActivity.Builder.getBuilder();
        builder.displayNoaaSatellite();
        startActivity(builder.build(this));
    }

    private void displayGeossSatelliteFragment() {
        SatelliteActivity.Builder builder = SatelliteActivity.Builder.getBuilder();
        builder.displayGeosSatellite();
        startActivity(builder.build(this));
    }

    private void displaySoaringForecasts() {
        SoaringForecastFragment fragment = new SoaringForecastFragment();
        displayFragment(fragment, false);
    }

    private void displayTaskList(){
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList();
        startActivity(builder.build(this));
    }

    private void displayTurnpointsImport() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTurnpointImport();
        startActivity(builder.build(this));
    }

    private void startDrJacksBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.drjack.info/BLIP/univiewer.html"));
        startActivity(browserIntent);
    }

    private void startSkySightBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://skysight.io/secure/#"));
        startActivity(browserIntent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddAirportEvent event) {
        AirportSearchFragment airportSearchFragment = AirportSearchFragment.newInstance(appRepository, appPreferences);
        displayFragment(airportSearchFragment, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SnackbarMessage message) {
        Snackbar.make(findViewById(R.id.app_coordinator_layout), message.getMessage(),
                Snackbar.LENGTH_INDEFINITE)
                .show();
    }

}
