package com.fisincorporated.aviationweather.dagger;

import android.content.res.Resources;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;
import com.fisincorporated.aviationweather.satellite.SatelliteImage;
import com.fisincorporated.aviationweather.satellite.SatelliteImageType;
import com.fisincorporated.aviationweather.satellite.SatelliteRegion;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import retrofit2.Retrofit;

// TODO - Consider breaking up into separate modules - Android specific, Retrofit and Cache

@Module
public class AppModule {

    public static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    @Provides
    @Named("app.shared.preferences.name")
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }

    private WeatherApplication application;

    public AppModule(WeatherApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences() {
        return new AppPreferences(application);
    }

    @Provides
    @Singleton
    public Retrofit provideAppRetrofit() {
        return new AppRetrofit(getInterceptor()).getRetrofit();
    }

    @Provides
    @Singleton
    public Interceptor getInterceptor() {
        return new LoggingInterceptor();
    }

    @Provides
    public AviationWeatherApi providesAviationWeatherApi() {
        return provideAppRetrofit().create(AviationWeatherApi.class);
    }

    @Provides
    @Singleton
    public Cache<String, SatelliteImage> provideCache() {
        return new Cache2kBuilder<String, SatelliteImage>() {}
                .name("Satellite Images Cache")
                .eternal(false)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .entryCapacity(15)
                .build();
    }

    @Provides
    @Singleton
    public List<SatelliteRegion> provideSatelliteRegionArray() {
        ArrayList<SatelliteRegion> satelliteRegions = new ArrayList<>();
        Resources res = application.getResources();
        try {
            String[] regions = res.getStringArray(R.array.satellite_regions);
            if (regions != null) {
                for (int i = 0; i < regions.length; ++i) {
                    SatelliteRegion satelliteRegion = new SatelliteRegion(regions[i]);
                    satelliteRegions.add(satelliteRegion);
                }
            }
        } catch(Resources.NotFoundException nfe){}
        return satelliteRegions;
    }

    @Provides
    @Singleton
    public List<SatelliteImageType> provideSatelliteImageType() {
        ArrayList<SatelliteImageType> satelliteImageTypes = new ArrayList<>();
        Resources res = application.getResources();
        try {
            String[] imageTypes = res.getStringArray(R.array.satellite_image_types);
            if (imageTypes != null) {
                for (int i = 0; i < imageTypes.length; ++i) {
                    SatelliteImageType satelliteImageType = new SatelliteImageType(imageTypes[i]);
                    satelliteImageTypes.add(satelliteImageType);
                }
            }
        } catch(Resources.NotFoundException nfe){}
        return satelliteImageTypes;
    }


}
