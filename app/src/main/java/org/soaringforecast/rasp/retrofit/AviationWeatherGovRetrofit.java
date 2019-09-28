package org.soaringforecast.rasp.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class AviationWeatherGovRetrofit {

    private Retrofit retrofit;

    public AviationWeatherGovRetrofit(OkHttpClient okHttpClient, String aviationWeatherUrl ){
        retrofit = new Retrofit.Builder()
                .baseUrl(aviationWeatherUrl)
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }



}
