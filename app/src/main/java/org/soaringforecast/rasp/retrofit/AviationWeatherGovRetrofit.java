package org.soaringforecast.rasp.retrofit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class AviationWeatherGovRetrofit {

    private static final String AVIATION_WEATHER_URL = "https://aviationweather.gov/adds/dataserver_current/";

    private Retrofit retrofit;

    @Inject
    public AviationWeatherGovRetrofit(OkHttpClient okHttpClient ){

        retrofit = new Retrofit.Builder()
                .baseUrl(AVIATION_WEATHER_URL)
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }



}
