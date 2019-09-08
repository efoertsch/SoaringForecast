package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.airport.AirportListDownloader;
import org.soaringforecast.rasp.repository.AppRepository;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class AirportsDownloaderModule {

    @Provides
    AirportListDownloader getAirportListDownloader(OkHttpClient okHttpClient, AppRepository appRepository){
        return new AirportListDownloader(okHttpClient, appRepository);
    }
}
