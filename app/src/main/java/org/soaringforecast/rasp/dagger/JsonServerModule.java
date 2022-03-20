package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.retrofit.JsonServerRetrofit;

import dagger.Module;
import okhttp3.OkHttpClient;

@Module
public class JsonServerModule {

    private static JsonServerRetrofit jsonServerRetrofit;

    protected JsonServerRetrofit getJsonServerRetrofit(OkHttpClient okHttpClient, String jsonServerUrl) {
        if (jsonServerRetrofit == null){
            jsonServerRetrofit =  new JsonServerRetrofit(okHttpClient, jsonServerUrl);
        }
        return jsonServerRetrofit;
    }
}
