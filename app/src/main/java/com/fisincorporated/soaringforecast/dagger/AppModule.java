package com.fisincorporated.soaringforecast.dagger;

import android.content.Context;
import android.content.res.Resources;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.app.SoaringWeatherApplication;
import com.fisincorporated.soaringforecast.cache.BitmapCache;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherApi;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.soaringforecast.retrofit.LoggingInterceptor;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImage;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageType;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteRegion;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastImage;
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
