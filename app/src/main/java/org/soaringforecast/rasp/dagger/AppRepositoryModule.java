package org.soaringforecast.rasp.dagger;


import android.content.Context;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.StringUtils;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class AppRepositoryModule extends ForecastServerModule {

    @Provides
    @Singleton
    public AppRepository getAppRepository(Context context
            , @Named("interceptor") OkHttpClient okHttpClient
            , @Named("forecast_server_url") String forecastServerUrl
            , BitmapImageUtils bitmapImageUtils
            , @Named("forecast_server_url") String raspUrl
            , StringUtils stringUtils, AppPreferences appPreferences) {
        return AppRepository.getAppRepository(context, getSoaringForecastApi(okHttpClient, forecastServerUrl), bitmapImageUtils, raspUrl, stringUtils, appPreferences);
    }

    @Provides
    @Singleton
    public SoaringForecastApi getSoaringForecastApi(@Named("interceptor") OkHttpClient okHttpClient
            , String forecastServerUrl) {
        return getForecastServerRetrofit(okHttpClient, forecastServerUrl).getRetrofit().create(SoaringForecastApi.class);
    }

}
