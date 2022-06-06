package org.soaringforecast.rasp.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class TurnpointFileRetrofit {

    private static final String TURNPOINTS_URL = "https://soaringweb.org/TP/";

    private Retrofit retrofit;

    public TurnpointFileRetrofit(OkHttpClient okHttpClient ){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(TURNPOINTS_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }


}
