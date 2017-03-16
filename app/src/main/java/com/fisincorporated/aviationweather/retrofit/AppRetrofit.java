package com.fisincorporated.aviationweather.retrofit;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class AppRetrofit {

    public static final String AVIATION_WEATHER_URL = "https://aviationweather.gov/adds/dataserver_current/";

    private Retrofit retrofit;

    @Inject
    public AppRetrofit(Interceptor interceptor){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(interceptor);
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(AVIATION_WEATHER_URL)
                .client(new OkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create());

        retrofit = builder.client(httpClient.build()).build();
    };

    public Retrofit getRetrofit() {
        return retrofit;
    }



}
