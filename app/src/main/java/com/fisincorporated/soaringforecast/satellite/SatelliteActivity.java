package com.fisincorporated.soaringforecast.satellite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.soaringforecast.common.MasterActivity;
import com.fisincorporated.soaringforecast.satellite.geos.GeosSatelliteFragment;
import com.fisincorporated.soaringforecast.satellite.noaa.NoaaSatelliteImageFragment;

public class SatelliteActivity extends MasterActivity {

    private static final String SATELITTE_DISPLAY = "SATELITTE_DISPLAY";
    private static final String NOAA_SATELLITE =  "NOAA_SATELITTE";
    private static final String GEOS_SATELLITE = "GEOS_SATELITTE" ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected Fragment createFragment() {
        String satelliteType = getIntent().getExtras().getString(SATELITTE_DISPLAY);
        if (satelliteType != null) {
            switch (satelliteType) {
                case NOAA_SATELLITE:
                    return getSatelliteFragment();
                case GEOS_SATELLITE:
                    return getGeosSatelliteFragment();
                default:
                    finish();
            }
        }
        return null;
    }

    private Fragment getSatelliteFragment() {
        return NoaaSatelliteImageFragment.newInstance();

    }

    private Fragment getGeosSatelliteFragment() {
        return GeosSatelliteFragment.newInstance();
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static SatelliteActivity.Builder getBuilder() {
            SatelliteActivity.Builder builder = new SatelliteActivity.Builder();
            return builder;
        }

        public SatelliteActivity.Builder displayNoaaSatellite() {
            bundle.putString(SATELITTE_DISPLAY, NOAA_SATELLITE);
            return this;
        }

        public SatelliteActivity.Builder displayGeosSatellite() {
            bundle.putString(SATELITTE_DISPLAY, GEOS_SATELLITE);
            return this;
        }
        public Intent build(Context context) {
            Intent intent = new Intent(context, SatelliteActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
