package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.airports.AirportListActivity;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherFragment;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.satellite.SatelliteImageFragment;
import com.fisincorporated.aviationweather.settings.SettingsPreferenceFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface DiComponent {

    void inject(AirportWeatherFragment fragment);

    void inject(AirportListActivity activity);

    void inject(WeatherApplication weatherApplication);

    void inject(SettingsPreferenceFragment settingsPreferenceFragment);

    void inject(AppPreferences appPreferences);

    void inject(SatelliteImageFragment fragment);

}

