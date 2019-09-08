package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;

import dagger.Module;
import okhttp3.OkHttpClient;

@Module
public class ForecastServerModule {

    private static ForecastServerRetrofit forecastServerRetrofit;

    protected ForecastServerRetrofit getForecastServerRetrofit(OkHttpClient okHttpClient, String forecastServerUrl) {
        if (forecastServerRetrofit == null){
            forecastServerRetrofit =  new ForecastServerRetrofit(okHttpClient, forecastServerUrl);
        }
        return forecastServerRetrofit;
    }
}
