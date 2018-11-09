package com.fisincorporated.soaringforecast.dagger;



import android.content.Context;

import com.fisincorporated.soaringforecast.workmanager.AirportsImportWorker;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {OkHttpClientModule.class
        , ChannelIdModule.class
        , AppRepositoryModule.class
        , AirportListDownloaderModule.class
})
public interface AirportsImportWorkerComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        AirportsImportWorkerComponent build();
    }

    // allow to inject into our Main class
    // method name not important
    void inject(AirportsImportWorker airportsImportWorker);

}
