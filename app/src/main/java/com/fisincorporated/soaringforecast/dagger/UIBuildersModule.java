package com.fisincorporated.soaringforecast.dagger;

import com.fisincorporated.soaringforecast.airport.list.AirportListFragment;
import com.fisincorporated.soaringforecast.airportweather.AirportWeatherFragment;
import com.fisincorporated.soaringforecast.drawer.WeatherDrawerActivity;
import com.fisincorporated.soaringforecast.satellite.SatelliteImageFragment;
import com.fisincorporated.soaringforecast.settings.SettingsPreferenceFragment;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastFragment;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.fisincorporated.soaringforecast.task.download.TurnpointsImportFragment;
import com.fisincorporated.soaringforecast.task.list.TaskListFragment;
import com.fisincorporated.soaringforecast.task.search.TurnpointSearchFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {})
public abstract class UIBuildersModule {

    @ContributesAndroidInjector(modules = {})
    abstract WeatherDrawerActivity bindWeatherDrawerActivity();

    @ContributesAndroidInjector(modules = {})
    abstract AirportWeatherFragment bindAirportWeatherFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SettingsPreferenceFragment bindSettingsPreferenceFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SatelliteImageFragment bindSatelliteImageFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SoaringForecastFragment bindSoaringForecastFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AirportListFragment bindAirportListFragment();

    // ---- Turnpoints ----
    @ContributesAndroidInjector(modules = {})
    abstract TaskActivity bindTaskActivity();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointsImportFragment bindTurnpointsImportFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointSearchFragment bindTurnpointSearchFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TaskListFragment bindTaskListFragment();


}


