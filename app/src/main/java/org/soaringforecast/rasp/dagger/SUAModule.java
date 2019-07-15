package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.soaring.forecast.SUAHandler;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class SUAModule extends ForecastServerModule{

    @Provides
    public JSONServerApi providesJSONServerApi(@Named("interceptor") OkHttpClient okHttpClient, @Named("forecast_server_url") String forecastServerUrl) {
        return getForecastServerRetrofit(okHttpClient, forecastServerUrl).getRetrofit().create(JSONServerApi.class);
    }

    @Provides
    public SUAHandler  providesSUAHandler(Context context, JSONServerApi jsonServerApi){
        return SUAHandler.getInstance(context, jsonServerApi);
    }

}
