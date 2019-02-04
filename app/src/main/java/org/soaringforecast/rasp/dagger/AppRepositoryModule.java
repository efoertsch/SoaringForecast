package org.soaringforecast.rasp.dagger;


import android.content.Context;

import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppRepositoryModule {

    @Provides
    @Singleton
    public AppRepository getAppRepository(Context context) {
        return  AppRepository.getAppRepository(context);
    }

}
