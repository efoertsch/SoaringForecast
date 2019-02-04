package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.airport.AirportListDownloader;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Named;

import dagger.Module;
import okhttp3.OkHttpClient;

@Module
public class AirportListDownloaderModule {

    AirportListDownloader getirportListDownloader(@Named("interceptor")OkHttpClient okHttpClient, AppRepository appRepository){
        return new AirportListDownloader(okHttpClient, appRepository);
    }
}
