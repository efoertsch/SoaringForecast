package com.fisincorporated.soaringforecast.dagger;

import android.content.Context;

import com.fisincorporated.soaringforecast.workmanager.TurnpointsImportWorker;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;


@Singleton
@Component(modules = { TurnpointsImporterModule.class
        ,OkHttpClientModule.class
        , ChannelIdModule.class
        , AppRepositoryModule.class})

public interface TurnpointsImportComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        TurnpointsImportComponent build();
    }

    void inject(TurnpointsImportWorker turnpointsImportWorker);

}
