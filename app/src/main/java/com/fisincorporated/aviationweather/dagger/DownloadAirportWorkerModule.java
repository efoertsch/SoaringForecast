package com.fisincorporated.aviationweather.dagger;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

@Module
public class DownloadAirportWorkerModule {

    @Provides
    @Named("CHANNEL_ID")
    String provideChannelId() {
        return "Soaring Weather";
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttpClient(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        return httpClient.build();

    }

}
