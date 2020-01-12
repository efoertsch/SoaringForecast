package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.retrofit.UsgsServerRetrofit;

import dagger.Module;
import okhttp3.OkHttpClient;

@Module
public class UsgsServerModule {

    private static UsgsServerRetrofit usgsServerRetrofit;

    protected UsgsServerRetrofit getUsgsServerRetrofit(OkHttpClient okHttpClient, String usgsServerUrl) {
        if (usgsServerRetrofit == null) {
            usgsServerRetrofit = new UsgsServerRetrofit(okHttpClient, usgsServerUrl);
        }
        return usgsServerRetrofit;
    }
}

