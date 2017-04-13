package com.fisincorporated.aviationweather.retrofit;


import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class SatelliteImageRetrofit {

    public static final String SATELLITE_WEATHER_URL = "https://aviationweather.gov/adds/data/satellite/";

    private Retrofit retrofit;

    @Inject
    public SatelliteImageRetrofit (){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(SATELLITE_WEATHER_URL)
                .client(new OkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create());

        retrofit = builder.client(httpClient.build()).build();
    };

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
