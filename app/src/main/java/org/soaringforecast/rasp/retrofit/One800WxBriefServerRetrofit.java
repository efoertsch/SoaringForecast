package org.soaringforecast.rasp.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class One800WxBriefServerRetrofit {

    private Retrofit retrofit;

    public One800WxBriefServerRetrofit(OkHttpClient okHttpClient, String one800WxBriefServerUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(one800WxBriefServerUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
