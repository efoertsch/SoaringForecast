package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.app.SoaringWeatherApplication;
import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.retrofit.AviationWeatherApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastImage;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

// TODO -  break up into separate modules - app level, activity/fragment level

@Module
public class AppModule {

    private static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    private BitmapCache bitmapCache;

    private Context appContext;

    @Provides
    @Named(AIRPORT_PREFS)
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }


    // For unit testing only
    public AppModule() {
    }

    public AppModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @Singleton
    Context provideContext(SoaringWeatherApplication application) {
        return appContext;
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences() {
        return new AppPreferences(appContext, AIRPORT_PREFS);
    }

    @Provides
    @Singleton
    public AviationWeatherApi providesAviationWeatherApi(@Named("interceptor") OkHttpClient okHttpClient) {
        return new AviationWeatherGovRetrofit(okHttpClient).getRetrofit().create(AviationWeatherApi.class);
    }

    @Provides
    @Singleton
    public Interceptor getInterceptor() {
        return new LoggingInterceptor();
    }

    @Provides @Named("rasp_url")
    @Singleton
    public String getRaspUrl(){
        return appContext.getString(R.string.rasp_url);
    }

    @Provides
    @Singleton
    public Cache<String, SatelliteImage> provideCache() {
        return new Cache2kBuilder<String, SatelliteImage>() {
        }
                .name("Satellite Images Cache")
                .eternal(false)
                .expireAfterWrite(15, TimeUnit.MINUTES)    // expire/refresh after 15 minutes
                .entryCapacity(20)
                .build();
    }

    @Provides
    @Singleton
    public BitmapCache getBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = BitmapCache.init(appContext);
        }
        return bitmapCache;
    }


    @Provides
    @Singleton
    public BitmapImageUtils provideBitmapImageUtils(@Named("no_interceptor") OkHttpClient okHttpClient) {
        return new BitmapImageUtils(getBitmapCache(), okHttpClient);
    }

    @Provides
    @Singleton
    public Cache<String, SoaringForecastImage> provideSoaringForecastImageCache() {
        return new Cache2kBuilder<String, SoaringForecastImage>() {
        }
                .name("SoaringForecast Images Cache")
                .eternal(false)
                .expireAfterWrite(30, TimeUnit.MINUTES)    // expire/refresh after 15 minutes
                .entryCapacity(20)
                .build();
    }

}
