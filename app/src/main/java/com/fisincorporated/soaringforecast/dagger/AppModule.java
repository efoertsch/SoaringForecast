package com.fisincorporated.soaringforecast.dagger;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.ObservableArrayList;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.app.SoaringWeatherApplication;
import com.fisincorporated.soaringforecast.cache.BitmapCache;
import com.fisincorporated.soaringforecast.dagger.annotations.AviationWeatherGov;
import com.fisincorporated.soaringforecast.dagger.annotations.SoaringForecast;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherApi;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.soaringforecast.retrofit.LoggingInterceptor;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImage;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageType;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteRegion;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastDownloader;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastImage;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastModel;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

// TODO - Consider breaking up into separate modules - Android specific, Retrofit and Cache

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
    @AviationWeatherGov
    public Retrofit provideAviationWeatherGovRetrofit() {
        return new AviationWeatherGovRetrofit(getOkHttpClient(getInterceptor())).getRetrofit();
    }

    @Provides
    @Singleton
    public AviationWeatherApi providesAviationWeatherApi() {
        return provideAviationWeatherGovRetrofit().create(AviationWeatherApi.class);
    }

    @Provides
    @Singleton
    public Interceptor getInterceptor() {
        return new LoggingInterceptor();
    }


    @Provides
    @Singleton
    @SoaringForecast
    public Retrofit provideSoaringForecastRetrofit() {
        return new SoaringForecastRetrofit(getOkHttpClient(getInterceptor())).getRetrofit();
    }

    @Provides
    @Singleton
    public SoaringForecastApi providesSoaringForecastApi() {
        return provideSoaringForecastRetrofit().create(SoaringForecastApi.class);
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
    public List<SatelliteRegion> provideSatelliteRegionArray() {
        ArrayList<SatelliteRegion> satelliteRegions = new ArrayList<>();
        Resources res = appContext.getResources();
        try {
            String[] regions = res.getStringArray(R.array.satellite_regions);
            for (int i = 0; i < regions.length; ++i) {
                SatelliteRegion satelliteRegion = new SatelliteRegion(regions[i]);
                satelliteRegions.add(satelliteRegion);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        return satelliteRegions;
    }

    @Provides
    @Singleton
    public List<SatelliteImageType> provideSatelliteImageType() {
        ArrayList<SatelliteImageType> satelliteImageTypes = new ArrayList<>();
        Resources res = appContext.getResources();
        try {
            String[] imageTypes = res.getStringArray(R.array.satellite_image_types);
            for (int i = 0; i < imageTypes.length; ++i) {
                SatelliteImageType satelliteImageType = new SatelliteImageType(imageTypes[i]);
                satelliteImageTypes.add(satelliteImageType);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        return satelliteImageTypes;
    }

    @Provides @Named("interceptor")
    @Singleton
    public OkHttpClient getOkHttpClient(Interceptor interceptor) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.addInterceptor(interceptor);
        return httpClient.build();
    }

    @Provides @Named("no_interceptor")
    @Singleton
    public OkHttpClient getOkHttpClientNoInterceptor() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        return httpClient.build();
    }

    @Provides
    @Singleton
    public List<SoaringForecastModel> provideSoaringForecastTypeArray() {
        String[] types;
        ObservableArrayList<SoaringForecastModel> soaringForecastModels = new ObservableArrayList<>();
        Resources res = appContext.getResources();
        try {
            types = res.getStringArray(R.array.soaring_forecast_models);
            for (int i = 0; i < types.length; ++i) {
                SoaringForecastModel soaringForecastModel = new SoaringForecastModel(types[i]);
                soaringForecastModels.add(soaringForecastModel);
            }
        } catch (Resources.NotFoundException nfe) {
        }
        return soaringForecastModels;
    }

    @Provides
    @Singleton
    public BitmapImageUtils provideBitmapImageUtils() {
        return new BitmapImageUtils(getBitmapCache(), getOkHttpClientNoInterceptor());
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

    // TODO put in separate activity module
    @Provides
    @Singleton
    public SoaringForecastDownloader provideSoaringForecastDownloader() {
        return new SoaringForecastDownloader(providesSoaringForecastApi(), provideBitmapImageUtils());
    }

}
