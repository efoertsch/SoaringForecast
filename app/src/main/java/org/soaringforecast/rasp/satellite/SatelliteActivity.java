package org.soaringforecast.rasp.satellite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.MasterActivity;
import org.soaringforecast.rasp.satellite.geos.GeosSatelliteFragment;
import org.soaringforecast.rasp.satellite.noaa.NoaaSatelliteImageFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SatelliteActivity extends MasterActivity {

    private static final String SATELLITE_DISPLAY = "SATELLITE_DISPLAY";
    private static final String NOAA_SATELLITE = "NOAA_SATELLITE";
    private static final String GEOS_SATELLITE = "GEOS_SATELLITE";

    private MenuItem geosMenuItem;
    private MenuItem noaaMenuItem;

    private String satelliteType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected Fragment createFragment() {
        satelliteType = getIntent().getExtras().getString(SATELLITE_DISPLAY);
        return getSatelliteFragment();
    }

    @Nullable
    private Fragment getSatelliteFragment() {
        if (satelliteType != null) {
            switch (satelliteType) {
                case NOAA_SATELLITE:
                    return getNoaaSatelliteFragment();
                case GEOS_SATELLITE:
                    return getGeosSatelliteFragment();
            }
        }
        return getNoaaSatelliteFragment();
    }

    private Fragment getNoaaSatelliteFragment() {
        return NoaaSatelliteImageFragment.newInstance();
    }

    private Fragment getGeosSatelliteFragment() {
        return GeosSatelliteFragment.newInstance();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.satellite_activity_menu, menu);
        geosMenuItem = menu.findItem(R.id.satellite_menu_geos);
        noaaMenuItem = menu.findItem(R.id.satellite_menu_noaa);
        toggleSatelliteMenuOptions();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.satellite_menu_geos:
                satelliteType = GEOS_SATELLITE;
                toggleSatelliteMenuOptions();
                displayFragment(getGeosSatelliteFragment(), true, false);
                return true;
            case R.id.satellite_menu_noaa:
                satelliteType = NOAA_SATELLITE;
                toggleSatelliteMenuOptions();
                displayFragment(getNoaaSatelliteFragment(), true, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleSatelliteMenuOptions() {
        boolean displayGeos = (GEOS_SATELLITE.equals(satelliteType));
        // toggle satellite menu options for display of other satellite
        if (geosMenuItem != null) {
            geosMenuItem.setVisible(!displayGeos);
        }
        if (noaaMenuItem != null) {
            noaaMenuItem.setVisible(displayGeos);
        }
    }


    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static SatelliteActivity.Builder getBuilder() {
            return new SatelliteActivity.Builder();
        }

        public SatelliteActivity.Builder displayNoaaSatellite() {
            bundle.putString(SATELLITE_DISPLAY, NOAA_SATELLITE);
            return this;
        }

        public SatelliteActivity.Builder displayGeosSatellite() {
            bundle.putString(SATELLITE_DISPLAY, GEOS_SATELLITE);
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, SatelliteActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
