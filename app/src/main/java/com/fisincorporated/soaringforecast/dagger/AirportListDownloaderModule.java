package com.fisincorporated.soaringforecast.dagger;

import com.fisincorporated.soaringforecast.airport.AirportListDownloader;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import javax.inject.Named;

import dagger.Module;
import okhttp3.OkHttpClient;

@Module
public class AirportListDownloaderModule {

    AirportListDownloader getirportListDownloader(@Named("interceptor")OkHttpClient okHttpClient, AppRepository appRepository){
        return new AirportListDownloader(okHttpClient, appRepository);
    }
}
