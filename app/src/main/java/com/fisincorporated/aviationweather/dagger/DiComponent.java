package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.airports.AirportListActivity;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface DiComponent {

    void inject(AirportWeatherActivity activity);

    void inject(AirportListActivity activity);



}

