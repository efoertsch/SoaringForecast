package org.soaringforecast.rasp.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class JsonServerRetrofit {

    private Retrofit retrofit;

    public JsonServerRetrofit(OkHttpClient okHttpClient, String jsonServerUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(jsonServerUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
