package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.retrofit.AirportMetarService;
import com.fisincorporated.aviationweather.retrofit.AirportTafService;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class AppModule {

    public static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    @Provides
    @Named("app.shared.preferences.name")
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }

    private WeatherApplication application;

    private AppRetrofit appRetrofit;

    public AppModule(WeatherApplication application) {

        this.application = application;
        // only doing this to set default preferences if needed
        provideAppPreferences();
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences(){
        return new AppPreferences(application);
    }

    @Provides
    @Singleton
    public Retrofit provideAppRetrofit(){
        return new AppRetrofit(getLoggingInterceptor()).getRetrofit();
    }

    @Provides
    @Singleton
    public LoggingInterceptor getLoggingInterceptor(){
        return new LoggingInterceptor();
    }

    @Provides
    public AirportMetarService providesAirportMetarService() {
        return provideAppRetrofit().create(AirportMetarService.class);
    }

    @Provides
    public AirportTafService providesAirportTafService() {
        return provideAppRetrofit().create(AirportTafService.class);
    }
}
