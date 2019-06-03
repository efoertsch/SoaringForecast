package org.soaringforecast.rasp.soaring.forecast;


import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.about.AboutActivity;
import org.soaringforecast.rasp.airport.AirportActivity;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.messages.CallFailure;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.databinding.AppNavDrawerBinding;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.satellite.SatelliteActivity;
import org.soaringforecast.rasp.settings.SettingsActivity;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.task.TaskActivity;
import org.soaringforecast.rasp.turnpointview.IAmDone;
import org.soaringforecast.rasp.turnpointview.TurnpointSatelliteViewFragment;
import org.soaringforecast.rasp.utils.ViewUtilities;
import org.soaringforecast.rasp.windy.WindyActivity;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

// Nav bar http://guides.codepath.com/android/fragment-navigation-drawer#setup-toolbar

public class ForecastDrawerActivity extends DaggerAppCompatActivity {

    private AppNavDrawerBinding appNavDrawerBinding;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Inject
    AppRepository appRepository;

    @Inject
    AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appNavDrawerBinding = DataBindingUtil.setContentView(this, R.layout.app_nav_drawer);
        drawerLayout = appNavDrawerBinding.appDrawerLayout;
        toolbar = appNavDrawerBinding.appDrawerToolbarLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = setupDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        NavigationView navigationView = appNavDrawerBinding.appWeatherDrawer;
        setupDrawerContent(navigationView);
        checkForGooglePlayServices();
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

    public void checkForecastsToBeDisplayed() {
        NavigationView navigationView = findViewById(R.id.app_weather_drawer);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_menu_soaring_forecasts);
        if (appPreferences.isAnyForecastOptionDisplayed()) {
            menuItem.setVisible(true);

            menuItem = menu.findItem(R.id.nav_menu_windy);
            if (menuItem != null) {
                menuItem.setVisible(appPreferences.isWindyDisplayed());
            }
            menuItem = menu.findItem(R.id.nav_menu_skysight);
            if (menuItem != null) {
                menuItem.setVisible(appPreferences.isSkySightDisplayed());
            }
            menuItem = menu.findItem(R.id.nav_menu_dr_jacks);
            if (menuItem != null) {
                menuItem.setVisible(appPreferences.isDrJacksDisplayed());
            }
        } else {
            menuItem.setVisible(false);
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
            case R.id.nav_menu_windy:
                displayWindy();
                break;
            case R.id.nav_menu_skysight:
                startSkySightBrowser();
                break;
            case R.id.nav_menu_dr_jacks:
                startDrJacksBrowser();
                break;
            case R.id.nav_menu_airport_weather:
                displayAirportMetarTaf();
                break;
            case R.id.nav_menu_noaa_satellite:
                displayNoaaSatelliteFragment();
                break;
            case R.id.nav_menu_geos_satellite:
                displayGeossSatelliteFragment();
                break;
            case R.id.nav_menu_airport_list:
                displayAirportListFragment();
                break;
            case R.id.nav_menu_task_list:
                displayTaskList();
                break;
            case R.id.nav_menu_import_turnpoints:
                displayTurnpointsImport();
                break;
            case R.id.nav_menu_settings:
                displaySettingsActivity();
                break;
            case R.id.nav_menu_about:
                displayAbout();
                break;
        }
        drawerLayout.closeDrawers();
    }

    private void displayFragment(Fragment fragment, boolean replace, boolean addToBackstack) {
        FragmentTransaction fragmentTransaction;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (replace) {
            fragmentTransaction = fragmentManager.beginTransaction()
                    .replace(R.id.app_frame_layout, fragment);
        } else {
            fragmentTransaction = fragmentManager.beginTransaction()
                    .add(R.id.app_frame_layout, fragment);
        }

        if (addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private void displayAirportListFragment() {
        AirportActivity.Builder builder = AirportActivity.Builder.getBuilder();
        builder.displayAirportListMaintenace();
        startActivity(builder.build(this));
    }

    private void displayWindy() {
        WindyActivity.Builder builder = WindyActivity.Builder.getBuilder();
        startActivity(builder.build(this));
    }

    private void displayAirportMetarTaf() {
        AirportActivity.Builder builder = AirportActivity.Builder.getBuilder();
        builder.displayMetarTaf();
        startActivity(builder.build(this));
    }

    private void displaySettingsActivity() {
        SettingsActivity.Builder builder = SettingsActivity.Builder.getBuilder();
        startActivity(builder.displaySettings().build(this));
    }

    private void displayAbout() {
        Intent i = new Intent(this, AboutActivity.class);
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
        displayFragment(new SoaringForecastFragment(), true, false);
    }

    private void displayTaskList() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList().enableClickTask(false);
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

    private void checkForGooglePlayServices() {
        int GooglePlayAvailableCode;
        GooglePlayAvailableCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == GooglePlayAvailableCode) {
            displaySoaringForecasts();
        } else {
            displayNoGoogleServicesAlert();
        }
    }

    private void displayNoGoogleServicesAlert() {
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.oops)
                .setMessage(R.string.need_google_play_services)
                .setPositiveButton(R.string.exit, (dialog, id) -> {
                    finish();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void displayCallFailure(CallFailure callFailure) {
        ViewUtilities.displayErrorDialog(appNavDrawerBinding.getRoot(), getString(R.string.oops), callFailure.getcallFailure());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SnackbarMessage message) {
        Snackbar.make(appNavDrawerBinding.appCoordinatorLayout, message.getMessage(), message.getDuration())
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTurnpointMessageEvent(DisplayTurnpoint displayTurnpoint) {
        TurnpointSatelliteViewFragment turnpointSatelliteViewFragment = TurnpointSatelliteViewFragment.newInstance(displayTurnpoint.getTurnpoint());
        displayFragment(turnpointSatelliteViewFragment, false, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIAmDoneMessageEvent(IAmDone iAmDone) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.app_frame_layout);
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    }
}


