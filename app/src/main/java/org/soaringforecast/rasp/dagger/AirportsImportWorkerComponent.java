package org.soaringforecast.rasp.dagger;



import android.content.Context;

import org.soaringforecast.rasp.soaring.AppPreferencesModule;
import org.soaringforecast.rasp.workmanager.AirportsImportWorker;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {OkHttpClientModule.class
        , ChannelIdModule.class
        , AppRepositoryModule.class
        , BitmapImageModule.class
        , StringsUtilsModule.class
        , AppPreferencesModule.class
        , ForecastServerUrlModule.class
        , AirportsDownloaderModule.class
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
