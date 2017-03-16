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
import okhttp3.Interceptor;
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
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences(){
        return new AppPreferences(application);
    }

    @Provides
    @Singleton
    public Retrofit provideAppRetrofit(){
        return new AppRetrofit(getInterceptor()).getRetrofit();
    }

    @Provides
    @Singleton
    public Interceptor getInterceptor(){
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
