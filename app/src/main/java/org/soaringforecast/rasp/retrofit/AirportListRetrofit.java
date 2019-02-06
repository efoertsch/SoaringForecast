package org.soaringforecast.rasp.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class AirportListRetrofit {

    public static final String AIRPORT_LIST_URL = "http://ourairports.com/data/";

    private Retrofit retrofit;

    public AirportListRetrofit(OkHttpClient okHttpClient ){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(AIRPORT_LIST_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
