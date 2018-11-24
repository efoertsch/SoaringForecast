package com.fisincorporated.soaringforecast.dagger;

import android.content.Context;

import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastDownloader;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class SoaringForecastModule {

    @Provides
    public SoaringForecastApi providesSoaringForecastApi(@Named("interceptor")OkHttpClient okHttpClient, Context context) {
        return new SoaringForecastRetrofit(okHttpClient, context).getRetrofit().create(SoaringForecastApi.class);
    }

    @Provides
    public SoaringForecastDownloader provideSoaringForecastDownloader(@Named("no_interceptor")OkHttpClient okHttpClient,
                                                                      BitmapImageUtils bitmapImageUtils, Context context) {
        return new SoaringForecastDownloader(providesSoaringForecastApi(okHttpClient, context), bitmapImageUtils, context);
    }
}
