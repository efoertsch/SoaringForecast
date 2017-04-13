package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
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
    public AviationWeatherApi providesAviationWeatherApi() {
        return provideAppRetrofit().create(AviationWeatherApi.class);
    }


}
