package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.airport.codelist.AirportListActivity;
import com.fisincorporated.aviationweather.airport.list.AirportListFragment;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherFragment;
import com.fisincorporated.aviationweather.satellite.SatelliteImageFragment;
import com.fisincorporated.aviationweather.settings.SettingsPreferenceFragment;
import com.fisincorporated.aviationweather.soaring.forecast.SoaringForecastFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {})
public abstract class UIBuildersModule {

    @ContributesAndroidInjector(modules = {})
    abstract AirportWeatherFragment bindAirportWeatherFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AirportListActivity bindAirportListActivity();

    @ContributesAndroidInjector(modules = {})
    abstract SettingsPreferenceFragment bindSettingsPreferenceFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SatelliteImageFragment bindSatelliteImageFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SoaringForecastFragment bindSoaringForecastFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AirportListFragment binAirportListFragment();
}


