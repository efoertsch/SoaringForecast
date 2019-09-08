package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.soaringforecast.rasp.retrofit.AviationWeatherApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastImage;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

// TODO -  break up into modules by responsibility

@Module
public class AppModule {

    private Context appContext;

    // For unit testing only
    public AppModule() {
    }

    public AppModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return appContext;
    }


    @Provides
    @Singleton
    public AviationWeatherApi providesAviationWeatherApi(@Named("interceptor") OkHttpClient okHttpClient) {
        return new AviationWeatherGovRetrofit(okHttpClient).getRetrofit().create(AviationWeatherApi.class);
    }


    @Provides
    @Singleton
    public Cache<String, SatelliteImage> provideSatelliteImageCache() {
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
    public Cache<String, SoaringForecastImage> provideSoaringForecastImageCache() {
        return new Cache2kBuilder<String, SoaringForecastImage>() {
        }
                .name("SoaringForecast Images Cache")
                .eternal(false)
                .expireAfterWrite(30, TimeUnit.MINUTES)    // expire/refresh after 15 minutes
                .entryCapacity(20)
                .build();
    }

//    @Provides
//    @Singleton
//    public AppRepository getAppRepository(Context context
//            , @Named("interceptor") OkHttpClient okHttpClient
//            , @Named("forecast_server_url") String forecastServerUrl
//            , BitmapImageUtils bitmapImageUtils
//            , @Named("forecast_server_url") String raspUrl
//            , StringUtils stringUtils
//            , AppPreferences appPreferences) {
//        return AppRepository.getAppRepository(context, getSoaringForecastApi( okHttpClient,forecastServerUrl), bitmapImageUtils, raspUrl, stringUtils, appPreferences);
//    }

//    @Provides
//    public SoaringForecastApi getSoaringForecastApi(@Named("interceptor") OkHttpClient okHttpClient, String forecastServerUrl) {
//        return getForecastServerRetrofit(okHttpClient, forecastServerUrl).getRetrofit().create(SoaringForecastApi.class);
//    }
//
//    private static ForecastServerRetrofit forecastServerRetrofit;
//
//    protected ForecastServerRetrofit getForecastServerRetrofit(OkHttpClient okHttpClient, String forecastServerUrl) {
//        if (forecastServerRetrofit == null){
//            forecastServerRetrofit =  new ForecastServerRetrofit(okHttpClient, forecastServerUrl);
//        }
//        return forecastServerRetrofit;
//    }




}
