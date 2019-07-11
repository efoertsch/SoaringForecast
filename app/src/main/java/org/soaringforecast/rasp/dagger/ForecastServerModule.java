package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;

import okhttp3.OkHttpClient;


public class ForecastServerModule {

    private static ForecastServerRetrofit forecastServerRetrofit;

    protected ForecastServerRetrofit getForecastServerRetrofit(OkHttpClient okHttpClient, String raspUrl) {
        if (forecastServerRetrofit == null){
            forecastServerRetrofit =  new ForecastServerRetrofit(okHttpClient, raspUrl);
        }
        return forecastServerRetrofit;
    }
}
