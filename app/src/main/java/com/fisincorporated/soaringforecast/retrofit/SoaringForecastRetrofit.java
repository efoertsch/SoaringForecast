package com.fisincorporated.soaringforecast.retrofit;


import android.content.Context;

import com.fisincorporated.soaringforecast.R;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SoaringForecastRetrofit {

    private Retrofit retrofit;

    public SoaringForecastRetrofit(OkHttpClient okHttpClient, Context context ){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.rasp_url))
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
