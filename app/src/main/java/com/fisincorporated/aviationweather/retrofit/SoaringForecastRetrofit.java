package com.fisincorporated.aviationweather.retrofit;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SoaringForecastRetrofit {

    public static final String SOARING_FORECAST_URL = "http://www.soargbsc.com/rasp/";

    private Retrofit retrofit;

    public SoaringForecastRetrofit(OkHttpClient okHttpClient ){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(SOARING_FORECAST_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = builder.build();
    };

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
