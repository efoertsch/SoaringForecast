package com.fisincorporated.soaringforecast.dagger;



import android.content.Context;

import com.fisincorporated.soaringforecast.workmanager.DownloadAirportsWorker;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {OkHttpClientModule.class
        , ChannelIdModule.class
        , AppRepositoryModule.class})
public interface DownloadAirportWorkerComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        DownloadAirportWorkerComponent build();
    }

    // allow to inject into our Main class
    // method name not important
    void inject(DownloadAirportsWorker downloadAirportsWorker);

}
