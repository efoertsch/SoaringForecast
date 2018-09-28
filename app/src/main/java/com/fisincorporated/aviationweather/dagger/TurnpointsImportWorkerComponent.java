package com.fisincorporated.aviationweather.dagger;

import android.content.Context;

import com.fisincorporated.aviationweather.workmanager.TurnpointsImportWorker;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;


@Singleton
@Component(modules = { TurnpointProcessorModule.class
        , ChannelIdModule.class
        , AppRepositoryModule.class})

public interface TurnpointsImportWorkerComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        TurnpointsImportWorkerComponent build();
    }

    // allow to inject into our Main class
    // method name not important
    void inject(TurnpointsImportWorker turnpointsImportWorker);

}
